import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;

import classes.MarketComplete;
import classes.MarketPending;
import classes.Stock;

//Put db connection and queries for Hk market db here.
//Put rabbitMQ receiver here to receive from their own market topic. Will receive from servant.java

public class HKDbScript {
	private final static String QUEUE_NAME = "HKMarket";
	public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";

	private static String CONN_STRING = "jdbc:mysql://localhost:3306/hkstockmarket"; // jdbc:mysql://ip:3306/DBNAME

	public static void setConnString(String ipandPort, String dbName) {
		CONN_STRING = "jdbc:mysql//" + ipandPort + dbName;
	}

	public ArrayList<Stock> getAllStocks() throws SQLException {
		ArrayList<Stock> arrayListStocks = null;
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
			System.out.println("Connected to DB");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String query = "{CALL getAllStocks}";
		CallableStatement stmt = con.prepareCall(query); // prepare to call

		ResultSet rs = stmt.executeQuery();

		System.out.println("before while loop");

		int count = 0;
		while (rs.next()) {
			if (count == 0) {
				arrayListStocks = new ArrayList<Stock>(); // initialize arraylist if results to be found
			}
			java.sql.Timestamp dbSqlTimestamp = rs.getTimestamp("CreatedDate");
			LocalDateTime localDateTime = dbSqlTimestamp.toLocalDateTime();
			Stock stockItem = new Stock(rs.getInt("stockId"),rs.getString("CompanyName"),rs.getString("TickerSymbol"),rs.getFloat("CurrentValue"),rs.getBoolean("Status"),rs.getString("Timezone"),localDateTime);

//			stockItem.setStockId(rs.getInt("stockId"));
//			stockItem.setCompanyName(rs.getString("CompanyName"));
//			stockItem.setTickerSymbol(rs.getString("TickerSymbol"));
//			stockItem.setCurrentValue(rs.getFloat("CurrentValue"));
//			stockItem.setStatus(rs.getBoolean("Status"));
//			stockItem.setTimezone(rs.getString("Timezone"));
//			java.sql.Timestamp dbSqlTimestamp = rs.getTimestamp("CreatedDate");
//			LocalDateTime localDateTime = dbSqlTimestamp.toLocalDateTime();
//			stockItem.setCreatedDate(localDateTime);
			System.out.println(stockItem.toString());
			arrayListStocks.add(stockItem);
			count++;
		}

		return arrayListStocks;

	}

	public ArrayList<MarketPending> getPendingOrders(int stockId) throws SQLException {
		ArrayList<MarketPending> arrayListOrders = null;
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
			System.out.println("Connected to DB");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String query = "{CALL getOrdersByStockId(?)}";
		CallableStatement stmt = con.prepareCall(query); // prepare to call
		stmt.setInt(1, stockId); // Set the parameter

		ResultSet rs = stmt.executeQuery();

		System.out.println("before while loop");

		int count = 0;
		while (rs.next()) {
			if (count == 0) {
				arrayListOrders = new ArrayList<MarketPending>(); // initialize arraylist if results to be found
			}
			MarketPending marketOrder = new MarketPending();

			marketOrder.setMarketPendingId(rs.getInt("MarketPendingId"));
			marketOrder.setStockId(rs.getInt("StockId"));
			marketOrder.setSellerId(rs.getInt("SellerId"));
			marketOrder.setBuyerId(rs.getInt("BuyerId"));
			marketOrder.setQuantity(rs.getInt("Quantity"));
			marketOrder.setPrice(rs.getFloat("Price"));

			java.sql.Timestamp dbSqlTimestamp = rs.getTimestamp("CreatedDate");
			LocalDateTime localDateTime = dbSqlTimestamp.toLocalDateTime();
			marketOrder.setCreatedDate(localDateTime);

			arrayListOrders.add(marketOrder);
			count++;
		}

		return arrayListOrders;
	}

	public ArrayList<MarketComplete> getCompletedOrders(int stockId) throws SQLException {
		ArrayList<MarketComplete> arrayListOrders = null;
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
			System.out.println("Connected to DB");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String query = "{CALL getOrdersCompletedByStockId(?)}";
		CallableStatement stmt = con.prepareCall(query); // prepare to call
		stmt.setInt(1, stockId); // Set the parameter

		ResultSet rs = stmt.executeQuery();

		System.out.println("before while loop");

		int count = 0;
		while (rs.next()) {
			if (count == 0) {
				arrayListOrders = new ArrayList<MarketComplete>(); // initialize arraylist if results to be found
			}
			MarketComplete marketOrder = new MarketComplete();

			marketOrder.setMarketCompleteId(rs.getInt("MarketCompleteId"));
			marketOrder.setStockId(rs.getInt("StockId"));
			marketOrder.setSellerId(rs.getInt("SellerId"));
			marketOrder.setBuyerId(rs.getInt("BuyerId"));
			marketOrder.setQuantity(rs.getInt("Quantity"));
			marketOrder.setPrice(rs.getFloat("Price"));

			java.sql.Timestamp dbSqlTimestamp = rs.getTimestamp("TransactionDate");
			LocalDateTime localDateTime = dbSqlTimestamp.toLocalDateTime();
			marketOrder.setTransactionDate(localDateTime);

			arrayListOrders.add(marketOrder);
			count++;
		}

		return arrayListOrders;
	}

}
