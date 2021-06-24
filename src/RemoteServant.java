import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

public class RemoteServant extends UnicastRemoteObject implements RemoteInterface {
	AccountDetailsDbScript accountDetailsDb = null;
	HKDbScript hkDb = null;
	SGDbScript sgDb = null;
	USADbScript usaDb = null;

	public RemoteServant() throws RemoteException {
		super();
		accountDetailsDb = new AccountDetailsDbScript(); // Start the RabbitMQ Receiver that's in main method
		try {
			accountDetailsDb.startWaitForMsg();
		} catch (SQLException e) {
			System.out.println("error start wait for msg");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hkDb = new HKDbScript(); // Start the RabbitMQ Receiver that's in main method
		sgDb = new SGDbScript(); // Start the RabbitMQ Receiver that's in main method
		usaDb = new USADbScript(); // Start the RabbitMQ Receiver that's in main method

		System.out.format("Creating server object\n"); // Print to client that server object is being created once
														// constructor called.
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

	// add method to trigger the two different server
	// method add lease or check for existing lease (lease should be around 10 - 30
	// sec normally + the process run time)
	// possible timing 150ms-300ms.
	// method heartbeat (check if the server alive)
	// method try to restart the server
	// method check for the new leader / add log
	// how to make the lease timing keep on running and nofity the method when it is
	// ended

	/*
	 * flow 1. First election select the leader and enter into the log 2. The leader
	 * will have their own lease and will renew every few second ? 3. if the leader
	 * lease timeout or when heatbeat fail . it will hold a new election to select a
	 * new leader 4. once leader is selected , enter a new log with the generation
	 * number (maybe hashmap) and have a lease 5. restart the fail server and it
	 * will check who is the leader at log 6. The leader will update the follower
	 * all the latest data
	 */

	HashMap<String, Integer> logMap = new HashMap<>(); // for log (will be server name and generation number)
//	 private int leasetime = 20; // default lease time for the leader 
	String startAccountDB = "accountDB1"; // set as default first // may need to get latency and use it to assign unique
											// id?
	String servername[] = { "192.168.210.1", "192.168.210.128 ", "192.168.210.129" };
	boolean leaseAlive = false;
	// 192.168.210.129 server 3
	// 192.168.210.128 server 2
	// 192.168.210.1 server 1

	// set the first time when running // use the faster
	public String setServer() {
		long prevTotal = 0;
		int selectedserver = 1;
		try {

			for (int i = 0; i < servername.length; i++) {
				long startTime = System.nanoTime();
				checkConnection(servername[i], "root", "password", "AccountDetailsServer");
				long endTime = System.nanoTime();
				long total = endTime - startTime;
				if (i == 0) {
					prevTotal = total; // first time running
				} else if (i > 0) {
					if (total < prevTotal) { // compare the timing
						selectedserver = i;
						prevTotal = total; // if found the faster , do not need to care previous value just need
											// continue to compare with next
					}
				}
			}
			// get the selectedserver
			// add the generation and log map
			//

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ""; // if the set up properly

	}

	// set a lease to run in backgroup for the leader
	// set timer
	public void setLease(String ipname, String username, String password) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			// private final double currentCount = 0.3; // temp set 300 ms
			@Override
			public void run() {
				boolean checkHeartbeatResult = false;
				try {
					checkHeartbeatResult = checkConnection(ipname, username, password, "AccountDetailsServer");
					if (checkHeartbeatResult == false) { // check if it ok to reset the lease , if heartbeat fail no
															// reset of lease
						timer.cancel(); // cancel all the schedule task that maybe happending
						leaseAlive = false;
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
		timer.schedule(task, 0, 300); // to trigger to reschedule the lease will repeat itself till the condition is
										// met
	}

	// act like heartbeat to check if connection exist or not
	public static boolean checkConnection(String ipname, String username, String password, String dbname)
			throws SQLException {
		Connection con = null;
		boolean result = false;
		String CONN_STRING = "jdbc:mysql://" + ipname + "/" + dbname;
		// String CONN_STRING = "jdbc:mysql://localhost:3306/accountdetailsserver";
		// jdbc:mysql://localhost:3306/accountdetailsserver
		// jdbc:mysql//" + ipandPort + dbName;
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

	// ------ testing -----------------------
	// boolean result = checkConnection("192.168.210.128", "root", "password" ,
	// "AccountDetailsServer");
	// System.out.println("checking for db result connection" + " " + result);

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

	@Override
	public String getAllStocksByMarket(String market) throws RemoteException {
		if (market.equals("US")) {

		} else if (market.equals("HK")) {

		} else {
			// SG

		}
		return null;
	}

}
