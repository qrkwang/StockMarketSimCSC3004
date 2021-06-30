import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import classes.MarketComplete;
import classes.MarketPending;
import classes.Stock;
import classes.StockOwned;

//Put db connection and queries for Hk market db here.
//Put rabbitMQ receiver here to receive from their own market topic. Will receive from servant.java

public class HKDbScript {
	private boolean isOnline = true; // will be true unless algo detected offline in RemoteServant.java
	private ClientInt currentClientInt;
	private final static String QUEUE_NAME = "HKMarket";
	public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";

	private static String CONN_STRING = "jdbc:mysql://localhost:3306/hkstockmarket"; // jdbc:mysql://ip:3306/DBNAME

	public static void setConnString(String ipandPort, String dbName) {
		CONN_STRING = "jdbc:mysql//" + ipandPort + dbName;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public ClientInt getCurrentClientInt() {
		return currentClientInt;
	}

	public void setCurrentClientInt(ClientInt currentClientInt) {
		this.currentClientInt = currentClientInt;
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
				} catch (Exception e) {

					// Call back to client
					this.getCurrentClientInt().printToClient("Transaction failed while processing order.");
					System.out.println("start wait for msg exception");
					e.printStackTrace();
				}
			};
			channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
			});
		} catch (Exception e) {
			System.out.println("server cannot connect");
			e.printStackTrace();
		}

	}

	public ArrayList<MarketPending> retrieveOrdersToMatch(boolean isBuy, int stockId, float orderPrice)
			throws SQLException {
		Connection con = null;
		ArrayList<MarketPending> retrievedOrders = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
			System.out.println("Connected to DB");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (isBuy) {
			String query = "{CALL getPendingSellOrdersRequiredForNewInsertion(?, ?)}";
			// stock id and input price
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, stockId); // Set the parameter
			stmt.setFloat(2, orderPrice); // Set the parameter

			ResultSet rs = stmt.executeQuery();

			System.out.println("before while loop");

			int count = 0;
			while (rs.next()) {
				if (count == 0) {
					retrievedOrders = new ArrayList<MarketPending>(); // initialize arraylist if results to be found

				}
				// see if can split into 2 and then return the corresponding arraylist for it.

				java.sql.Timestamp dbSqlTimestamp = rs.getTimestamp("CreatedDate");
				LocalDateTime localDateTime = dbSqlTimestamp.toLocalDateTime();
				MarketPending marketOrder = new MarketPending(rs.getInt("MarketPendingId"), rs.getInt("StockId"),
						rs.getInt("SellerId"), rs.getInt("BuyerId"), rs.getInt("Quantity"), rs.getFloat("Price"),
						localDateTime);
				retrievedOrders.add(marketOrder);
				count++;

			}
		} else {
			String query = "{CALL getPendingBuyOrdersRequiredForNewInsertion(?, ?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, stockId); // Set the parameter
			stmt.setFloat(2, orderPrice); // Set the parameter

			ResultSet rs = stmt.executeQuery();

			System.out.println("before while loop");

			int count = 0;
			while (rs.next()) {
				if (count == 0) {
					retrievedOrders = new ArrayList<MarketPending>(); // initialize arraylist if results to be found

				}

				java.sql.Timestamp dbSqlTimestamp = rs.getTimestamp("CreatedDate");
				LocalDateTime localDateTime = dbSqlTimestamp.toLocalDateTime();
				MarketPending marketOrder = new MarketPending(rs.getInt("MarketPendingId"), rs.getInt("StockId"),
						rs.getInt("SellerId"), rs.getInt("BuyerId"), rs.getInt("Quantity"), rs.getFloat("Price"),
						localDateTime);
				retrievedOrders.add(marketOrder);
				count++;

			}
		}

		return retrievedOrders;

	}

	public String receiveOrder(String message) throws SQLException {
		String[] splitArray = message.split(",");
		int stockId = Integer.parseInt(splitArray[0]);
		int sellerId = Integer.parseInt(splitArray[1]);
		int buyerId = Integer.parseInt(splitArray[2]);
		int qty = Integer.parseInt(splitArray[3]);
		float price = Float.parseFloat(splitArray[4]);

		if (sellerId == -1 && buyerId != -1) {

			// its a buy order
			ArrayList<MarketPending> fetchedOrders = retrieveOrdersToMatch(true, stockId, price);
			if (fetchedOrders == null) {
				// No orders to match with, he want to buy but there's no one selling under his
				// buy price or equals to

				// If return no entry, create a marketpending order and insert.

			}
			fetchedOrders.forEach(item -> {
				System.out.println(item);
			});

		} else {
			// its a sell order
			ArrayList<MarketPending> fetchedOrders = retrieveOrdersToMatch(false, stockId, price);
			if (fetchedOrders == null) {
				// No orders to match with, he want to sell but there's no one buying above his
				// sell price or equals to.

				// If return no entry, create a marketpending order and insert.

			}
			fetchedOrders.forEach(item -> {
				System.out.println(item);
			});

		}

		// IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price
		// Double, In transactionDate DATETIME

		// Do market algo first, pull entries first.

		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
			System.out.println("Connected to DB");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "offline";

		}

		// later

		String query = "{CALL InsertToMarketPending(?,?,?,?,?,?)}";
		CallableStatement stmt = con.prepareCall(query); // prepare to call
		stmt.setInt(1, stockId);
		stmt.setInt(2, sellerId);
		stmt.setInt(3, buyerId);
		stmt.setInt(4, qty);
		stmt.setFloat(5, price);
		stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			System.out.println(rs.getString(1));

		}
		return "success";
	}

	public ArrayList<StockOwned> getOwnedStocks(int accountId) throws SQLException {
		ArrayList<StockOwned> arrayListOwned = null;
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
			System.out.println("Connected to DB");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String query = "{CALL getTotalHoldingsByAccountId(?)}";
		CallableStatement stmt = con.prepareCall(query); // prepare to call

		stmt.setInt(1, accountId); // Set the parameter
		ResultSet rs = stmt.executeQuery();

		System.out.println("before while loop");

		int count = 0;
		while (rs.next()) {
			if (count == 0) {
				arrayListOwned = new ArrayList<StockOwned>(); // initialize arraylist if results to be found

			}

			StockOwned stockOwnedItem = new StockOwned(rs.getInt("StockId"), rs.getString("CompanyName"),
					rs.getString("TickerSymbol"), rs.getInt(4), rs.getInt(5));
			System.out.println(stockOwnedItem.toString());
			arrayListOwned.add(stockOwnedItem);
			count++;

		}
		return arrayListOwned;

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
			Stock stockItem = new Stock(rs.getInt("StockId"), rs.getString("CompanyName"), rs.getString("TickerSymbol"),
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
