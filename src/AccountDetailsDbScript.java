
import java.io.IOException;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mysql.jdbc.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import classes.AccountDetails;

public class AccountDetailsDbScript {
	private final static String QUEUE_NAME = "hello";
	public final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private final String USERNAME = "root";
	private final String PASSWORD = "root";

	// Change this string according to leader election
	private String CONN_STRING = "jdbc:mysql://localhost:3306/accountdetailsserver"; // jdbc:mysql://ip:3306/DBNAME

	public void setConnString(String ipandPort, String dbName) {
		CONN_STRING = "jdbc:mysql://" + ipandPort + "/" + dbName;
	}

	public float getAccountBalanceById(int accountId) throws SQLException {
		Connection con = null;
		float accountBalance = 0;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.CONN_STRING, this.USERNAME, this.PASSWORD);
			String query = "{CALL getAccountHoldingsById(?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				accountBalance = rs.getFloat("availableCash");

			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return accountBalance;

	}

	public String getAccountDetails(String userName, String pw) throws SQLException {
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
			System.out.println("Connected to DB");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Account Details JSON is\n" + stringAccountDetails);
			return stringAccountDetails.toString();
		}
		con.close();

		return "not found";
	}

	public void startWaitForMsg() throws SQLException {
		System.out.println("starting wait for msg function");

		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			com.rabbitmq.client.Connection connection;
			connection = factory.newConnection();

			Channel channel = connection.createChannel();

			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			System.out.println(" [*] AccountDetailsDbScript waiting for msg.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
