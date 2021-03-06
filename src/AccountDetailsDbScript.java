
import java.io.IOException;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mysql.jdbc.Connection;

import classes.AccountDetails;

// Handle database for account
public class AccountDetailsDbScript {
	private final static String QUEUE_NAME = "hello";
	public final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private String username;
	private String password;
	private String conn_string;

	// Constructor
	public AccountDetailsDbScript(String ip, String dbname, String u, String p) {
		this.conn_string = "jdbc:mysql://" + ip + "/" + dbname;
		this.username = u;
		this.password = p;

	}

	public void setConnString(String ipandPort, String dbName) {
		conn_string = "jdbc:mysql://" + ipandPort + "/" + dbName;
	}

	// Retrieve account balance
	public float getAccountBalanceById(int accountId) throws SQLException {
		Connection con = null;
		String currConn = this.conn_string;

		System.out.println("account ID is " + accountId);

		float accountBalance = 0;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(currConn, this.username, this.password);
			String query = "{CALL getAccountHoldingsById(?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, accountId); // Set the parameter
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				accountBalance = rs.getFloat("availableCash");
				System.out.println("account bal is " + accountBalance);

			}
			con.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return (Float) null;
		}
		System.out.println("account bal is " + accountBalance);

		return accountBalance;

	}

	// Retrieve account detail based on username and password
	// Used to check for log in
	public String getAccountDetails(String userName, String pw) throws SQLException {
		Connection con = null;
		String currConn = this.conn_string;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(currConn, username, password);
			System.out.println("Connected to DB");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		String query = "{CALL getAccountDetailsByUsernameAndPw(?,?)}"; // Query of calling stored procedure "Get account
		// Details" with
		CallableStatement stmt = con.prepareCall(query); // prepare to call

		stmt.setString(1, userName); // Set the parameter
		stmt.setString(2, pw); // Set the parameter

		ResultSet rs = stmt.executeQuery();

		System.out.println("before while loop");
		while (rs.next()) {
			System.out.println("there's result");

			AccountDetails accountDetail = new AccountDetails(rs.getInt("accountId"), rs.getString("userName"),
					rs.getString("email"), rs.getFloat("totalAccountValue"), rs.getFloat("totalSecurityValue"),
					rs.getFloat("availableCash"));

			System.out.println(accountDetail);

			// Convert object to string to return as string:
			ObjectMapper objectMapper = new ObjectMapper();

			// configure Object mapper for pretty print
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

			// writing to console, can write to any output stream such as file
			StringWriter stringAccountDetails = new StringWriter();
			try {
				objectMapper.writeValue(stringAccountDetails, accountDetail);
			} catch (IOException e) {
				System.out.println("io exception!!");
				e.printStackTrace();
			}
			System.out.println("Account Details JSON is\n" + stringAccountDetails);
			return stringAccountDetails.toString();
		}
		con.close();

		return "not found";
	}

	// Retrieve account details based on account id
	public String getAccountDetailsById(int id) throws SQLException {
		Connection con = null;
		String currConn = this.conn_string;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(currConn, username, password);
			System.out.println("Connected to DB");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		String query = "{CALL getAccountDetailsById(?)}"; // Query of calling stored procedure "Get account
		// Details" with
		CallableStatement stmt = con.prepareCall(query); // prepare to call

		stmt.setInt(1, id); // Set the parameter

		ResultSet rs = stmt.executeQuery();

		System.out.println("before while loop");
		while (rs.next()) {
			System.out.println("there's result");

			AccountDetails accountDetail = new AccountDetails(rs.getInt("accountId"), rs.getString("userName"),
					rs.getString("email"), rs.getFloat("totalAccountValue"), rs.getFloat("totalSecurityValue"),
					rs.getFloat("availableCash"));

			System.out.println(accountDetail);

			// Convert object to string to return as string:
			ObjectMapper objectMapper = new ObjectMapper();

			// configure Object mapper for pretty print
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

			// writing to console, can write to any output stream such as file
			StringWriter stringAccountDetails = new StringWriter();
			try {
				objectMapper.writeValue(stringAccountDetails, accountDetail);
			} catch (IOException e) {
				System.out.println("io exception!!");
				e.printStackTrace();
			}
			System.out.println("Account Details JSON is\n" + stringAccountDetails);
			return stringAccountDetails.toString();
		}
		con.close();

		return "not found";
	}

	// Update purchases by the client
	public void updatePurchaseInAccount(int buyerId, float totalPaid) throws SQLException {
		Connection con = null;
		String currConn = this.conn_string;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(currConn, this.username, this.password);

			String query = "{CALL updatePurchaseInAccount(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, buyerId);
			stmt.setFloat(2, totalPaid);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Update sales by the client
	public void updateSaleInAccount(int sellerId, float value) throws SQLException {
		Connection con = null;
		String currConn = this.conn_string;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(currConn, this.username, this.password);

			String query = "{CALL updateSaleInAccount(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, sellerId);
			stmt.setFloat(2, value);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
