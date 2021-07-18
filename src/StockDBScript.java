import java.rmi.RemoteException;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

import com.mysql.jdbc.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import classes.MarketComplete;
import classes.MarketPending;
import classes.OrderBook;
import classes.Stock;
import classes.StockOwned;

//Put db connection and queries for Hk market db here.
//Put rabbitMQ receiver here to receive from their own market topic. Will receive from servant.java

public class StockDBScript {
	private boolean isOnline = true; // will be true unless algo detected offline in RemoteServant.java
	private ClientInt currentClientInt;
	private String queue_name;
	public final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private String username;
	private String password;
	private String conn_string; // jdbc:mysql://ip:3306/DBNAME
	private AccountDetailsDbScript accountDetailsDb;
//	public StockDBScript() {
//		this.queue_name = "";
//		this.conn_string = "";
//	}

	public StockDBScript(String qn, String ip, String dbname, String u, String p,
			AccountDetailsDbScript accountDetailsDb) {
		this.queue_name = qn;
		this.conn_string = "jdbc:mysql://" + ip + "/" + dbname;
		this.username = u;
		this.password = p;
		this.accountDetailsDb = accountDetailsDb;
	}

	public void setConnString(String ipandPort, String dbName) {
		conn_string = "jdbc:mysql//" + ipandPort + dbName;
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

			channel.queueDeclare(queue_name, false, false, false, null);
			System.out.println(" [*] DbScript waiting for msg.");

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				String message = new String(delivery.getBody(), "UTF-8");
				System.out.println(" [x] Received '" + message + "'");
				try {
					receiveOrder(message);
				} catch (Exception e) {

					if (this.getCurrentClientInt() == null) {
						System.out.println("start wait for msg exception");

					} else {
						// Call back to client
						this.getCurrentClientInt().printToClient("Transaction failed while processing order.");
					}

					e.printStackTrace();
				}
			};
			channel.basicConsume(queue_name, true, deliverCallback, consumerTag -> {
			});
		} catch (Exception e) {
			System.out.println("server cannot connect");
			e.printStackTrace();
		}
	}

	public void deleteMarketPendingOrder(int id) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL DeleteMarketPending(?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, id);

			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	public void updateLastMatchedMarketPendingOrder(int id, int lastQty) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL UpdateMarketPendingQuantity(?, ?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, id);
			stmt.setInt(2, lastQty);

			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		con.close();
	}

	private void addMarketCompleteOrder(boolean isBuy, int stockId, int sellerId, int buyerId, int totalQty,
			float avgPrice) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL InsertToMarketComplete(?,?,?,?,?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, stockId);
			stmt.setNull(2, Types.NULL);
			stmt.setInt(3, buyerId);
			stmt.setInt(4, totalQty);
			stmt.setFloat(5, avgPrice);
			stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		con.close();
	}

	private void addMarketPendingOrder(boolean isBuy, int stockId, int transactionAccId, int totalQty, float price)
			throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL InsertToMarketPending(?,?,?,?,?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, stockId);
			if (isBuy) {
				stmt.setNull(2, Types.NULL);
				stmt.setInt(3, transactionAccId);

			} else {
				stmt.setNull(2, transactionAccId);
				stmt.setInt(3, Types.NULL);
			}
			stmt.setInt(4, totalQty);
			stmt.setFloat(5, price);
			stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
	}

	private void closeSellMarketPendingOrder(int marketPendingId, int buyerId) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL CloseSellMarketPendingOrders(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, marketPendingId);
			stmt.setInt(2, buyerId);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void closeBuyMarketPendingOrder(int marketPendingId, int sellerId) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL CloseSellMarketPendingOrders(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, marketPendingId);
			stmt.setInt(2, sellerId);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updatePurchaseInAccount(int buyerId, float totalPaid) throws SQLException {
		Connection con = null;
//		( minus acc value, + securityvalue, - available cash)

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL updatePurchaseInAccount(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, buyerId);
			stmt.setFloat(2, totalPaid);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateSaleInAccount(int sellerId, float value) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL updateSaleInAccount(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, sellerId);
			stmt.setFloat(2, value);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<MarketPending> retrieveOrdersToMatch(boolean isBuy, int stockId, float orderPrice)
			throws SQLException {
		Connection con = null;
		ArrayList<MarketPending> retrievedOrders = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

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
					java.sql.Timestamp dbSqlTimestamp = rs.getTimestamp("CreatedDate");
					LocalDateTime localDateTime = dbSqlTimestamp.toLocalDateTime();
					MarketPending marketOrder = new MarketPending(rs.getInt("MarketPendingId"), rs.getInt("StockId"),
							rs.getInt("SellerId"), rs.getInt("BuyerId"), rs.getInt("Quantity"), rs.getFloat("Price"),
							localDateTime);
					retrievedOrders.add(marketOrder);
					count++;

				}
				con.close();
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
				con.close();

			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
		return retrievedOrders;

	}

	private void receiveOrder(String message) throws SQLException, RemoteException, ClassNotFoundException {
		String[] splitArray = message.split(",");
		int stockId = Integer.parseInt(splitArray[0]);
		int sellerId = Integer.parseInt(splitArray[1]);
		int buyerId = Integer.parseInt(splitArray[2]);
		int qty = Integer.parseInt(splitArray[3]);
		float price = Float.parseFloat(splitArray[4]);
		Connection con = null;
		boolean isbuyOrder;
		boolean isRandomGenOrder = false;

		float accountBalance = 0;
		// Check if is buyer or seller order first.
		if (sellerId == -1 && buyerId != -1) {
			isbuyOrder = true;

			if (buyerId == 0) {
				isRandomGenOrder = true;
			} else {
				accountBalance = accountDetailsDb.getAccountBalanceById(buyerId);
			}
		} else {
			isbuyOrder = false;

			if (sellerId == 0) {
				isRandomGenOrder = true;
			} else {
				accountBalance = accountDetailsDb.getAccountBalanceById(sellerId);
			}
		}

		float orderValue = qty * price;
		if (accountBalance < orderValue) {
			this.getCurrentClientInt().printToClient("not enough balance");
		}

		// Order processing
		if (isbuyOrder) {
			// its a buy order

			ArrayList<MarketPending> fetchedOrders = retrieveOrdersToMatch(true, stockId, price);
			// If return no entries for matching
			if (fetchedOrders == null) {
				// No orders to match with, he want to buy but there's no one selling under his
				// buy price or equals to

				Class.forName(DRIVER_CLASS);
				con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);
				// If return no entry, create a marketpending order and insert.
				String query1 = "{CALL InsertToMarketPending(?,?,?,?,?,?)}";
				CallableStatement stmt1 = con.prepareCall(query1); // prepare to call
				stmt1.setInt(1, stockId);
				stmt1.setNull(2, Types.NULL);
				stmt1.setInt(3, buyerId);
				stmt1.setInt(4, qty);
				stmt1.setFloat(5, price);
				stmt1.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

				ResultSet rs1 = stmt1.executeQuery();
				con.close();

				System.out.println(rs1);

			} else {

				int buyOrderQuantity = qty;
				int lastOrderQty = 0;
				float avgPrice = 0; // average price throughout the filled orders

				ArrayList<Integer> orderIds = new ArrayList<Integer>(); // get list of order IDs so later can use
																		// for
																		// updating them in DB.

				// minus the quantity per order and then execute the update in SQL.
				for (int i = 0; i < fetchedOrders.size(); i++) {
					float moneyPaid = 0;
					int totalQtyStocks = 0;
					MarketPending order = fetchedOrders.get(i);

					// minus my quantity here and see how much left.
					// avg the price throughout when minus quantity
					if (buyOrderQuantity <= 0) { // If buyOrderQuantity reached 0 or lesser, break the loop. Don't
													// search for more orders anymore.
						avgPrice = moneyPaid / totalQtyStocks;
						break;
					}

					if (order.getQuantity() <= buyOrderQuantity) { // if the fetched order quantity is smaller or
																	// equal
																	// to buyorderquantity

						buyOrderQuantity -= order.getQuantity();
						moneyPaid += order.getPrice() * order.getQuantity(); // accumulate the money paid by adding
																				// it
																				// per order.
						totalQtyStocks += order.getQuantity(); // accumulate the total qty of stocks bought by
																// adding it
																// per order.

						orderIds.add(order.getMarketPendingId()); // keep array list of order ID so later can update
																	// those orders through SQL.

					} else if (order.getQuantity() > buyOrderQuantity) { // if fetched order qty bigger than
																			// buyOrderQuantity

						lastOrderQty = order.getQuantity() - buyOrderQuantity;
						buyOrderQuantity = 0; // make buy order quantity 0 to break the loop, so will stop looking
												// for more matches

						moneyPaid += order.getPrice() * order.getQuantity(); // accumulate the money paid by adding
																				// it per order.

						totalQtyStocks += order.getQuantity(); // accumulate the total qty of stocks bought by
																// adding it per order.

						orderIds.add(order.getMarketPendingId()); // keep array list of order ID so later can update
																	// those orders through SQL.

					} else {
						// Shouldn't reach here at all.
					}
				}
				if (orderIds.size() == 0) {
					// no matched orders, make a new marketpending order with initial values.
					addMarketPendingOrder(true, stockId, buyerId, qty, price);

				} else {
					for (int i = 0; i < orderIds.size(); i++) { // loop through order IDs and update / delete them
						// indicating match.

						if (i == orderIds.size() - 1) {
							// once reach last item, check if lastOrderQty has any value. If have, update
							// the last order with that qty.

							if (lastOrderQty != 0) {
								// update last order here with that qty, call SQL function.
								updateLastMatchedMarketPendingOrder(orderIds.get(i), lastOrderQty);
								break; // break to stop the loop and not let it close the last matched order.
							}
						}

						// call closemarketpendingOrder
						closeSellMarketPendingOrder(orderIds.get(i), buyerId); // this will delete marketpending
																				// entries and create
																				// corresponding marketcomplete
																				// entries with buyerId.

					}

					float totalPaid;
					if (buyOrderQuantity != 0) {
						// means that the order cannot be fulfilled fully, still need more quantity,
						// will create a new marketpending order for the rest of the qty.
						addMarketPendingOrder(true, stockId, buyerId, buyOrderQuantity, price);
						// update the user account values with currently executed order total price.
						int totalQtyBought = qty - buyOrderQuantity;
						totalPaid = avgPrice * totalQtyBought;

					} else {
						// order fulfilled perfectly
						// update the user account values with total cost spent
						totalPaid = price * qty;

					}
					if (!isRandomGenOrder) {
						this.updatePurchaseInAccount(buyerId, totalPaid);
					}
				}

			}

		} else

		{
			// its a sell order
			ArrayList<MarketPending> fetchedOrders = retrieveOrdersToMatch(false, stockId, price);
			if (fetchedOrders == null) {
				// No orders to match with, he want to sell but there's no one buying above his
				// sell price or equals to.
				Class.forName(DRIVER_CLASS);
				con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);
				// If return no entry, create a marketpending order and insert.
				String query2 = "{CALL InsertToMarketPending(?,?,?,?,?,?)}";
				CallableStatement stmt2 = con.prepareCall(query2); // prepare to call
				stmt2.setInt(1, stockId);
				stmt2.setInt(2, sellerId);
				stmt2.setNull(3, Types.NULL);
				stmt2.setInt(4, qty);
				stmt2.setFloat(5, price);
				stmt2.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

				ResultSet rs2 = stmt2.executeQuery();
				System.out.println(rs2);

			} else {
				// if there are orders fetched for matching.
				int sellOrderQuantity = qty;
				int lastOrderQty = 0;
				float avgPrice = 0; // average price throughout the filled orders

				ArrayList<Integer> orderIds = new ArrayList<Integer>(); // get list of order IDs so later can use
																		// for
																		// updating them in DB.

				// minus the quantity per order and then execute the update in SQL.
				for (int i = 0; i < fetchedOrders.size(); i++) {
					float moneyReceived = 0;
					int totalQtyStocks = 0;
					MarketPending order = fetchedOrders.get(i);

					// minus my quantity here and see how much left.
					// avg the price throughout when minus quantity
					if (sellOrderQuantity <= 0) { // If buyOrderQuantity reached 0 or lesser, break the loop. Don't
													// search for more orders anymore.
						avgPrice = moneyReceived / totalQtyStocks;
						break;
					}

					if (order.getQuantity() <= sellOrderQuantity) { // if the fetched order quantity is smaller or
																	// equal
																	// to sellOrderQuantity

						sellOrderQuantity -= order.getQuantity();
						moneyReceived += order.getPrice() * order.getQuantity(); // accumulate the moneyReceived by
																					// adding it
																					// per order.
						totalQtyStocks += order.getQuantity(); // accumulate the total qty of stocks sold by
																// adding it
																// per order.

						orderIds.add(order.getMarketPendingId()); // keep array list of order ID so later can update
																	// those orders through SQL.

					} else if (order.getQuantity() > sellOrderQuantity) { // if fetched order qty bigger than
																			// sellOrderQuantity

						lastOrderQty = order.getQuantity() - sellOrderQuantity;
						sellOrderQuantity = 0; // make sell order quantity 0 to break the loop, so will stop looking
												// for more matches

						moneyReceived += order.getPrice() * order.getQuantity(); // accumulate the money received by
																					// adding
																					// it per order.

						totalQtyStocks += order.getQuantity(); // accumulate the total qty of stocks bought by
																// adding it per order.

						orderIds.add(order.getMarketPendingId()); // keep array list of order ID so later can update
																	// those orders through SQL.

					} else {
						// Shouldn't reach here at all.
					}

				}
				if (orderIds.size() == 0) {
					// no matched orders, make a new marketpending order with initial values.
					addMarketPendingOrder(false, stockId, sellerId, qty, price);

				} else {
					for (int i = 0; i < orderIds.size(); i++) { // loop through order IDs and update / delete them
						// indicating match.

						if (i == orderIds.size() - 1) {
							// once reach last item, check if lastOrderQty has any value. If have, update
							// the last order with that qty.

							if (lastOrderQty != 0) {
								// update last order here with that qty, call SQL function.
								updateLastMatchedMarketPendingOrder(orderIds.get(i), lastOrderQty);
								break; // break to stop the loop and not let it close the last matched order.
							}
						}

						// call closemarketpendingOrder
						closeBuyMarketPendingOrder(orderIds.get(i), sellerId); // this will delete marketpending
																				// entries and create
																				// corresponding marketcomplete
																				// entries with sellerId.

					}

					float totalValueSold;
					if (sellOrderQuantity != 0) {
						// means that the order cannot be fulfilled fully, still need more quantity,
						// will create a new marketpending order for the rest of the qty.
						addMarketPendingOrder(false, stockId, sellerId, sellOrderQuantity, price);
						// update the user account values with currently executed order total price.
						int totalQtySold = qty - sellOrderQuantity;
						totalValueSold = avgPrice * totalQtySold;
					} else {
						// order fulfilled perfectly
						// update the user account values with total cost spent
						totalValueSold = price * qty;

					}
					if (!isRandomGenOrder) {

						this.updateSaleInAccount(sellerId, totalValueSold);
					}
				}
			}

		}

		con.close();
	}

	public ArrayList<StockOwned> getOwnedStocks(int accountId) throws SQLException {
		ArrayList<StockOwned> arrayListOwned = null;
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

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
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		con.close();
		return arrayListOwned;
	}

	public ArrayList<Stock> getAllStocks() throws SQLException {
		ArrayList<Stock> arrayListStocks = null;
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(conn_string, username, password);

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
				Stock stockItem = new Stock(rs.getInt("StockId"), rs.getString("CompanyName"),
						rs.getString("TickerSymbol"), rs.getFloat("CurrentValue"), rs.getBoolean("Status"),
						rs.getString("Timezone"), localDateTime);

				System.out.println(stockItem.toString());
				arrayListStocks.add(stockItem);
				count++;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
		return arrayListStocks;

	}

	public ArrayList<MarketPending> getPendingOrders(int stockId) throws SQLException {
		ArrayList<MarketPending> arrayListOrders = null;
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(conn_string, username, password);

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
				MarketPending marketOrder = new MarketPending(rs.getInt("MarketPendingId"), rs.getInt("StockId"),
						SellerId, BuyerId, rs.getInt("Quantity"), rs.getFloat("Price"), localDateTime);

				marketOrder.setCreatedDate(localDateTime);

				System.out.println("Market Pending: ");
				System.out.println(marketOrder.toString());
				arrayListOrders.add(marketOrder);
				count++;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
		return arrayListOrders;
	}

	public ArrayList<MarketComplete> getCompletedOrders(int stockId) throws SQLException {
		ArrayList<MarketComplete> arrayListOrders = null;
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(conn_string, username, password);

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
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
		return arrayListOrders;
	}

	public double getAvgCompletedOrder(int stockId) throws SQLException {
		Connection con = null;
		double averagePrice = 0;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL getTop5CompletedOrderByStockId(?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, stockId); // Set the parameter

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				averagePrice = rs.getDouble("averagePrice");
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();

		return averagePrice;
	}

	public ArrayList<OrderBook> getOrderBook(int stockId) throws SQLException {
		ArrayList<OrderBook> arrayListOrderBook = null;
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL getOrderBookByStockId(?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, stockId); // Set the parameter

			ResultSet rs = stmt.executeQuery();
			int count = 0;
			while (rs.next()) {
				if (count == 0) {
					arrayListOrderBook = new ArrayList<OrderBook>(); // initialize arraylist if results to be found
				}
				java.sql.Timestamp dbSqlTimestamp = rs.getTimestamp("TransactionDate");
				LocalDateTime localDateTime = dbSqlTimestamp.toLocalDateTime();

				OrderBook orderbook = new OrderBook(rs.getString("Type"), rs.getInt("Quantity"), rs.getFloat("Price"));

				System.out.println("Market Completed: ");
				System.out.println(orderbook.toString());
				arrayListOrderBook.add(orderbook);
				count++;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
		return arrayListOrderBook;
	}

	public double randomInRange(double min, double max) {
		Random random = new Random();
		double range = max - min;
		double scaled = random.nextDouble() * range;
		double shifted = scaled + min;
		return shifted;
	}

	public String dbRandomOrderGeneration(int stockId) {
		StringBuilder message = new StringBuilder("");
		try {
			boolean buyOrSell = false;
			boolean randomSign = false;
			int quantity = 0;
			Random rd = new Random();
			double offsetValue = 0;
			double averagePrice = 0;
			String formattedPrice = "";

			// buy = 0, sell = 1
			buyOrSell = rd.nextBoolean();
			// Get the average price of top 5 per stock
			averagePrice = getAvgCompletedOrder(stockId);
			if (averagePrice < 10) {
				offsetValue = randomInRange(0, 0.1);
			} else if (averagePrice >= 10 && averagePrice < 100) {
				offsetValue = randomInRange(0, 0.2);
			} else {
				offsetValue = randomInRange(0, 0.5);
			}
			// true = + , false = -
			randomSign = rd.nextBoolean();
			// Generate new quantity
			quantity = rd.nextInt(50);
			System.out.println("OffsetValue " + offsetValue);
			System.out.println(averagePrice);
			formattedPrice = String.format("%.2f", averagePrice + offsetValue);

			if (randomSign == true && buyOrSell == true) {
				message.append(stockId).append(",").append(-1).append(",").append(0).append(",").append(quantity)
						.append(",").append(formattedPrice);
			} else if (randomSign == true && buyOrSell == false) {
				message.append(stockId).append(",").append(0).append(",").append(-1).append(",").append(quantity)
						.append(",").append(formattedPrice);

			} else if (randomSign == false && buyOrSell == true) {
				message.append(stockId).append(",").append(-1).append(",").append(0).append(",").append(quantity)
						.append(",").append(formattedPrice);

			} else if (randomSign == false && buyOrSell == false) {
				message.append(stockId).append(",").append(0).append(",").append(-1).append(",").append(quantity)
						.append(",").append(formattedPrice);

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message.toString();
	}

}