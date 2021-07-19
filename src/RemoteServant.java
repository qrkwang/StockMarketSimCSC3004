import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import classes.MarketComplete;
import classes.MarketPending;
import classes.OrderBook;
import classes.Stock;
import classes.StockOwned;
import redis.clients.jedis.Jedis;

public class RemoteServant extends UnicastRemoteObject implements RemoteInterface {
	private AccountDetailsDbScript accountDetailsDb;
	private StockDBScript hkDb;
	private StockDBScript sgDb;
	private StockDBScript usaDb;
	private HashMap<Integer, ClientInt> clientHashMap; // accountId and clientInterface

	private HashMap<Integer, String> logMap; // for log (will be server name and generation number)
	private List<String> listServer;
	private final String ACCOUNTSERVER = "localhost";
	private final String ACCOUNTSERVER2 = "localhost"; 
	private final String ACCOUNTSERVER3 = "localhost"; 
	private final String USSERVERIPADDRESS = "localhost";
	private final String SGSERVERIPADDRESS = "localhost";
	private final String HKSERVERIPADDRESS = "localhost";

//	private final String ACCOUNTSERVER = "localhost:3306"; // 192.168.87.54
//	private final String ACCOUNTSERVER2 = "localhost:3306"; // 192.168.87.55
//	private final String ACCOUNTSERVER3 = "localhost:3306"; // 192.168.87.56
//	private final String USSERVERIPADDRESS = "localhost";
//	private final String SGSERVERIPADDRESS = "localhost";
//	private final String HKSERVERIPADDRESS = "localhost";
	private String accountUser;

	private boolean leaseAlive;

	private Jedis jedis;
	private HashMap<String, Long> lastSearchTimestamp;
	private final String DELIMITER = "|";

	private enum Market {
		SG, HK, US
	}

	public RemoteServant() throws RemoteException {
		super();
		System.out.format("Creating server object\n"); // Print to client that server object is being created once
		// constructor called.

		accountDetailsDb = new AccountDetailsDbScript(ACCOUNTSERVER + ":3306", "accountdetailsserver", "root", "root");
		hkDb = new StockDBScript("HKMarket", HKSERVERIPADDRESS + ":3306", "HKStockMarket", "root", "root",
				accountDetailsDb);
		sgDb = new StockDBScript("SGMarket", SGSERVERIPADDRESS + ":3306", "SGStockMarket", "root", "root",
				accountDetailsDb);
		usaDb = new StockDBScript("USMarket", USSERVERIPADDRESS + ":3306", "USStockMarket", "root", "root",
				accountDetailsDb);

		clientHashMap = new HashMap<Integer, ClientInt>();
		logMap = new HashMap<Integer, String>(); // for log (will be server name and generation number)
		accountUser = "a";
		listServer = new ArrayList<>(Arrays.asList(ACCOUNTSERVER, ACCOUNTSERVER2, ACCOUNTSERVER3));
		leaseAlive = false;

		jedis = new Jedis();
		lastSearchTimestamp = new HashMap<String, Long>();

		try {
			hkDb.startWaitForMsg();
			sgDb.startWaitForMsg();
			usaDb.startWaitForMsg();

		} catch (Exception e) {
			System.out.println("error start wait for msg");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		startRandomOrderGeneration(Market.US);
		startRandomOrderGeneration(Market.SG);
		startRandomOrderGeneration(Market.HK);
		startLeaderElectionAlgo();
		startDataRedundancyAlgo();
		startCache();

	}

	/*
	 * ----------------------LEADER ELECTION----------------------
	 * 
	 */
	public boolean startLeaderElectionAlgo() {
		String serverNo = null;
		boolean result = false;

		int generation = 0; // increase everytime it election a new leader
		if (leaseAlive == false) { // running for first time
			serverNo = electionLeader(listServer, null, generation);
			if (serverNo == null) {
				System.out
						.println("Fail to find any working server , please restart application or check server status");
			} else {
				System.out.println("Set up server " + serverNo);
				result = true;
			}
		}
		return result;
	}

	public String electionLeader(List<String> listServer, String currServer, int generation) {
		String selectedserver = null;
		List<String> serverlist = new ArrayList<String>(listServer);
		HashMap<String, Long> rankListServer = new HashMap<>();
		// long startTimeSelectedServer = System.currentTimeMillis();
		try {
			for (int i = 0; i < serverlist.size(); i++) {
				// rank them by the faster server speed
				long startTime = System.nanoTime();
				boolean connectionResult = checkConnection(serverlist.get(i), "root", "root", "AccountDetailsServer");
				long endTime = System.nanoTime();
				long total = endTime - startTime;
				System.out.println("total time for " + total + " server running = " + serverlist.get(i));
				System.out.println("server result connection " + connectionResult);
				if (connectionResult == true) {
					rankListServer.put(serverlist.get(i), total); // adding result that pass the connection
				}
			}
			// sorting of map to get the best time result smaller to the bigger
			Map<String, Long> sortedServerList = rankListServer.entrySet().stream().sorted(Entry.comparingByValue())
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			System.out.println(Arrays.asList(sortedServerList));
			generation = generation + 1; // increase count every new election with leader
			if (!rankListServer.isEmpty()) {
				selectedserver = sortedServerList.keySet().stream().findFirst().get();// get the first key
				logMap.put(generation, selectedserver); // add the generation and log map
				setLease(selectedserver, "root", "root"); // once elected leader start the lease time
				System.out.println(
						"Selected Server as a leader is " + selectedserver + " current generation no " + generation);
				// for testing
				/*
				 * long endTimeSelectedServer = System.currentTimeMillis(); long
				 * totalTimeSelectedServer = endTimeSelectedServer - startTimeSelectedServer;
				 * System.out.println("total time for leader election to select a new sever - "
				 * + totalTimeSelectedServer);
				 */
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (leaseAlive == true && !logMap.isEmpty()) { // call the script once the node is ready to be called
			accountDetailsDb.setConnString(selectedserver, "AccountDetailsServer");
			System.out.println("running the accountDetailsDb leader");
		}
		return selectedserver; // return the leader
	}

	// act like heartbeat to check if connection exist or not
	public static boolean checkConnection(String ipname, String username, String password, String dbname)
			throws SQLException {
		Connection con = null;
		boolean result = false;
		String CONN_STRING = "jdbc:mysql://" + ipname + "/" + dbname;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = (Connection) DriverManager.getConnection(CONN_STRING, username, password);
			if (con != null) {
				result = true; // able to connect to db
				con.close();
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

	// set a lease to run in backgroup for the leader
	public void setLease(String ipname, String username, String password) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				boolean checkHeartbeatResult = false;
				try {
					checkHeartbeatResult = checkConnection(ipname, username, password, "AccountDetailsServer");
					System.out.println("task have expired , ready to check for renew");
					if (checkHeartbeatResult == false) { // check if it ok to reset the lease , if heartbeat fail no
						timer.cancel(); // cancel all the schedule task that maybe happending
						leaseAlive = false;
						System.out.println("time out unable to lease due to error");
						String[] serverDetailsLog = getLogResult(logMap);
						restartServer(serverDetailsLog[1], accountUser, "accountServer.py"); // try to restart server
						String resultElection = electionLeader(listServer, ipname,
								Integer.parseInt(serverDetailsLog[0])); // call for election again to get new leader
						if (resultElection.isEmpty() || resultElection == null) {
							System.out.println(
									"Fail to find any working server , please restart application or check server status");
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
					cancel(); // if there is exception also cancel the lease
					leaseAlive = false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		leaseAlive = true;
		System.out.println("lease have been renew");
		timer.schedule(task, 0, 3000); // to trigger to reschedule the lease will repeat itself till the condition is
										// met
	}

	public void restartServer(String ipAddr, String username, String fileName) {
		try {
			String path = new File(fileName).getAbsolutePath();
			String newPath = new File(path).getParent();
			String[] cmd = { "python", newPath + "\\src\\" + fileName, ipAddr, username };
			Process process = Runtime.getRuntime().exec(cmd);

			String result;
			String error;

			BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((result = stdout.readLine()) != null) {
				System.out.println(result);
			}

			while ((error = stdError.readLine()) != null) {
				System.out.println(error);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String[] getLogResult(HashMap<Integer, String> log) {
		String logMapResult = log.entrySet().toArray()[log.size() - 1].toString(); // trying to get last value
		String[] resultgenserver = logMapResult.split("="); // get back the last election leader server & generation
															// number
		return resultgenserver;
	}

	/*
	 * ----------------------DATA REDUNDANCY----------------------
	 * 
	 */
	public void startDataRedundancyAlgo() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				String failedServer = null;
				boolean usRequiredRecovery = false;
				boolean hkRequiredRecovery = false;
				boolean sgRequiredRecovery = false;
				while (true) {
					try {
						if (sendPingRequest(USSERVERIPADDRESS) == false) {
							failedServer = "US";
							System.out.println("Cannot ping US");
							usaDb.setOnline(false);
						} else {
							usaDb.setOnline(true);
						}
						if (sendPingRequest(SGSERVERIPADDRESS) == false) {
							failedServer = "SG";
							System.out.println("Cannot ping SG");
							sgDb.setOnline(false);
						} else {
							sgDb.setOnline(true);
						}
						if (sendPingRequest(HKSERVERIPADDRESS) == false) {
							failedServer = "HK";
							System.out.println("Cannot ping HK");
							hkDb.setOnline(false);
						} else {
							hkDb.setOnline(true);
						}

						if (failedServer != null && usRequiredRecovery == false && sgRequiredRecovery == false
								&& hkRequiredRecovery == false) {
							executeFile("src/sshRecoverIfFail.py", failedServer);
							if (failedServer.equals("US")) {
								usRequiredRecovery = true;
							} else if (failedServer.equals("HK")) {
								hkRequiredRecovery = true;
							} else if (failedServer.equals("SG")) {
								sgRequiredRecovery = true;
							}

						}
						if ((usRequiredRecovery == true && usaDb.isOnline() == true)
								|| (sgDb.isOnline() == true && sgRequiredRecovery == true)
								|| (hkDb.isOnline() == true && hkRequiredRecovery == true)) {
							executeFile("src/sshRecoverOriginalServer.py", failedServer);
							failedServer = null;
							usRequiredRecovery = false;
							sgRequiredRecovery = false;
							hkRequiredRecovery = false;

						}
						Thread.sleep(60000);

					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});
		thread.start();
	}

	public static void executeFile(String fileName, String failedServer) {
		try {
			String[] cmd = { "python", fileName, failedServer };

			Process p = Runtime.getRuntime().exec(cmd);

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			System.out.println("running file " + fileName);

			while ((line = reader.readLine()) != null) {
				System.out.println(line + "\n");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static boolean sendPingRequest(String ipAddress) throws UnknownHostException, IOException {
		InetAddress host = InetAddress.getByName(ipAddress);

//		  System.out.println("Sending Ping Request to " + ipAddress);
		if (host.isReachable(5000))
			return true;
		else
			return false;
	}

	/*
	 * ----------------------CACHING----------------------
	 * 
	 */
	public void startCache() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						cacheMarket();
						checkStockCache();
						Thread.sleep(60000);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	public void cacheMarket() {
		jedis.set(Market.US.name(), getAllStocksByMarket(Market.US));
		jedis.set(Market.HK.name(), getAllStocksByMarket(Market.HK));
		jedis.set(Market.SG.name(), getAllStocksByMarket(Market.SG));
	}

	public String retrieveMarketCache(String market, ClientInt client) throws RemoteException {
		if (jedis.exists(market)) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(60000);
						client.updateMarket(market);
					} catch (ConnectException e) {
						Thread.currentThread().interrupt();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			thread.start();
			return jedis.get(market);
		} else
			return null;
	}

	public void checkStockCache() {
		Iterator it = lastSearchTimestamp.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Long> entry = (Entry<String, Long>) it.next();
			if (System.currentTimeMillis() - entry.getValue() > 600000) {
				it.remove();
				jedis.del(entry.getKey());
				jedis.del(entry.getKey() + DELIMITER + "OrderBook");
			} else {
				cacheCompletedOrders(entry.getKey());
				cacheOrderBook(entry.getKey());
			}
		}
	}

	public String cacheCompletedOrders(String key) {
		String[] keySplit = key.split(Pattern.quote(DELIMITER));
		String market = keySplit[0];
		int stockid = Integer.parseInt(keySplit[1]);
		String value = retrieveCompletedOrders(market, stockid);
		jedis.set(key, value);
		return value;
	}

	public String cacheOrderBook(String key) {
		String[] keySplit = key.split(Pattern.quote(DELIMITER));
		String market = keySplit[0];
		int stockid = Integer.parseInt(keySplit[1]);
		String value = retrieveOrderBook(market, stockid);
		System.out.println("Order Book: " + value);
		jedis.set(key + DELIMITER + "OrderBook", value);
		return value;
	}

	public String retrieveStockCache(String market, int stockid, ClientInt client) throws RemoteException {
		String key = market + DELIMITER + stockid;
		lastSearchTimestamp.put(key, System.currentTimeMillis());
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(60000);
					client.updateStock(market, stockid);
				} catch (InterruptedException | RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		if (jedis.exists(key)) {
			String orderbook = jedis.get(key + DELIMITER + "OrderBook");
			client.sendOrderBook(orderbook);
			return jedis.get(key);
		} else {
			// Retrieve from database and cache if not found
			String res = cacheCompletedOrders(key);
			String orderbook = cacheOrderBook(key);
			client.sendOrderBook(orderbook);
			return res;
		}
	}

	/*
	 * ----------------------DATABASE----------------------
	 * 
	 */
	public void addToClientHashMap(ClientInt cc, int accountId) {
		clientHashMap.put(accountId, cc);

	}

	@Override
	public void removeFromClientHashMap(int accountId) throws RemoteException {
		if (clientHashMap.containsKey(accountId)) {
			clientHashMap.remove(accountId);
		}
	}

	public ClientInt retrieveClientIntFromHashMap(int accountId) {
		System.out.println("retrieve from hashmap print");
		System.out.println(clientHashMap);

		if (clientHashMap.containsKey(accountId)) {
			return clientHashMap.get(accountId);
		}
		return null;
	}

	@Override
	public String getAccountDetailsByUsernameAndPW(ClientInt cc, String username, String pw) throws RemoteException {
		System.out.println("servantgetaccountdetailsybusernameandpw " + username + pw);
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			String resAccountDetail = accountDetailsDb.getAccountDetails(username, pw);
			System.out.println("result from db script" + resAccountDetail);
			if (resAccountDetail == "not found") {
				return "not found";
			}
			JsonNode jsonNodeRoot = objectMapper.readTree(resAccountDetail);
//			JsonNode jsonNodePW = jsonNodeRoot.get("password");
			JsonNode jsonNodeAccountId = jsonNodeRoot.get("accountId");
//			String password = jsonNodePW.asText();
			int accountId = Integer.parseInt(jsonNodeAccountId.asText());
			addToClientHashMap(cc, accountId);
			return resAccountDetail;
		} catch (SQLException | JsonProcessingException e) {
			// TODO Auto-generated catch block
			System.out.println("sql or json processing exception");
			e.printStackTrace();
			return "problem";
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<StockOwned> getAccountHoldingsById(int accountId) throws RemoteException {
		System.out.println(" in remote srevant account id " + accountId);
		ArrayList<StockOwned> allStockOwned = new ArrayList<StockOwned>();
		try {
			ArrayList<StockOwned> stockOwnedHk = hkDb.getOwnedStocks(accountId);
			ArrayList<StockOwned> stockOwnedSg = sgDb.getOwnedStocks(accountId);
			ArrayList<StockOwned> stockOwnedUsa = usaDb.getOwnedStocks(accountId);

			if (stockOwnedHk != null) {
				allStockOwned.addAll(stockOwnedHk);
			}
			if (stockOwnedSg != null) {
				allStockOwned.addAll(stockOwnedSg);

			}
			if (stockOwnedUsa != null) {
				allStockOwned.addAll(stockOwnedUsa);

			}

			System.out.println("printing stock owned by account id " + accountId);

			allStockOwned.forEach(item -> {
				System.out.println(item);
			});

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		// price and stock is combined, price is avg price.

		return allStockOwned;
	}

	@Override
	public String sendOrder(int accountId, String market, String order, boolean randomGeneration)
			throws RemoteException {
		System.out.println("sending order");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");

		String QUEUE_NAME = "";

		// Get the clientInt of that accountId first then set on the DB class.
		ClientInt user = this.retrieveClientIntFromHashMap(accountId);
		if (!randomGeneration) {
			if (market.equals("US")) {
				QUEUE_NAME = "USMarket";
				hkDb.addToClientHashMap(user, accountId);
			} else if (market.equals("HK")) {
				QUEUE_NAME = "HKMarket";
				hkDb.addToClientHashMap(user, accountId);
			} else {
				QUEUE_NAME = "SGMarket";
				hkDb.addToClientHashMap(user, accountId);
			}
		} else {
			if (market.equals("US")) {
				QUEUE_NAME = "USMarket";
			} else if (market.equals("HK")) {
				QUEUE_NAME = "HKMarket";
			} else {
				QUEUE_NAME = "SGMarket";
			}
		}
		try (com.rabbitmq.client.Connection connection = factory.newConnection();
				Channel channel = connection.createChannel()) {
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			channel.basicPublish("", QUEUE_NAME, null, order.getBytes());

			System.out.println(" [x] Sent '" + order + "'");
		} catch (IOException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("resource")
	public String retrievePendingOrders(String market, int stockId) {
		StringBuilder sb = new StringBuilder();
		ArrayList<MarketPending> arrayListStocks = null;
		try {
			if (market.equals("US"))
				arrayListStocks = usaDb.getPendingOrders(stockId);
			else if (market.equals("HK"))
				arrayListStocks = hkDb.getPendingOrders(stockId);
			else
				arrayListStocks = sgDb.getPendingOrders(stockId);

			if (arrayListStocks == null)
				return "empty";
			// Serialize list of object to string for returning to client.
			new ObjectOutputStream(new OutputStream() {
				@Override
				public void write(int i) throws IOException {
					sb.append((char) i);
				}
			}).writeObject(arrayListStocks);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			return "error fetching";
		}
		return sb.toString();
	}

	@SuppressWarnings("resource")
	public String retrieveCompletedOrders(String market, int stockId) {
		ArrayList<MarketComplete> arrayListStocks = null;
		StringBuilder sb = new StringBuilder();

		try {
			if (market.equals("US"))
				arrayListStocks = usaDb.getCompletedOrders(stockId);
			else if (market.equals("HK"))
				arrayListStocks = hkDb.getCompletedOrders(stockId);
			else
				arrayListStocks = sgDb.getCompletedOrders(stockId);

			if (arrayListStocks == null)
				return "empty";
			// Serialize list of object to string for returning to client.
			new ObjectOutputStream(new OutputStream() {
				@Override
				public void write(int i) throws IOException {
					sb.append((char) i);
				}
			}).writeObject(arrayListStocks);
		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error fetching";
		}
		return sb.toString();
	}

	@SuppressWarnings("resource")
	public String retrieveOrderBook(String market, int stockId) {
		ArrayList<OrderBook> arrayListStocks = null;
		StringBuilder sb = new StringBuilder();

		try {
			if (market.equals("US"))
				arrayListStocks = usaDb.getOrderBook(stockId);
			else if (market.equals("HK"))
				arrayListStocks = hkDb.getOrderBook(stockId);
			else
				arrayListStocks = sgDb.getOrderBook(stockId);

			if (arrayListStocks == null)
				return "empty";
			// Serialize list of object to string for returning to client.
			new ObjectOutputStream(new OutputStream() {
				@Override
				public void write(int i) throws IOException {
					sb.append((char) i);
				}
			}).writeObject(arrayListStocks);
		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error fetching";
		}
		return sb.toString();
	}

	@SuppressWarnings("resource")
	public String getAllStocksByMarket(Market market) {
		StringBuilder sb = new StringBuilder();
		ArrayList<Stock> arrayListStocks = null;
		try {
			if (market.equals(Market.US))
				arrayListStocks = usaDb.getAllStocks();
			else if (market.equals(Market.HK))
				arrayListStocks = hkDb.getAllStocks();
			else if (market.equals(Market.SG))
				arrayListStocks = sgDb.getAllStocks();

			if (arrayListStocks == null)
				return "empty";
			// Serialize list of object to string for returning to client.
			new ObjectOutputStream(new OutputStream() {
				@Override
				public void write(int i) throws IOException {
					sb.append((char) i);
				}
			}).writeObject(arrayListStocks);

		} catch (SQLException | IOException e) {
			e.printStackTrace();
			return "error fetching";
		}
		return sb.toString();
	}
	/*
	 * ----------------------RANDOM ORDER GENERATION----------------------
	 * 
	 */

	public void startRandomOrderGeneration(Market market) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Generating new order");

				try {
					ArrayList<Stock> arrayListStocks = null;

					if (market.equals(market.US)) {
						arrayListStocks = usaDb.getAllStocks();
					} else if (market.equals(market.SG)) {
						arrayListStocks = sgDb.getAllStocks();
					} else {
						arrayListStocks = hkDb.getAllStocks();
					}

					String message = "";
					for (Stock stock : arrayListStocks) {
						for (int i = 0; i < 20; i++) {
							if (market.equals(market.US)) {
								message = usaDb.dbRandomOrderGeneration(stock.getStockId());
								sendOrder(0, market.US.toString(), message, true);
							} else if (market.equals(market.SG)) {
								message = sgDb.dbRandomOrderGeneration(stock.getStockId());
								sendOrder(0, market.SG.toString(), message, true);
							} else {
								message = hkDb.dbRandomOrderGeneration(stock.getStockId());
								sendOrder(0, market.HK.toString(), message, true);
							}

						}

					}

					// TODO Auto-generated method stub

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		thread.start();
	}
}
