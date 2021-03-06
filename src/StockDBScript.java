import java.rmi.RemoteException;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import classes.MarketComplete;
import classes.MarketPending;
import classes.OrderBook;
import classes.Stock;
import classes.StockOwned;

//Handle rabbitMQ and Database for each market

public class StockDBScript {
	private boolean isOnline = true; // will be true unless algo detected offline in RemoteServant.java
	private String queue_name;
	public final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private String username;
	private String password;
	private String conn_string;
	private String market;
	private AccountDetailsDbScript accountDetailsDb;
	private HashMap<Integer, ClientInt> clientHashMap; // accountId and clientInterface

	public StockDBScript(String m, String qn, String ip, String dbname, String u, String p,
			AccountDetailsDbScript accountDetailsDb) {
		this.market = m;
		this.queue_name = qn;
		this.conn_string = "jdbc:mysql://" + ip + "/" + dbname;
		this.username = u;
		this.password = p;
		this.accountDetailsDb = accountDetailsDb;
		clientHashMap = new HashMap<Integer, ClientInt>();

	}

	public void setConnString(String ipandPort, String dbName) {
		conn_string = "jdbc:mysql://" + ipandPort + "/" + dbName;
	}

	public String getConnString() {
		return conn_string;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	// Client HashMap
	public void addToClientHashMap(ClientInt cc, int accountId) {
		clientHashMap.put(accountId, cc);

	}

	public void removeFromClientHashMap(int accountId) {
		if (clientHashMap.containsKey(accountId)) {
			clientHashMap.remove(accountId);
		}
	}

	public ClientInt retrieveClientIntFromHashMap(int accountId) {
		System.out.println("retrieve from hashmap print");
		System.out.println(clientHashMap);

		if (clientHashMap.containsKey(accountId)) {
			return clientHashMap.get(accountId);
		}
		return null;
	}

	//RabbitMQ receiver
	public void startWaitForMsg() {
		System.out.println("Starting wait for msg function");

		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			factory.setAutomaticRecoveryEnabled(false);
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
					System.out.println("error during receiveOrder function");
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

	// Remove pending order
	private void deleteMarketPendingOrder(int id, String currConn) throws SQLException {
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(currConn, this.username, this.password);

			String query = "{CALL DeleteMarketPending(?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, id);

			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();

		}
	}

	// Update pending order quantity
	// Insertion of completed transaction
	private void updateLastMatchedMarketPendingOrder(boolean isBuy, int id, int lastQty, int qty, int accId, String currConn) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(currConn, this.username, this.password);
			String query;
			if (isBuy) {
			 query = "{CALL UpdateSellMarketPendingQuantity(?, ?, ?, ?)}";
			} else {
				 query = "{CALL UpdateBuyMarketPendingQuantity(?, ?, ?, ?)}";

			}
			
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, id);
			stmt.setInt(2, lastQty);
			stmt.setInt(3, qty);
			stmt.setInt(4, accId);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();

		}
		con.close();
	}

	// Add complete transaction
	private void addMarketCompleteOrder(boolean isBuy, int stockId, int sellerId, int buyerId, int totalQty,
			float avgPrice, String currConn) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(currConn, this.username, this.password);

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
			e.printStackTrace();

		}
		con.close();
	}

	// Add pending order
	private void addMarketPendingOrder(boolean isBuy, int stockId, int transactionAccId, int totalQty, float price,
			String currConn) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(currConn, this.username, this.password);

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
			e.printStackTrace();
		}
		con.close();
	}

	// Remove sell pending order
	// Insert completed transaction
	private void closeSellMarketPendingOrder(int marketPendingId, int buyerId, String currConn) throws SQLException {
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
			e.printStackTrace();
		}
	}

	// Remove buy pending order
	// Insert completed transaction
	private void closeBuyMarketPendingOrder(int marketPendingId, int sellerId) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL CloseBuyMarketPendingOrders(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, marketPendingId);
			stmt.setInt(2, sellerId);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
			con.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Retrieve client holding for specific stock
	private int fetchHoldingQtyByStockIdandAccId(int stockId, int accId) throws SQLException {
		Connection con = null;
		int qty = -1;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);

			String query = "{CALL getQuantityByAccountIdAndStockId(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, accId);
			stmt.setInt(2, stockId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				qty = rs.getInt("Quantity");
			}
			con.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return qty;
	}

	// Retrieve pending order that matches client request
	private ArrayList<MarketPending> retrieveOrdersToMatch(boolean isBuy, int stockId, float orderPrice,
			String currConn) throws SQLException {
		System.out.println("retrieving orders to match function");
		Connection con = null;
		ArrayList<MarketPending> retrievedOrders = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(currConn, this.username, this.password);

			if (isBuy) {
				String query = "{CALL getPendingSellOrdersRequiredForNewInsertion(?, ?)}";
				// stock id and input price
				CallableStatement stmt = con.prepareCall(query); // prepare to call

				stmt.setInt(1, stockId); // Set the parameter
				stmt.setFloat(2, orderPrice); // Set the parameter

				ResultSet rs = stmt.executeQuery();

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
			e.printStackTrace();
		}
		con.close();
		return retrievedOrders;
	}

	// Received order from the RabbitMQ
	// Processing of the order
	private void receiveOrder(String message) throws RemoteException {
		System.out.println("receive order with msg " + message);
		String[] splitArray = message.split(",");
		int stockId = Integer.parseInt(splitArray[0]);
		int sellerId = Integer.parseInt(splitArray[1]);
		int buyerId = Integer.parseInt(splitArray[2]);
		int qty = Integer.parseInt(splitArray[3]);
		float price = Float.parseFloat(splitArray[4]);
		System.out.println("stock id is " + stockId);
		int totalQtyStocks = 0;
		boolean bought = false;
		boolean sold = false;
		ClientInt currClient = null;
		int accountId = -1;
		Connection con = null;
		String currConn = conn_string;

		boolean isbuyOrder;
		boolean isRandomGenOrder = false;

		float accountBalance = 0;
		// Check if is buyer or seller order first.
		if (sellerId == -1 && buyerId != -1) {
			System.out.println("receive order is buyer order");
			currClient = this.retrieveClientIntFromHashMap(buyerId);
			accountId = buyerId;
			isbuyOrder = true;
		} else {
			System.out.println("receive order is seller order");
			currClient = this.retrieveClientIntFromHashMap(sellerId);
			accountId = sellerId;
			isbuyOrder = false;

		}

		if (buyerId == 0 || sellerId == 0) {
			System.out.println("receive order is randomGen order");

			// order is randomGenOrder
			isRandomGenOrder = true;

		}
		try {

			if (!isRandomGenOrder) {
				// received order is not randomGen order
				System.out.println("received order is not randomGen order");

				// check balance if not randomGenOrder
				accountBalance = accountDetailsDb.getAccountBalanceById(accountId);
				if (!this.isOnline) {
					// server down and order is not bot order, print to buyer error message.
					currClient.printToClient("error processing");
					return;
				}
			}

			// Order processing
			if (isbuyOrder) {
				// its a buy order
				System.out.println("Is Buy Order");

				float orderValue = qty * price;
				if ((accountBalance < orderValue) && !isRandomGenOrder) {
					System.out.println("account bal" + accountBalance);
					System.out.println("qty and price " + qty + " " + price);

					System.out.println("order Value " + orderValue);

					System.out.println("account balance not enough");
					try {
						currClient.printToClient("not enough balance");
						return;
					} catch (RemoteException e) {
						System.out.println("client offline / cannot connect to client");
						e.printStackTrace();
					}
				}

				ArrayList<MarketPending> fetchedOrders = retrieveOrdersToMatch(true, stockId, price, currConn);
				// If return no entries for matching
				if (fetchedOrders == null) {
					System.out.println("No fetched orders, Inserting buy order ");

					// No orders to match with, he want to buy but there's no one selling under his
					// buy price or equals to

					Class.forName(DRIVER_CLASS);
					System.out.println("print conn string, username, password" + currConn + " " + this.username + " "
							+ this.password);
					con = (Connection) DriverManager.getConnection(currConn, this.username, this.password);
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
					System.out.println("Got fetched orders");
					int buyOrderQuantity = qty;
					int lastOrderQty = 0;
					float avgPrice = 0; // average price throughout the filled orders

					ArrayList<Integer> orderIds = new ArrayList<Integer>(); // get list of order IDs so later can use
																			// for
																			// updating them in DB.

					// minus the quantity per order and then execute the update in SQL.
					for (int i = 0; i < fetchedOrders.size(); i++) {
						float moneyPaid = 0;
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
							System.out.println("order qty is " + order.getQuantity());
							System.out.println("my buy order qty is " + buyOrderQuantity);
							
							System.out.println("last order qty is " + lastOrderQty);
							
							moneyPaid += order.getPrice() * order.getQuantity(); // accumulate the money paid by adding
																					// it per order.

							totalQtyStocks += buyOrderQuantity; // accumulate the total qty of stocks bought by
																// adding it per order.

							buyOrderQuantity = 0; // make buy order quantity 0 to break the loop, so will stop looking
							// for more matches

							orderIds.add(order.getMarketPendingId()); // keep array list of order ID so later can update
																		// those orders through SQL.

						} else {
							// Shouldn't reach here at all.
						}
					}
					if (orderIds.size() == 0) {
						// no matched orders, make a new marketpending order with initial values.
						System.out.println("got fetched orders but no matched orders");
						addMarketPendingOrder(true, stockId, buyerId, qty, price, currConn);

					} else {
						System.out.println("got fetched orders and got matched orders");
						for (int i = 0; i < orderIds.size(); i++) { // loop through order IDs and update / delete them
							// indicating match.

							if (i == orderIds.size() - 1) {
								// once reach last item, check if lastOrderQty has any value. If have, update
								// the last order with that qty.

								if (lastOrderQty != 0) {
									// update last order here with that qty, call SQL function.
									System.out.println("update last matched market pending order");

									// close last matched order
									updateLastMatchedMarketPendingOrder(true, orderIds.get(i), lastOrderQty, qty, accountId, currConn);
									
									// update that guy sell order with lastorderqty, make a new order completed with my buy id and his sell id, with my buyorder qty.
									break; // break to stop the loop and not let it close the last matched order.
								}
							}

							System.out.println("closing sell market pending order");
							// call closemarketpendingOrder
							closeSellMarketPendingOrder(orderIds.get(i), buyerId, currConn); // this will delete
																								// marketpending
							// entries and create
							// corresponding marketcomplete
							// entries with buyerId.

						}

						float totalPaid;
						if (buyOrderQuantity != 0) {
							// means that the order cannot be fulfilled fully, still need more quantity,
							// will create a new marketpending order for the rest of the qty.
							System.out.println(
									"add market pending because after closing matched orders, still got more to buy.");
							addMarketPendingOrder(true, stockId, buyerId, buyOrderQuantity, price, currConn);
							// update the user account values with currently executed order total price.
							int totalQtyBought = qty - buyOrderQuantity;
							totalPaid = avgPrice * totalQtyBought;

						} else {
							// order fulfilled perfectly
							// update the user account values with total cost spent
							totalPaid = price * qty;

						}
						if (!isRandomGenOrder) {
							this.accountDetailsDb.updatePurchaseInAccount(buyerId, totalPaid);
							bought = true;
						}
					}

				}

			} else

			{
				System.out.println("Is sell order");
				// its a sell order

				// check if sell qty match with his own holdings qty.
				if (!isRandomGenOrder) {
					int personHoldingQty = this.fetchHoldingQtyByStockIdandAccId(stockId, accountId);
					if (personHoldingQty < qty) {
						this.retrieveClientIntFromHashMap(accountId).printToClient("not enough quantity");
						return;

					} else if (personHoldingQty == -1) {
						this.retrieveClientIntFromHashMap(accountId).printToClient("error processing ");
						return;
					} else {
//						System.out.println("enough qty to sell");
					}
				}

				ArrayList<MarketPending> fetchedOrders = retrieveOrdersToMatch(false, stockId, price, currConn);
				if (fetchedOrders == null) {
					System.out.println("Inserting sell order");
					// No orders to match with, he want to sell but there's no one buying above his
					// sell price or equals to.
					Class.forName(DRIVER_CLASS);
					con = (Connection) DriverManager.getConnection(currConn, this.username, this.password);
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
							System.out.println("order qty is " + order.getQuantity());
							System.out.println("my sell order qty is " + sellOrderQuantity);
							
							System.out.println("last order qty is " + sellOrderQuantity);

							moneyReceived += order.getPrice() * order.getQuantity(); // accumulate the money received by
																						// adding
																						// it per order.

							totalQtyStocks += sellOrderQuantity; // accumulate the total qty of stocks bought by
																	// adding it per order.

							sellOrderQuantity = 0; // make sell order quantity 0 to break the loop, so will stop looking
							// for more matches

							orderIds.add(order.getMarketPendingId()); // keep array list of order ID so later can update
																		// those orders through SQL.

						} else {
							// Shouldn't reach here at all.
						}

					}
					if (orderIds.size() == 0) {
						// no matched orders, make a new marketpending order with initial values.
						addMarketPendingOrder(false, stockId, sellerId, qty, price, currConn);

					} else {
						for (int i = 0; i < orderIds.size(); i++) { // loop through order IDs and update / delete them
							// indicating match.

							if (i == orderIds.size() - 1) {
								// once reach last item, check if lastOrderQty has any value. If have, update
								// the last order with that qty.

								if (lastOrderQty != 0) {
									// update last order here with that qty, call SQL function.
									updateLastMatchedMarketPendingOrder(false, orderIds.get(i), lastOrderQty, qty, accountId, currConn);
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
							addMarketPendingOrder(false, stockId, sellerId, sellOrderQuantity, price, currConn);
							// update the user account values with currently executed order total price.
							int totalQtySold = qty - sellOrderQuantity;
							totalValueSold = avgPrice * totalQtySold;
						} else {
							// order fulfilled perfectly
							// update the user account values with total cost spent
							totalValueSold = price * qty;

						}
						if (!isRandomGenOrder) {
							this.accountDetailsDb.updateSaleInAccount(sellerId, totalValueSold);
							sold = true;
						}
					}
				}

			}
			if (!isRandomGenOrder)
				currClient.updateOrderBook(market, stockId, bought, sold, totalQtyStocks);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			if (!isRandomGenOrder) {
				currClient.printToClient("error processing");
			} else {
				// will reach here if it is randomGenOrder, if randomGenOrder, cannot print to
				// client, just print statement.
				System.out.println("Error when processing random gen order");
			}
		}
	}

	// Retrieve all stocks owned by client
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

				StockOwned stockOwnedItem = new StockOwned(this.market, rs.getInt("StockId"),
						rs.getString("CompanyName"), rs.getString("TickerSymbol"), rs.getInt(4), rs.getInt(5));
				System.out.println(stockOwnedItem.toString());
				arrayListOwned.add(stockOwnedItem);
				count++;

			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		con.close();
		return arrayListOwned;
	}

	// Retrieve all stocks in the market
	public ArrayList<Stock> getAllStocks() throws SQLException {
		ArrayList<Stock> arrayListStocks = null;
		Connection con = null;
		String currConn = conn_string;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(currConn, username, password);

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

				arrayListStocks.add(stockItem);
				count++;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (CommunicationsException e) {
			e.printStackTrace();
			System.out.println(market + "-" + currConn);
			return null;

		}
		con.close();
		return arrayListStocks;

	}

	// Retrieve all pending order for the stock
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

				arrayListOrders.add(marketOrder);
				count++;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		con.close();
		return arrayListOrders;
	}

	// Retrieve all completed transaction for the stock
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

				arrayListOrders.add(marketOrder);
				count++;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		con.close();
		return arrayListOrders;
	}

	// Retrieve average price of the 5 latest completed transaction
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
			e.printStackTrace();
		}
		con.close();

		return averagePrice;
	}

	// Retrieve the order book for the stock
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
				OrderBook orderbook = new OrderBook(rs.getString("Type"), rs.getInt("Quantity"), rs.getFloat("Price"));

				arrayListOrderBook.add(orderbook);
				count++;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		con.close();
		return arrayListOrderBook;
	}

	// Generate a random number within the range
	public double randomInRange(double min, double max) {
		Random random = new Random();
		double range = max - min;
		double scaled = random.nextDouble() * range;
		double shifted = scaled + min;
		return shifted;
	}

	// Generate random order to simulate active market
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

			if (averagePrice == 0) {
				averagePrice = randomInRange(0.1, 150.0);
			}

			if (averagePrice < 10) {

				offsetValue = randomInRange(-0.1, 0.1);
			} else if (averagePrice >= 10 && averagePrice < 100) {
				offsetValue = randomInRange(-0.2, 0.2);
			} else {
				offsetValue = randomInRange(-0.5, 0.5);
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
			e.printStackTrace();
		}
		return message.toString();
	}

}