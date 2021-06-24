import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.mysql.jdbc.Connection;

/*
 * Remote server to call servant and bind to registry
 */

// add method to trigger the two different server 
//method add lease or check for existing lease (lease should be around 10 - 30 sec normally + the process run time)
//possible timing 150ms-300ms.
// method heartbeat (check if the server alive)
// method try to restart the server
// method check for the new leader / add log 
// how to make the lease timing keep on running and nofity the method when it is ended 

/*
 * flow 
1. First election select the leader and enter into the log 
2. The leader will have their own lease and will renew every few second ?
3. if the leader lease timeout or when heatbeat fail . it will hold a new election to select a new leader
4. once leader is selected , enter a new log with the generation number (maybe hashmap) and have a lease 
5. restart the fail server and it will check who is the leader at log 
6. The leader will update the follower all the latest data 
 */
public class RemoteServer {
	
	 HashMap<String, Integer> logMap = new HashMap<>(); // for log (will be server name and generation number)
//	 private int leasetime = 20; // default lease time for the leader 
	 String startAccountDB = "accountDB1"; // set as default first // may need to get latency and use it to assign unique id?
	 String servername[]={"192.168.210.1","192.168.210.128 ","192.168.210.129"};
	 boolean leaseAlive = false;
	 	//192.168.210.129 server 3
			//192.168.210.128 server 2
			// 192.168.210.1 server 1
	
	 // set the first time when running // use the faster 
	 public String setServer() {
		  long prevTotal = 0;
		  int selectedserver = 1;
		try {
			
			for(int i=0; i < servername.length; i++ ) {
				long startTime = System.nanoTime();
				checkConnection(servername[i], "root",  "password" , "AccountDetailsServer");
				long endTime = System.nanoTime();
				long total = endTime - startTime;
				if(i == 0) {
					prevTotal = total; // first time running
				}else if(i > 0){ 
					if(total < prevTotal) { // compare the timing 
						selectedserver = i;
						prevTotal = total; // if found the faster , do not need to care previous value just need continue to compare with next
					}
				}
			}
			// get the selectedserver 
			//add the generation and log map 
			// 

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return ""; // if the set up properly
		 
	 }
	 
	 // set a lease to run in backgroup for the leader 
	 // set timer 
	 public void setLease(String ipname,  String username, String password) {
	        Timer timer = new Timer();
	        TimerTask task = new TimerTask() {
		 //    private final double currentCount = 0.3; // temp set 300 ms 
		        @Override
		        public void run() { 
		        	boolean checkHeartbeatResult =  false;
					try {
						checkHeartbeatResult = checkConnection(ipname,  username ,  password , "AccountDetailsServer");
						  if(checkHeartbeatResult == false) { // check if it ok to reset the lease , if heartbeat fail no reset of lease 
							  timer.cancel(); // cancel all the schedule task that maybe happending 
							  leaseAlive = false;
		         }
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						cancel(); // if there is exception also cancel the lease 
						leaseAlive = false;
					}
		        }
		    };
		    leaseAlive = true;
	        timer.schedule(task,0, 300); // to trigger to reschedule the lease will repeat itself till the condition is met 		 
	 }
	 

	 // act like heartbeat to check if connection exist or not 
		public boolean checkConnection(String ipname, String username, String password , String dbname) throws SQLException {
			Connection con = null;
			boolean result = false;
			String CONN_STRING = "jdbc:mysql://" + ipname + "/" + dbname;
			try {
				Class.forName("com.mysql.jdbc.Driver");
				con = (Connection) DriverManager.getConnection(CONN_STRING, username, password);
	            if (con != null) {
	            	 result = true; // able to connect to db
	            }
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			con.close();
			return result;
		}

	 
	public static void main(String[] args) {
		try {
			int port = 1099;

			System.out.format("Created server, now advertising it\n");

			// Naming lookup
			RemoteInterface remoteObj = new RemoteServant();
			LocateRegistry.createRegistry(port);
			Naming.rebind("rmi://localhost:" + port + "/RemoteServer", remoteObj);
			System.out.format("Advertising completed\n");

			
			

		} catch (Exception e) {
			System.out.format("export exception - %s\n", e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

}
