import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

//Put db connection and queries for AccountDetails db here.

public class AccountDetailsDbScript {

	public static void getAccountDetails(int accountDetailsId) throws SQLException {

		Connection con = DriverManager.getConnection("jdbc:default:connection");

		String query = "{CALL get_account_Details(?)}"; // Query of calling stored procedure "Get account Details" with
														// input of ?
		CallableStatement stmt = con.prepareCall(query); // prepare to call

		stmt.setInt(1, accountDetailsId); // Set the parameter

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			System.out.println(String.format("%s - %s", rs.getString("first_name") + " " + rs.getString("last_name"), //Getting attribute of returned rows and printing it.
					rs.getString("skill")));
		}
		con.close();
		//When really do the method, will have a return value to server for it to use.
	}
	
	
	
	
}
