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

//	public StockDBScript() {
//		this.queue_name = "";
//		this.conn_string = "";
//	}

	public StockDBScript(String qn, String ip, String dbname, String u, String p) {
		this.queue_name = qn;
		this.conn_string = "jdbc:mysql://" + ip + "/" + dbname;
		this.username = u;
		this.password = p;
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
			System.out.println("Connected to DB");

			String query = "{CALL DeleteMarketPending(?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, id);

			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		con.close();
	}

	public void updateLastMatchedMarketPendingOrder(int id, int lastQty) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);
			System.out.println("Connected to DB");

			String query = "{CALL UpdateMarketPendingQuantity(?, ?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, id);
			stmt.setInt(2, lastQty);

			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
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
			System.out.println("Connected to DB");

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
			System.out.println("Connected to DB");

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
			System.out.println("Connected to DB");

			String query = "{CALL CloseSellMarketPendingOrders(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, marketPendingId);
			stmt.setInt(2, buyerId);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
	}

	private void closeBuyMarketPendingOrder(int marketPendingId, int sellerId) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);
			System.out.println("Connected to DB");

			String query = "{CALL CloseSellMarketPendingOrders(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, marketPendingId);
			stmt.setInt(2, sellerId);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
	}

	private void updatePurchaseInAccount(int buyerId, int value) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);
			System.out.println("Connected to DB");

			String query = "{CALL CloseSellMarketPendingOrders(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, buyerId);
			stmt.setInt(2, value);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
	}

	private void updateSaleInAccount(int sellerId, int value) throws SQLException {
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);
			System.out.println("Connected to DB");

			String query = "{CALL CloseSellMarketPendingOrders(?,?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call

			stmt.setInt(1, sellerId);
			stmt.setInt(2, value);
			ResultSet rs = stmt.executeQuery();
			System.out.println(rs);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
	}

	public ArrayList<MarketPending> retrieveOrdersToMatch(boolean isBuy, int stockId, float orderPrice)
			throws SQLException {
		Connection con = null;
		ArrayList<MarketPending> retrievedOrders = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);
			System.out.println("Connected to DB");

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
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
		return retrievedOrders;

	}

	public String receiveOrder(String message) throws SQLException {
		String[] splitArray = message.split(",");
		int stockId = Integer.parseInt(splitArray[0]);
		int sellerId = Integer.parseInt(splitArray[1]);
		int buyerId = Integer.parseInt(splitArray[2]);
		int qty = Integer.parseInt(splitArray[3]);
		float price = Float.parseFloat(splitArray[4]);
		Connection con = null;

		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);
			System.out.println("Connected to DB");

			if (sellerId == -1 && buyerId != -1) {
				// its a buy order

				ArrayList<MarketPending> fetchedOrders = retrieveOrdersToMatch(true, stockId, price);
				// If return no entries for matching
				if (fetchedOrders == null) {
					// No orders to match with, he want to buy but there's no one selling under his
					// buy price or equals to

					// If return no entry, create a marketpending order and insert.
					String query = "{CALL InsertToMarketPending(?,?,?,?,?,?)}";
					CallableStatement stmt = con.prepareCall(query); // prepare to call
					stmt.setInt(1, stockId);
					stmt.setNull(2, Types.NULL);
					stmt.setInt(3, buyerId);
					stmt.setInt(4, qty);
					stmt.setFloat(5, price);
					stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

					ResultSet rs = stmt.executeQuery();
					System.out.println(rs);

					return "success";

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

						if (buyOrderQuantity != 0) {
							// means that the order cannot be fulfilled fully, still need more quantity,
							// will create a new marketpending order for the rest of the qty.
							addMarketPendingOrder(true, stockId, buyerId, buyOrderQuantity, price);
							// update the user account values with currently executed order total price.
							int totalQtyBought = qty - buyOrderQuantity;
							float totalPaid = avgPrice * totalQtyBought;
							// make a function that is like updatePurchaseInAccount(value)
							// then it will ( minus acc value, + securityvalue, - available cash)
							// updateSaleInAccount

						} else {
							// order fulfilled perfectly
							// update the user account values with total cost spent
							float totalPaid = price * qty;

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

					// If return no entry, create a marketpending order and insert.
					String query = "{CALL InsertToMarketPending(?,?,?,?,?,?)}";
					CallableStatement stmt = con.prepareCall(query); // prepare to call
					stmt.setInt(1, stockId);
					stmt.setInt(2, sellerId);
					stmt.setNull(3, Types.NULL);
					stmt.setInt(4, qty);
					stmt.setFloat(5, price);
					stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

					ResultSet rs = stmt.executeQuery();
					System.out.println(rs);

				} else {
					// if there are orders fetched for matching.

				}
				fetchedOrders.forEach(item -> {
					System.out.println(item);
				});

			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "offline";

		}

		// IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price
		// Double, In transactionDate DATETIME

		// Do market algo first, pull entries first.

		// later
		con.close();
		return "success"; // remove later, this is here to remove error.
	}

	public ArrayList<StockOwned> getOwnedStocks(int accountId) throws SQLException {
		ArrayList<StockOwned> arrayListOwned = null;
		Connection con = null;
		try {
			Class.forName(DRIVER_CLASS);
			con = (Connection) DriverManager.getConnection(this.conn_string, this.username, this.password);
			System.out.println("Connected to DB");

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
			System.out.println("Connected to DB");

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
			System.out.println("Connected to DB");

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
			System.out.println("Connected to DB");

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
			System.out.println("Connected to DB");

			String query = "{CALL getTop5CompletedOrderByStockId(?)}";
			CallableStatement stmt = con.prepareCall(query); // prepare to call
			stmt.setInt(1, stockId); // Set the parameter
			
			ResultSet rs = stmt.executeQuery();
			if(rs.next())
			{
				averagePrice = rs.getDouble("averagePrice");
			}
		}
		catch(ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		con.close();
		
		return averagePrice;
	}
	
	public double randomInRange(double min, double max) {
		  Random random = new Random();
		  double range = max - min;
		  double scaled = random.nextDouble() * range;
		  double shifted = scaled + min;
		  return shifted; 
	}
	
	public void dbRandomOrderGeneration() {
		ArrayList<Stock> arrayListStocks = null;
		try {
			arrayListStocks = getAllStocks();
			
			boolean buyOrSell = false;
			boolean randomSign = false;
			int quantity = 0;
			Random rd = new Random();
	
			for (Stock stock :arrayListStocks) {
				for(int i = 0; i <20; i++) {
					double offsetValue = 0;
					double averagePrice = 0;
					String formattedPrice = "";
					StringBuilder message = new StringBuilder("");
					//buy = 0, sell = 1
					buyOrSell = rd.nextBoolean();
					//Get the average price of top 5 per stock
					averagePrice = getAvgCompletedOrder(stock.getStockId());
					if (averagePrice < 10) {
						offsetValue = randomInRange(0,0.1);	
					}
					else if(averagePrice >= 10 && averagePrice < 100) {
						offsetValue = randomInRange(0,0.2);	
					}
					else {
						offsetValue = randomInRange(0,0.5);	
					}
					// true = + , false = -
					randomSign = rd.nextBoolean();
					//Generate new quantity
					quantity = rd.nextInt(50);
					System.out.println("OffsetValue " + offsetValue);
					System.out.println(averagePrice);
					formattedPrice = String.format("%.2f", averagePrice + offsetValue);
		
					if(randomSign == true && buyOrSell == true)
					{
							message.append(stock.getStockId()).append(",").append(-1).append(",").append(0).append(",").append(quantity).append(",").append(formattedPrice);
					}
					else if(randomSign == true && buyOrSell == false){
							message.append(stock.getStockId()).append(",").append(0).append(",").append(-1).append(",").append(quantity).append(",").append(formattedPrice);
						
					}
					else if(randomSign == false && buyOrSell == true)
					{
						message.append(stock.getStockId()).append(",").append(-1).append(",").append(0).append(",").append(quantity).append(",").append(formattedPrice);
						
					}
					else if(randomSign == false && buyOrSell == false)
					{
						message.append(stock.getStockId()).append(",").append(0).append(",").append(-1).append(",").append(quantity).append(",").append(formattedPrice);

					}
					if (!message.equals("")) {
						System.out.println(message.toString());
						receiveOrder(message.toString());
					}				
				}
			}
					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}