import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import com.mysql.jdbc.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import classes.MarketComplete;
import classes.MarketPending;
import classes.Stock;

//Put db connection and queries for Hk market db here.
//Put rabbitMQ receiver here to receive from their own market topic. Will receive from servant.java

public class SGDbScript {
	private final static String QUEUE_NAME = "SGMarket";
	public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";

	private static String CONN_STRING = "jdbc:mysql://localhost:3306/sgstockmarket"; // jdbc:mysql://ip:3306/DBNAME

	public static void setConnString(String ipandPort, String dbName) {
		CONN_STRING = "jdbc:mysql//" + ipandPort + dbName;
	}

	public void startWaitForMsg() {
		System.out.println("starting wait for msg function");

		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			com.rabbitmq.client.Connection connection;
			connection = factory.newConnection();

			Channel channel = connection.createChannel();

			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			System.out.println(" [*] HKDbScript waiting for msg.");

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				String message = new String(delivery.getBody(), "UTF-8");
				System.out.println(" [x] Received '" + message + "'");
				try {
					receiveOrder(message);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void receiveOrder(String message) throws SQLException {
		// TODO Auto-generated method stub

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
			Stock stockItem = new Stock(rs.getInt("stockId"), rs.getString("CompanyName"), rs.getString("TickerSymbol"),
					rs.getFloat("CurrentValue"), rs.getBoolean("Status"), rs.getString("Timezone"), localDateTime);

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

			int SellerId = 0;
			int BuyerId = 0;

			// If ID is null, return as -1
			if (rs.getInt("SellerId") == 0) {
				SellerId = -1;
			} else {
				BuyerId = rs.getInt("SellerId");
			}
			if (rs.getInt("BuyerId") == 0) {
				BuyerId = -1;
			} else {
				BuyerId = rs.getInt("BuyerId");
			}

			java.sql.Timestamp dbSqlTimestamp = rs.getTimestamp("CreatedDate");
			LocalDateTime localDateTime = dbSqlTimestamp.toLocalDateTime();
			MarketPending marketOrder = new MarketPending(rs.getInt("MarketPendingId"), rs.getInt("StockId"), SellerId,
					BuyerId, rs.getInt("Quantity"), rs.getFloat("Price"), localDateTime);

			marketOrder.setCreatedDate(localDateTime);

			System.out.println("Market Pending: ");
			System.out.println(marketOrder.toString());
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
			java.sql.Timestamp dbSqlTimestamp = rs.getTimestamp("TransactionDate");
			LocalDateTime localDateTime = dbSqlTimestamp.toLocalDateTime();

			MarketComplete marketOrder = new MarketComplete(rs.getInt("MarketCompletedId"), rs.getInt("StockId"),
					rs.getInt("SellerId"), rs.getInt("BuyerId"), rs.getInt("Quantity"), rs.getFloat("Price"),
					localDateTime);

			System.out.println("Market Completed: ");
			System.out.println(marketOrder.toString());
			arrayListOrders.add(marketOrder);
			count++;
		}

		return arrayListOrders;
	}

}
