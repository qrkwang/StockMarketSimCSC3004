import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import classes.Stock;

public class RemoteServant extends UnicastRemoteObject implements RemoteInterface {
	AccountDetailsDbScript accountDetailsDb = null;
	HKDbScript hkDb = null;
	SGDbScript sgDb = null;
	USADbScript usaDb = null;

	public RemoteServant() throws RemoteException {
		super();
		accountDetailsDb = new AccountDetailsDbScript(); // Start the RabbitMQ Receiver that's in main method
		hkDb = new HKDbScript(); // Start the RabbitMQ Receiver that's in main method
		sgDb = new SGDbScript(); // Start the RabbitMQ Receiver that's in main method
		usaDb = new USADbScript(); // Start the RabbitMQ Receiver that's in main method

		try {
			accountDetailsDb.startWaitForMsg();
		} catch (SQLException e) {
			System.out.println("error start wait for msg");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.format("Creating server object\n"); // Print to client that server object is being created once
														// constructor called.
		
		  List<String> serverNo = null;
		  
		boolean result;
		try {
			//------------ for testing connection---------------------------------------------------
			result = checkConnection("192.168.210.128", "root",  "root" , "AccountDetailsServer");
		//	System.out.println("checking for db result connection" + " " + result);
			 int generation = 0; // increase everytime it election a new leader 
		// ------------ still working/testing in progress -------------------------------------
			 
			if(leaseAlive == false && serverNo == null) {	// running for first time 
			    serverNo = electionLeader(listServer, null , generation); 
			    System.out.println("result of calling method    " +  serverNo);		
				//setLease(serverNo.get(0), "root", "root"); // set lease once selected leader 
				System.out.println("Set up server for first time");
				
			}else if(leaseAlive == false && serverNo != null) { // for second time onward
				// the leader need to be reelection
				String logMapResult = logMap.entrySet().toArray()[logMap.size() -1].toString(); // trying to get last value 
				String[] resultgenserver = getLogResult(logMap);
			    serverNo = electionLeader(listServer, resultgenserver[0] , Integer.parseInt(resultgenserver[1])); // access last election leader from map log
				//setLease(serverNo.get(1), "root", "root"); // restart the lease again 
			}
			
			if(leaseAlive == true && serverNo != null) { // call the script once the node is ready to be called 
				accountDetailsDb.setConnString(serverNo.get(0) ,"AccountDetailsServer");
				System.out.println("running the accountDetailsDb leader");
				for(int no = 1; no < serverNo.size(); no++) { // start from 1 because 0 will always be the better server 					
					if(!serverNo.get(no).equals(serverNo.get(0))) {
					 accountDetailsDb.setConnString(serverNo.get(no) ,"AccountDetailsServer"); // call the follower to update database
					 System.out.println("running the accountDetailsDb follower");
					}
					
				}
			}
			 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public String testMethod(String s) throws RemoteException {
		System.out.println(s);
		return "printed";
	}

	@Override
	public String getAccountDetailsByUsernameAndPW(String username, String pw) throws RemoteException {
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
			String password = jsonNodePW.asText();
			System.out.println(password);

			if (password.equals(pw)) {
				System.out.println("passwword match");
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
	public ArrayList getAccountHoldingsById(int accountId) throws RemoteException {
		// TODO Auto-generated method stub
		// price and stock is combined, price is avg price.

		return null;
	}

	@Override
	public String sendOrder(int accountId, String order) throws RemoteException {
		System.out.println("sending order");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");

		String QUEUE_NAME = "";
		String[] splitArray = order.split(",");
		String market = splitArray[0];
		if (market.equals("US")) {
			QUEUE_NAME = "US";

		} else if (market.equals("HK")) {
			QUEUE_NAME = "HK";

		} else {
			QUEUE_NAME = "SG";

		}

		try (com.rabbitmq.client.Connection connection = factory.newConnection();
				Channel channel = connection.createChannel()) {
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			String message = "Hello World!";
			channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

			System.out.println(" [x] Sent '" + message + "'");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

    HashMap<String, Integer> logMap = new HashMap<>(); // for log (will be server name and generation number)
 List<String> listServer = new ArrayList<>(Arrays.asList( "192.168.210.128" , "192.168.210.129"));
 //"127.0.0.1",
  boolean leaseAlive = false;
	
	public List<String> electionLeader(List<String> listServer, String currServer , int generation) { 
		String selectedserver = null;
        List<String> serverlist =  new ArrayList<String>(listServer);
        HashMap<String, Long> rankListServer = new HashMap<>();
        List<String> ranked = new ArrayList<String>();

        try {
        	if(!logMap.isEmpty()) {
				int index = serverlist.indexOf(currServer);// remove the server that unable to run temp
				serverlist.remove(index);
			}
			for (int i = 0; i < serverlist.size(); i++) {
				// rank them by the faster server speed 
				long startTime = System.nanoTime();
				boolean connectionResult = checkConnection(serverlist.get(i) , "root", "root", "AccountDetailsServer");
				long endTime = System.nanoTime();
				long total = endTime - startTime;
				System.out.println("total tine for " + total + " server running" +  serverlist.get(i) );
				System.out.println("server result connection " + connectionResult +  " what server is running" +  serverlist.get(i));
				if(connectionResult == true) {
				rankListServer.put(serverlist.get(i), total); // adding result that pass the connection
				
				}
				
			}
			// sorting of map to get the best time result  
			Map<String, Long> sortedServerList = // smaller to the bigger 
					rankListServer.entrySet().stream()
				    .sorted(Entry.comparingByValue())
				    .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
				                              (e1, e2) -> e1, LinkedHashMap::new));

			System.out.println(Arrays.asList(sortedServerList));
			for(String key: sortedServerList.keySet()) {
				ranked.add(key);
				System.out.println("printing key to enter " + key );
			}
			
			generation = generation + 1; // increase count every new election with leader
			if(!ranked.isEmpty()) {
			logMap.put(ranked.get(0), generation); // add the generation and log map	
			selectedserver = ranked.get(0); // always get 0 because to get faster result 
			setLease(selectedserver, "root", "root"); // once elected leader start the lease time 
			System.out.println("Selected Server as a leader is " + selectedserver  +  "generation no" + generation);
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ranked; // return the leader 
	}
	
	public boolean restartServer() {
		return leaseAlive;
		// restart the server 
		// will know which one leader 
	}

	// set a lease to run in backgroup for the leader
	public void setLease(String ipname, String username, String password) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			// private final double currentCount = 0.3; // temp set 300 ms
			@Override
			public void run() {
				boolean checkHeartbeatResult = false;
				try {
					checkHeartbeatResult = checkConnection(ipname, username, password, "AccountDetailsServer");
					System.out.println("task have expired , ready to check for renew" );
					if (checkHeartbeatResult == false) { // check if it ok to reset the lease , if heartbeat fail no
						timer.cancel(); // cancel all the schedule task that maybe happending
						leaseAlive = false; 
						System.out.println("time out unable to lease due to error" );
						String[] serverDetailsLog = getLogResult(logMap);
						electionLeader(listServer,  ipname , Integer.parseInt(serverDetailsLog[1])); //call for election again to get new leader 
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					cancel(); // if there is exception also cancel the lease
					leaseAlive = false;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		leaseAlive = true;
		System.out.println("lease have been renew" );
		timer.schedule(task, 0, 300); // to trigger to reschedule the lease will repeat itself till the condition is met								
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
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return result;
	}

	public String[] getLogResult(HashMap<String, Integer> log) {
		String logMapResult = log.entrySet().toArray()[log.size() -1].toString(); // trying to get last value 
		String[] resultgenserver = logMapResult.split("="); // get back the last election leader server & generation number 
		return resultgenserver;
	}

	@Override
	public ArrayList retrieveOrders(String market, String tickerSymbol) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrievePrice(String market, String tickerSymbol) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
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
}
