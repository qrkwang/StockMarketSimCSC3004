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
	private final String ACCOUNTSERVER = "192.168.68.145"; // 192.168.87.54
	private final String ACCOUNTSERVER2 = "192.168.68.146"; // 192.168.87.55
	private final String ACCOUNTSERVER3 = "192.168.68.147"; // 192.168.87.56
	private final String USSERVERIPADDRESS = "192.168.68.148";
	private final String SGSERVERIPADDRESS = "192.168.68.149";
	private final String HKSERVERIPADDRESS = "192.168.68.150";
	
	private final String AccountsDBName = "AccountDetailsServer";
	private final String USDBName = "USStockMarket";
	private final String HKDBName = "HKStockMarket";
	private final String SGDBName = "SGStockMarket";
	
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

		accountDetailsDb = new AccountDetailsDbScript(ACCOUNTSERVER + ":3306", AccountsDBName, "root", "root");
		hkDb = new StockDBScript(Market.HK.name(), "HKMarket", HKSERVERIPADDRESS + ":3306", HKDBName, "root", "root",
				accountDetailsDb);
		sgDb = new StockDBScript(Market.SG.name(), "SGMarket", SGSERVERIPADDRESS + ":3306", SGDBName, "root", "root",
				accountDetailsDb);
		usaDb = new StockDBScript(Market.US.name(), "USMarket", USSERVERIPADDRESS + ":3306", USDBName, "root", "root",
				accountDetailsDb);

		clientHashMap = new HashMap<Integer, ClientInt>();
		logMap = new HashMap<Integer, String>(); // for log (will be server name and generation number)
		accountUser = "a";
		listServer = new ArrayList<>(Arrays.asList(ACCOUNTSERVER, ACCOUNTSERVER2, ACCOUNTSERVER3));
		leaseAlive = false;

		jedis = new Jedis();
		jedis.flushDB();
		lastSearchTimestamp = new HashMap<String, Long>();

		try {
			hkDb.startWaitForMsg();
			sgDb.startWaitForMsg();
			usaDb.startWaitForMsg();

		} catch (Exception e) {
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
	//Start the leader election aglgorithm
	public boolean startLeaderElectionAlgo() {
		String serverNo = null;
		boolean result = false;

		int generation = 0; // increase everytime it election a new leader
		if (leaseAlive == false) { // running for first time
			serverNo = electionLeader(listServer, null, generation);
			if (serverNo == null) {
				System.out.println("Fail to find any working server , please restart application or check server status");
			} else {
				System.out.println("Set up server " + serverNo);
				result = true;
			}
		}
		return result;
	}

	//Leaeder election algorithm
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
				System.out.println("Selected Server as a leader is " + selectedserver + " current generation no " + generation);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (leaseAlive == true && !logMap.isEmpty()) { // call the script once the node is ready to be called
			accountDetailsDb.setConnString(selectedserver, "AccountDetailsServer");
			System.out.println("running the accountDetailsDb leader");
		}
		return selectedserver; // return the leader
	}

	// Check the connection with the server
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

	// Set lease for the leader
	// Check for heartbeat and renew lease
	// End lease if failed heartbeat
	public void setLease(String ipname, String username, String password) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				boolean checkHeartbeatResult = false;
				try {
					System.out.println("Lease renewing, checking for heartbeat of leader server");
					//Check connection as the heartbeat
					checkHeartbeatResult = checkConnection(ipname, username, password, "AccountDetailsServer");
					if (checkHeartbeatResult == false) { // if heartbeat fail, end lease
						timer.cancel();  
						leaseAlive = false;
						System.out.println("Lease ended due to failed heartbeat");
						String[] serverDetailsLog = getLogResult(logMap);
						restartServer(serverDetailsLog[1], accountUser, "accountServer.py"); // try to restart server
						String resultElection = electionLeader(listServer, ipname,
								Integer.parseInt(serverDetailsLog[0])); // call election for new leader
						if (resultElection.isEmpty() || resultElection == null) {
							System.out.println("Fail to find any working server , please restart application or check server status");
						}
					}else {
						System.out.println("Lease renewed");
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
		System.out.println("Lease started");
		timer.schedule(task, 0, 3000); // trigger the lease to renew
	}

	// Attempt to restart the server
	public void restartServer(String ipAddr, String username, String fileName) {
		try {
			System.out.println("Restarting Server");
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

	// Get elected leader server and generation number
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
	//Start the redundancy algorithm
	public void startDataRedundancyAlgo() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				String failedServer = null;
				boolean usRequiredRecovery = false;
				boolean hkRequiredRecovery = false;
				boolean sgRequiredRecovery = false;
				boolean sgOnline = false;
				boolean hkOnline = false;
				boolean usOnline = false;
				//Check for connection to servers every minute
				while (true) {
					try {
						//If server failed, switch to temporary server
						if (sendPingRequest(USSERVERIPADDRESS) == false) {
							failedServer = Market.US.name();
							System.out.println("Cannot ping US");
							usOnline = false;
							usaDb.setConnString(SGSERVERIPADDRESS + ":3306", USDBName);
						} else {
							usOnline = true;
						}
						if (sendPingRequest(SGSERVERIPADDRESS) == false) {
							failedServer = Market.SG.name();
							System.out.println("Cannot ping SG");
							
							sgOnline = false;
							sgDb.setConnString(HKSERVERIPADDRESS + ":3306", SGDBName);
						} else {
							sgOnline = true;
						}
						if (sendPingRequest(HKSERVERIPADDRESS) == false) {
							failedServer = Market.HK.name();
							System.out.println("Cannot ping HK");
							hkOnline = false;
							hkDb.setConnString(USSERVERIPADDRESS + ":3306", HKDBName);
						} else {
							hkOnline = true;
						}
						
						// Server recovery
						if (failedServer != null && usRequiredRecovery == false && sgRequiredRecovery == false
								&& hkRequiredRecovery == false) {
							executeFile("src/sshRecoverIfFail.py", failedServer);
							if (failedServer.equals(Market.US.name())) {
								usRequiredRecovery = true;
							} else if (failedServer.equals(Market.HK.name())) {
								hkRequiredRecovery = true;
							} else if (failedServer.equals(Market.SG.name())) {
								sgRequiredRecovery = true;
							}

						}
						// Recover original data back to server and switch back to server from temporary server
						if ((usRequiredRecovery == true && usOnline == true)
								|| (sgOnline == true && sgRequiredRecovery == true)
								|| (hkOnline == true && hkRequiredRecovery == true)) {
							executeFile("src/sshRecoverOriginalServer.py", failedServer);

							if (failedServer.equals(Market.US.name())) {
								usaDb.setConnString(USSERVERIPADDRESS + ":3306", USDBName);
							} else if (failedServer.equals(Market.HK.name())) {
								hkDb.setConnString(HKSERVERIPADDRESS + ":3306", HKDBName);
							} else if (failedServer.equals(Market.SG.name())) {
								sgDb.setConnString(SGSERVERIPADDRESS + ":3306", SGDBName);
							}
							failedServer = null;
							usRequiredRecovery = false;
							sgRequiredRecovery = false;
							hkRequiredRecovery = false;

						}
						Thread.sleep(60000);

					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		});
		thread.start();
	}

	// Execute python file on failed server
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

	//Check connection wtih server
	public static boolean sendPingRequest(String ipAddress) throws UnknownHostException, IOException {
		InetAddress host = InetAddress.getByName(ipAddress);
		if (host.isReachable(5000))
			return true;
		else
			return false;
	}

	/*
	 * ----------------------CACHING----------------------
	 * 
	 */
	// Create Jedis key
	public String generateJedisKey(String market, int stockId) {
		return market + DELIMITER + stockId;
	}

	// Retrieve market and stock id from the key
	public HashMap<String, String> retrieveFromKey(String key) {
		HashMap<String, String> data = new HashMap<String, String>();
		String[] keySplit = key.split(Pattern.quote(DELIMITER));
		data.put("market", keySplit[0]);
		data.put("stockId", keySplit[1]);
		return data;
	}

	// Start the caching
	public void startCache() {
		// Update cache every minute
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						try {
							cacheMarket();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						checkStockCache();
						Thread.sleep(60000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	// Cache all market stocks
	public void cacheMarket() throws RemoteException {
		jedis.set(Market.US.name(), getAllStocksByMarket(Market.US));
		jedis.set(Market.HK.name(), getAllStocksByMarket(Market.HK));
		jedis.set(Market.SG.name(), getAllStocksByMarket(Market.SG));
	}

	// Retrieve specified market stocks
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
						e.printStackTrace();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			});
			thread.start();
			return jedis.get(market);
		} else
			cacheMarket();
			return  jedis.get(market);
	}

	// Check stocks' transactions that needed to be cache
	public void checkStockCache() {
		// Get the timestamp for each stocks queried by client
		Iterator it = lastSearchTimestamp.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Long> entry = (Entry<String, Long>) it.next();
			//If past 10 minutes, remove it from the caching and remove all cached data
			//Otherwise, update the cache
			if (System.currentTimeMillis() - entry.getValue() > 600000) {
				it.remove();
				jedis.del(entry.getKey());
				jedis.del(entry.getKey() + DELIMITER + "OrderBook");
			} else {
				try {
					HashMap<String, String> data = retrieveFromKey(entry.getKey());
					cacheCompletedOrders(data.get("market"), Integer.parseInt(data.get("stockId")));
					cacheOrderBook(data.get("market"), Integer.parseInt(data.get("stockId")));
				} catch (NumberFormatException | RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Cache stock completed transactions
	public String cacheCompletedOrders(String market, int stockId) {
		String key = generateJedisKey(market, stockId);
		String value = retrieveCompletedOrders(market, stockId);
		jedis.set(key, value);
		return value;
	}
	
	// Cache stock order book
	public String cacheOrderBook(String market, int stockId) throws RemoteException {
		String key = generateJedisKey(market, stockId);
		String value = retrieveOrderBook(market, stockId);
		if (value.equals("empty") && value.equals("error fetching"))
			value = null;
		jedis.set(key + DELIMITER + "OrderBook", value);
		return value;
	}

	// Retrieve stock from the cache
	public HashMap<String, String> retrieveStockCache(String market, int stockId, ClientInt client, boolean callback)
			throws RemoteException {
		String key = generateJedisKey(market, stockId);
		//Add timestamp for the client query
		lastSearchTimestamp.put(key, System.currentTimeMillis());
		HashMap<String, String> result = new HashMap<String, String>();
		// Callback client to update stock after 1 minute
		if (callback) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(60000);
						client.updateStock(market, stockId);
					} catch (InterruptedException | RemoteException e) {
						e.printStackTrace();
					}
				}
			});
			thread.start();
		}
		// Check if data cached,
		// If so, retrieve from cache
		// Otherwise, retrieve from database and cache the result
		if (jedis.exists(key)) {
			result.put("orderCompleted", jedis.get(key));
			result.put("orderBook", jedis.get(key + DELIMITER + "OrderBook"));
			return result;
		} else {
			String res = cacheCompletedOrders(market, stockId);
			String orderbook = cacheOrderBook(market, stockId);
			result.put("orderCompleted", res);
			result.put("orderBook", orderbook);
			return result;
		}
	}

	/*
	 * ----------------------DATABASE----------------------
	 * 
	 */
	
	// Save the client
	public void addToClientHashMap(ClientInt cc, int accountId) {
		clientHashMap.put(accountId, cc);

	}

	// Remove the client
	@Override
	public void removeFromClientHashMap(int accountId) throws RemoteException {
		usaDb.removeFromClientHashMap(accountId);
		hkDb.removeFromClientHashMap(accountId);
		sgDb.removeFromClientHashMap(accountId);

		if (clientHashMap.containsKey(accountId)) {
			clientHashMap.remove(accountId);
		}
	}

	// Retrieve the client
	public ClientInt retrieveClientIntFromHashMap(int accountId) {
		if (clientHashMap.containsKey(accountId)) {
			return clientHashMap.get(accountId);
		}
		return null;
	}

	// Check for log in
	// Retrieve account details
	@Override
	public String getAccountDetailsByUsernameAndPW(ClientInt cc, String username, String pw) throws RemoteException {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			String resAccountDetail = accountDetailsDb.getAccountDetails(username, pw);
			if (resAccountDetail == "not found") {
				return "not found";
			}
			JsonNode jsonNodeRoot = objectMapper.readTree(resAccountDetail);
			JsonNode jsonNodeAccountId = jsonNodeRoot.get("accountId");
			int accountId = Integer.parseInt(jsonNodeAccountId.asText());
			// Keep the client if succeed
			addToClientHashMap(cc, accountId);
			usaDb.addToClientHashMap(cc, accountId);
			hkDb.addToClientHashMap(cc, accountId);
			sgDb.addToClientHashMap(cc, accountId);

			return resAccountDetail;
		} catch (SQLException | JsonProcessingException e) {
			e.printStackTrace();
			return "problem";
		}
	}

	// Retrieve account details based on account Id
	@Override
	public String getAccountDetailsById(ClientInt cc, int accountId) throws RemoteException {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			String resAccountDetail = accountDetailsDb.getAccountDetailsById(accountId);
			if (resAccountDetail == "not found") {
				return "not found";
			}
			return resAccountDetail;
		} catch (SQLException e) {
			e.printStackTrace();
			return "problem";
		}
	}

	// Retrieve account holdings based on account Id
	@Override
	public ArrayList<StockOwned> getAccountHoldingsById(int accountId) throws RemoteException {
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
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		// price and stock is combined, price is avg price.

		return allStockOwned;
	}

	// Receive client order and add to respective message queue on RabbitMQ
	@Override
	public String sendOrder(int accountId, String market, String order, boolean randomGeneration)
			throws RemoteException {
		System.out.println("Sending order");
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

			System.out.println(" [x] Sent '" + order + "'" + QUEUE_NAME);
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Retrieve stock pending orders
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

	// Retrieve stock completed orders
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
			e.printStackTrace();
			return "error fetching";
		}
		return sb.toString();
	}

	// Retrieve stock order book
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
			e.printStackTrace();
			return "error fetching";
		}
		return sb.toString();
	}

	// Retrieve all stocks from the market
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
	//Start the random order generation to simulate active market
	public void startRandomOrderGeneration(Market market) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Generating new order");
				while (true) {
					try {
						StockDBScript db = null;
						String marketStr = "";
						if (market.equals(Market.US)) {
							db = usaDb;
							marketStr = Market.US.toString();
						} else if (market.equals(Market.SG)) {
							db = sgDb;
							marketStr = Market.SG.toString();
						} else {
							db = hkDb;
							marketStr = Market.HK.toString();
						}
						ArrayList<Stock> arrayListStocks = null;
						while (true) {
							arrayListStocks = db.getAllStocks();
							if (arrayListStocks != null) {
								break;
							}
						}
						System.out.println("All Stock Retrieved For " + marketStr + " - " + arrayListStocks);
						String message = "";
						for (Stock stock : arrayListStocks) {
							message = db.dbRandomOrderGeneration(stock.getStockId());
							sendOrder(0, marketStr, message, true);
							Thread.sleep(5000);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		thread.start();
	}
}
