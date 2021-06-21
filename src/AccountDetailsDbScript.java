import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;

//Put db connection and queries for AccountDetails db here.

public class AccountDetailsDbScript {
	public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "password";
	private static String CONN_STRING = "jdbc:mysql://localhost:3306/test"; // jdbc:mysql://ip:3306/DBNAME

	public static void setConnString(String ipandPort, String dbName) {
		CONN_STRING = "jdbc:mysql//" + ipandPort + dbName;
	}

	public static void getAccountDetails(int accountDetailsId) throws SQLException {
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
			System.out.println("Connected to DB");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String query = "{CALL get_account_Details(?)}"; // Query of calling stored procedure "Get account Details" with
														// input of ?
		CallableStatement stmt = con.prepareCall(query); // prepare to call

		stmt.setInt(1, accountDetailsId); // Set the parameter

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			System.out.println(String.format("%s - %s", rs.getString("first_name") + " " + rs.getString("last_name"), // Getting
																														// attribute
																														// of
																														// returned
																														// rows
																														// and
																														// printing
																														// it.
					rs.getString("skill")));
		}
		con.close();
		// When really do the method, will have a return value to server for it to use.
	}

}
