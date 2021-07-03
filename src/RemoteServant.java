import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
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
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import classes.MarketComplete;
import classes.MarketPending;
import classes.Stock;
import classes.StockOwned;
import redis.clients.jedis.Jedis;

public class RemoteServant extends UnicastRemoteObject implements RemoteInterface {
	private AccountDetailsDbScript accountDetailsDb;
	private HKDbScript hkDb;
	private SGDbScript sgDb;
	private USADbScript usaDb;
	private HashMap<Integer, ClientInt> clientHashMap; // accountId and clientInterface

	private HashMap<String, Integer> logMap; // for log (will be server name and generation number)
	private List<String> listServer;
	private String accountServer;
	private String accountServer2;
	private String accountServer3;
	private String accountUser;
	private static String USServerIPAddress;
	private static String SGServerIPAddress;
	private static String HKServerIPAddress;

	private boolean leaseAlive;

	private Jedis jedis;
	private HashMap<String, Long> lastSearchTimestamp;
	private final String DELIMITER = "|";

	public RemoteServant() throws RemoteException {
		super();
		accountDetailsDb = new AccountDetailsDbScript(); // Start the RabbitMQ Receiver that's in main method
		hkDb = new HKDbScript(); // Start the RabbitMQ Receiver that's in main method
		sgDb = new SGDbScript(); // Start the RabbitMQ Receiver that's in main method
		usaDb = new USADbScript(); // Start the RabbitMQ Receiver that's in main method
		clientHashMap = new HashMap<Integer, ClientInt>();

		logMap = new HashMap<String, Integer>(); // for log (will be server name and generation number)
		USServerIPAddress = "192.168.43.185";
		SGServerIPAddress = "192.168.43.210";
		HKServerIPAddress = "192.168.43.74";

		accountServer = "192.168.87.54";
		accountServer2 = "192.168.87.55";
		accountServer3 = "192.168.87.56";
		accountUser = "wh1901877";
		listServer = new ArrayList<>(Arrays.asList(accountServer, accountServer2, accountServer3));
		leaseAlive = false;

		jedis = new Jedis();
		lastSearchTimestamp = new HashMap<String, Long>();

		try {
			accountDetailsDb.startWaitForMsg();
			hkDb.startWaitForMsg();
			sgDb.startWaitForMsg();
			usaDb.startWaitForMsg();

		} catch (Exception e) {
			System.out.println("error start wait for msg");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.format("Creating server object\n"); // Print to client that server object is being created once
														// constructor called.
	}

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
		ClientInt retrievedClientInt = null;
		System.out.println("retrieve from hashmap print");
		System.out.println(clientHashMap);

		if (clientHashMap.containsKey(accountId)) {
			retrievedClientInt = clientHashMap.get(accountId);

		}
		return retrievedClientInt;
	}

	public void startLeaderElectionAlgo() throws RemoteException {
		String serverNo = null;
		int generation = 0; // increase everytime it election a new leader
		if (leaseAlive == false && serverNo == null) { // running for first time
			serverNo = electionLeader(listServer, null, generation);
			if (serverNo == null) {
				System.out
						.println("Fail to find any working server , please restart application or check server status");
			} else {
				System.out.println("Set up server " + serverNo);
			}
		}
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

	public void startDataRedundancyAlgo() throws RemoteException {

		String failedServer = null;
		boolean usRequiredRecovery = false;
		boolean hkRequiredRecovery = false;
		boolean sgRequiredRecovery = false;

		while (true) {
			try {
				if (sendPingRequest(USServerIPAddress) == false) {
					failedServer = "US";
					System.out.println("Cannot ping US");
					usaDb.setOnline(false);
				} else {
					usaDb.setOnline(true);
				}
				if (sendPingRequest(SGServerIPAddress) == false) {
					failedServer = "SG";
					System.out.println("Cannot ping SG");
					sgDb.setOnline(false);
				} else {
					sgDb.setOnline(true);
				}
				if (sendPingRequest(HKServerIPAddress) == false) {
					failedServer = "HK";
					System.out.println("Cannot ping HK");
					hkDb.setOnline(false);
				} else {
					hkDb.setOnline(true);
				}

				if (failedServer != null && usRequiredRecovery == false && sgRequiredRecovery == false
						&& hkRequiredRecovery == false) {
					executeFile("sshRecoverIfFail.py", failedServer);
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
					executeFile("sshRecoverOriginalServer.py", failedServer);
					failedServer = null;
					usRequiredRecovery = false;
					sgRequiredRecovery = false;
					hkRequiredRecovery = false;

				}
				Thread.sleep(360);

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

	public static boolean sendPingRequest(String ipAddress) throws UnknownHostException, IOException {
		InetAddress host = InetAddress.getByName(ipAddress);

//		  System.out.println("Sending Ping Request to " + ipAddress);
		if (host.isReachable(5000))
			return true;
		else
			return false;
	}

	public String electionLeader(List<String> listServer, String currServer, int generation) {
		String selectedserver = null;
		List<String> serverlist = new ArrayList<String>(listServer);
		HashMap<String, Long> rankListServer = new HashMap<>();

		try {

			/*
			 * if (!logMap.isEmpty()) { //should be remove due to the server can restart int
			 * index = serverlist.indexOf(currServer);// remove the server that unable to
			 * run temp serverlist.remove(index); }
			 */
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
				logMap.put(selectedserver, generation); // add the generation and log map
				setLease(selectedserver, "root", "root"); // once elected leader start the lease time
				System.out.println(
						"Selected Server as a leader is " + selectedserver + " current generation no " + generation);
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
						restartServer(serverDetailsLog[0], accountUser, "accountServer.py"); // try to restart server
						String resultElection = electionLeader(listServer, ipname,
								Integer.parseInt(serverDetailsLog[1])); // call for election again to get new leader
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
			// e.printStackTrace();
		}
		return result;
	}

	public String[] getLogResult(HashMap<String, Integer> log) {
		String logMapResult = log.entrySet().toArray()[log.size() - 1].toString(); // trying to get last value
		String[] resultgenserver = logMapResult.split("="); // get back the last election leader server & generation
															// number
		return resultgenserver;
	}

	@Override
	public String getAccountDetailsByUsernameAndPW(ClientInt cc, String username, String pw) throws RemoteException {
		System.out.println("servantgetaccountdetailsybusernameandpw " + username + pw);
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			String resAccountDetail = accountDetailsDb.getAccountDetails(username);
			System.out.println("result from db script" + resAccountDetail);
			if (resAccountDetail == "not found") {
				return "not found";
			}
			JsonNode jsonNodeRoot = objectMapper.readTree(resAccountDetail);
			JsonNode jsonNodePW = jsonNodeRoot.get("password");
			JsonNode jsonNodeAccountId = jsonNodeRoot.get("accountId");
			String password = jsonNodePW.asText();
			int accountId = Integer.parseInt(jsonNodeAccountId.asText());

			System.out.println(password);

			if (password.equals(pw)) {
				System.out.println("passwword match");
				addToClientHashMap(cc, accountId);
				return resAccountDetail;
			} else {
				System.out.println("passwword not match");

				return "wrong pw";
			}
		} catch (SQLException | JsonProcessingException e) {
			// TODO Auto-generated catch block
			System.out.println("sql or json processing exception");
			e.printStackTrace();
			return "problem";
		}
	}

	@Override
	public ArrayList<StockOwned> getAccountHoldingsById(int accountId) throws RemoteException {
		System.out.println(" in remote srevant account id " + accountId);
		try {
			ArrayList<StockOwned> stockOwnedHk = hkDb.getOwnedStocks(accountId);
			ArrayList<StockOwned> stockOwnedSg = sgDb.getOwnedStocks(accountId);
			ArrayList<StockOwned> stockOwnedUsa = usaDb.getOwnedStocks(accountId);

			stockOwnedHk.addAll(stockOwnedUsa);
			stockOwnedHk.addAll(stockOwnedSg);

			System.out.println("printing stock owned by account id " + accountId);

			stockOwnedHk.forEach(item -> {
				System.out.println(item);
			});

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		// price and stock is combined, price is avg price.

		return null;
	}

	@Override
	public String sendOrder(int accountId, String market, String order) throws RemoteException {
		System.out.println("sending order");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");

		String QUEUE_NAME = "";

		// Get the clientInt of that accountId first then set on the DB class.

		if (market.equals("US")) {
			QUEUE_NAME = "USMarket";
			ClientInt user = this.retrieveClientIntFromHashMap(accountId);
			hkDb.setCurrentClientInt(user);

		} else if (market.equals("HK")) {
			QUEUE_NAME = "HKMarket";
			ClientInt user = this.retrieveClientIntFromHashMap(accountId);
			hkDb.setCurrentClientInt(user);
		} else {
			QUEUE_NAME = "SGMarket";
			ClientInt user = this.retrieveClientIntFromHashMap(accountId);
			hkDb.setCurrentClientInt(user);
		}

		try (com.rabbitmq.client.Connection connection = factory.newConnection();
				Channel channel = connection.createChannel()) {
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			channel.basicPublish("", QUEUE_NAME, null, order.getBytes());

			System.out.println(" [x] Sent '" + order + "'");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("resource")
	public String retrievePendingOrders(String market, int stockId) {
		StringBuilder sb = new StringBuilder();

		if (market.equals("US")) {
			try {
				ArrayList<MarketPending> arrayListStocks = usaDb.getPendingOrders(stockId);
				if (arrayListStocks == null) {
					return "empty";
				}
				// Serialize list of object to string for returning to client.
				new ObjectOutputStream(new OutputStream() {
					@Override
					public void write(int i) throws IOException {
						sb.append((char) i);
					}
				}).writeObject(arrayListStocks);
				return sb.toString();

			} catch (SQLException | IOException e) {
				e.printStackTrace();
				return "error fetching";
			}
		} else if (market.equals("HK")) {
			try {
				ArrayList<MarketPending> arrayListStocks = hkDb.getPendingOrders(stockId);
				if (arrayListStocks == null) {
					return "empty";
				}
				// Serialize list of object to string for returning to client.
				new ObjectOutputStream(new OutputStream() {
					@Override
					public void write(int i) throws IOException {
						sb.append((char) i);
					}
				}).writeObject(arrayListStocks);
				return sb.toString();

			} catch (SQLException | IOException e) {
				e.printStackTrace();
				return "error fetching";
			}
		} else {
			// SG
			try {
				ArrayList<MarketPending> arrayListStocks = sgDb.getPendingOrders(stockId);
				if (arrayListStocks == null) {
					return "empty";
				}
				// Serialize list of object to string for returning to client.
				new ObjectOutputStream(new OutputStream() {
					@Override
					public void write(int i) throws IOException {
						sb.append((char) i);
					}
				}).writeObject(arrayListStocks);
				return sb.toString();

			} catch (SQLException | IOException e) {
				e.printStackTrace();
				return "error fetching";
			}
		}
	}

	@SuppressWarnings("resource")
	public String retrieveCompletedOrders(String market, int stockId) {
		StringBuilder sb = new StringBuilder();

		if (market.equals("US")) {
			try {
				ArrayList<MarketComplete> arrayListStocks = usaDb.getCompletedOrders(stockId);
				if (arrayListStocks == null) {
					return "empty";
				}
				// Serialize list of object to string for returning to client.
				new ObjectOutputStream(new OutputStream() {
					@Override
					public void write(int i) throws IOException {
						sb.append((char) i);
					}
				}).writeObject(arrayListStocks);
				return sb.toString();

			} catch (SQLException | IOException e) {
				e.printStackTrace();
				return "error fetching";
			}
		} else if (market.equals("HK")) {
			try {
				ArrayList<MarketComplete> arrayListStocks = hkDb.getCompletedOrders(stockId);
				if (arrayListStocks == null) {
					return "empty";
				}
				// Serialize list of object to string for returning to client.
				new ObjectOutputStream(new OutputStream() {
					@Override
					public void write(int i) throws IOException {
						sb.append((char) i);
					}
				}).writeObject(arrayListStocks);
				return sb.toString();

			} catch (SQLException | IOException e) {
				e.printStackTrace();
				return "error fetching";
			}
		} else {
			// SG
			try {
				ArrayList<MarketComplete> arrayListStocks = sgDb.getCompletedOrders(stockId);
				if (arrayListStocks == null) {
					return "empty";
				}
				// Serialize list of object to string for returning to client.
				new ObjectOutputStream(new OutputStream() {
					@Override
					public void write(int i) throws IOException {
						sb.append((char) i);
					}
				}).writeObject(arrayListStocks);
				return sb.toString();

			} catch (SQLException | IOException e) {
				e.printStackTrace();
				return "error fetching";
			}
		}
	}

	@SuppressWarnings("resource")
	@Override
	public String getAllStocksByMarket(String market) throws RemoteException {
		StringBuilder sb = new StringBuilder();

		if (market.equals("US")) {
			try {
				ArrayList<Stock> arrayListStocks = usaDb.getAllStocks();
				if (arrayListStocks == null) {
					return "empty";
				}
				// Serialize list of object to string for returning to client.
				new ObjectOutputStream(new OutputStream() {
					@Override
					public void write(int i) throws IOException {
						sb.append((char) i);
					}
				}).writeObject(arrayListStocks);
				return sb.toString();

			} catch (SQLException | IOException e) {
				e.printStackTrace();
				return "error fetching";
			}
		} else if (market.equals("HK")) {
			try {
				ArrayList<Stock> arrayListStocks = hkDb.getAllStocks();
				if (arrayListStocks == null) {
					return "empty";
				}
				// Serialize list of object to string for returning to client.
				new ObjectOutputStream(new OutputStream() {
					@Override
					public void write(int i) throws IOException {
						sb.append((char) i);
					}
				}).writeObject(arrayListStocks);
				return sb.toString();

			} catch (SQLException | IOException e) {
				e.printStackTrace();
				return "error fetching";
			}
		} else {
			// SG
			try {
				ArrayList<Stock> arrayListStocks = sgDb.getAllStocks();
				if (arrayListStocks == null) {
					return "empty";
				}
				// Serialize list of object to string for returning to client.
				new ObjectOutputStream(new OutputStream() {
					@Override
					public void write(int i) throws IOException {
						sb.append((char) i);
					}
				}).writeObject(arrayListStocks);
				return sb.toString();

			} catch (SQLException | IOException e) {
				e.printStackTrace();
				return "error fetching";
			}
		}
	}

	public void startCache() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					checkForCache();
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void checkForCache() {
		Iterator it = lastSearchTimestamp.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Long> entry = (Entry<String, Long>) it.next();
			if (System.currentTimeMillis() - entry.getValue() > 600000) {
				it.remove();
			} else {
				caching(entry.getKey());
			}
		}
	}

	public String caching(String key) {
		String[] keySplit = key.split(DELIMITER);
		String market = keySplit[0];
		int stockid = Integer.parseInt(keySplit[1]);
		String value = retrieveCompletedOrders(market, stockid);
		jedis.set(key, value);
		return value;
	}

	public String retrieveCache(String market, int stockid) throws RemoteException {
		String key = market + stockid;
		lastSearchTimestamp.put(key, System.currentTimeMillis());
		if (jedis.exists(key))
			return jedis.get(key);
		else {
			// Retrieve from database and cache if not found
			String res = caching(key);
			return res;
		}
	}
}
