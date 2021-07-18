import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knowm.xchart.OHLCChart;
import org.knowm.xchart.OHLCChartBuilder;
import org.knowm.xchart.XChartPanel;

import com.fasterxml.jackson.databind.ObjectMapper;

import classes.AccountDetails;
import classes.MarketComplete;
import classes.MarketPending;
import classes.OrderBook;
import classes.Stock;

public class Client extends java.rmi.server.UnicastRemoteObject implements ClientInt {
	private RemoteInterface remoteObj;
    private Insets defaultInsets;
	private JFrame frame;
    private JPanel loginPanel;
    private JPanel homePanel;
    private JPanel chartPanel;
    private AccountDetails accountDetailsObj;
    private ArrayList<OrderBook> arrayListOrderBook;
    private enum Market{
    	SG,
    	HK,
    	US
    }
    private enum Page{
    	SG,
    	HK,
    	US,
    	STOCK,
    	OTHER
    };
    private int currentDisplayStockId;
    private Market currentDisplayMarket;
    private Page currentPage;
    
	public Client() throws RemoteException {
		try {
			remoteObj = (RemoteInterface) Naming.lookup("rmi://localhost:1099/RemoteServer");
			currentDisplayStockId = -1;
			arrayListOrderBook = null;
			initialise();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Unicast from server to client to print whatever.
	public void printToClient(String s) throws java.rmi.RemoteException {

		System.out.println(s);
	}

	public void updateMarket(String market) throws java.rmi.RemoteException {
		String m = currentPage.name();
		if(m.equals(market)) {
			JTabbedPane mPane = (JTabbedPane) homePanel.getComponent(0);
			JPanel panel = null;
			switch(currentPage){
			case SG:
				panel = (JPanel) mPane.getComponentAt(1);
				panel = createCountryPanel(Market.SG);
				break;
			case HK:
				panel = (JPanel) mPane.getComponentAt(2);
				panel = createCountryPanel(Market.HK);
				break;
			case US:
				panel = (JPanel) mPane.getComponentAt(3);
				panel = createCountryPanel(Market.US);
				break;
			}
		}
	}

	public void updateStock(String market, int stockId) throws java.rmi.RemoteException {
		if(currentPage.name().equals("STOCK") && currentDisplayMarket.name().equals(market) 
				&& stockId == currentDisplayStockId) {
			
		}
	}

	public void sendOrderBook(String orderbook) throws java.rmi.RemoteException {
		arrayListOrderBook = (ArrayList<OrderBook>) deserializeString(orderbook,"orderBook");
	}
	// Client will need to display account details, send buy/sell order, list of
	// account stock holdings, polling of stock price per interval (not necessarily
	// need polling, can be on update) when on that page, stock page orders.

	@SuppressWarnings({ "unchecked", "resource", "rawtypes" })
	private static ArrayList<?> deserializeString(String sb, String action) {
		final int[] index = { 0 };
		ArrayList deserializedList = null;
		try {
			if (action.equals("stock")) {
				deserializedList = (ArrayList<Stock>) new ObjectInputStream(new InputStream() {
					@Override
					public int read() throws IOException {
						return sb.charAt(index[0]++);
					}
				}).readObject();
			} else if (action.equals("completeOrders")) {
				deserializedList = (ArrayList<MarketComplete>) new ObjectInputStream(new InputStream() {
					@Override
					public int read() throws IOException {
						return sb.charAt(index[0]++);
					}
				}).readObject();
			} else if (action.equals("pendingOrders"))  {
				// action is pendingOrders
				deserializedList = (ArrayList<MarketPending>) new ObjectInputStream(new InputStream() {
					@Override
					public int read() throws IOException {
						return sb.charAt(index[0]++);
					}
				}).readObject();
			} else if (action.equals("orderBook"))  {
				// action is pendingOrders
				deserializedList = (ArrayList<OrderBook>) new ObjectInputStream(new InputStream() {
					@Override
					public int read() throws IOException {
						return sb.charAt(index[0]++);
					}
				}).readObject();
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return deserializedList;
	}

	public void addLabel(JPanel panel, String label, int gridx, int gridy, Insets insets) {
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel lbl = new JLabel(label);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.insets = insets;
        panel.add(lbl, gbc);
	}
	
	public void addLabel(JTabbedPane panel, String label, int gridx, int gridy, Insets insets) {
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel lbl = new JLabel(label);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.insets = insets;
        panel.add(lbl, gbc);
	}

	public void addTextField(JPanel panel, JTextField txtField, int column, int gridx, int gridy) {
		GridBagConstraints gbc = new GridBagConstraints();
        txtField.setColumns(column);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = defaultInsets;
        panel.add(txtField, gbc);
	}

	public void switchPanel(JPanel panel) {
		frame.setContentPane(new JScrollPane(panel));
        frame.validate();
	}
	
	public void initialise() {
		frame = new JFrame("Stock");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
        frame.setBounds(100, 100, 800, 800);
		defaultInsets =  new Insets(0, 0, 10, 10);
		createLoginPanel();
		createHomePanel();
		chartPanel = new JPanel();
		switchPanel(loginPanel);
		currentPage = Page.OTHER;
		System.out.println("Done initialised!");
	}
	
	public void createLoginPanel() {
		GridBagConstraints gbc = new GridBagConstraints();
		loginPanel = new JPanel();
		loginPanel.setLayout(new GridBagLayout());
		addLabel(loginPanel, "Username :", 0, 0, defaultInsets);
		
		JTextField userNameTxt = new JTextField();
		addTextField(loginPanel, userNameTxt, 10, 1, 0);

		addLabel(loginPanel, "Password :", 0, 1, defaultInsets);

        JTextField passwordTxt = new JPasswordField();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = defaultInsets;
        loginPanel.add(passwordTxt, gbc);

        JLabel errorLbl = new JLabel("");
        errorLbl.setForeground(Color.RED);
        errorLbl.setHorizontalAlignment(JLabel.CENTER);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = defaultInsets;
        loginPanel.add(errorLbl, gbc);
        
        JButton btnLogin = new JButton("Login");
        Client c = this;
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//Check login
            	try {
	            	String resAccountDetails = remoteObj.getAccountDetailsByUsernameAndPW(c, userNameTxt.getText(), passwordTxt.getText());
	    			// convert json string to object
	            	switch(resAccountDetails) {
		            	case "not found":
		            		errorLbl.setText("Invalid Username or Password!");
		    				break;
		    			case "problem":
		            		errorLbl.setText("There's a problem while accessing the server. Please try again later.");
		    				break;
		    			default:
		                	//Redirect to homePanel
		    				ObjectMapper objectMapper = new ObjectMapper();
		    				accountDetailsObj = objectMapper.readValue(resAccountDetails, AccountDetails.class);
		                	switchPanel(homePanel);
	    			}
            	}catch (Exception ex) {
	    			System.out.format("Error obtaining remoteServer/remoteInterface from registry");
	    			ex.printStackTrace();
//	    			System.exit(1);
            	}
            }
        });
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = defaultInsets;
        loginPanel.add(btnLogin, gbc);
        gbc.gridwidth = 0;
	}

	public void createHomePanel() {
		homePanel = new JPanel();
		homePanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		JTabbedPane homePane = new JTabbedPane();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = defaultInsets;
        homePanel.add(homePane, gbc);
		JPanel dashboardPanel = new JPanel();
		addLabel(dashboardPanel, "", 0, 0, defaultInsets);
		JPanel SGPanel = createCountryPanel(Market.SG);
		JPanel HKPanel = createCountryPanel(Market.HK);
		JPanel USPanel = createCountryPanel(Market.US);
		homePane.add("Dashboard", dashboardPanel);
		homePane.add("SG Market", SGPanel);
		homePane.add("HK Market", HKPanel);
		homePane.add("US Market", USPanel);
		homePane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JPanel selected = (JPanel) homePane.getSelectedComponent();
				if(homePane.getSelectedIndex() == 1) {
					currentPage = Page.SG;
					selected = createCountryPanel(Market.SG);
				}
				else if(homePane.getSelectedIndex() == 2) {
					currentPage = Page.HK;
					selected = createCountryPanel(Market.HK);
				}
				else if(homePane.getSelectedIndex() == 3) {
					currentPage = Page.US;
					selected = createCountryPanel(Market.US);
				}
			}});
		JButton btnLogout = new JButton("Log out");
		btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//Redirect to homePanel
            	switchPanel(loginPanel);
				currentPage = Page.OTHER;
            }
        });
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 10, 10);
        gbc.anchor = GridBagConstraints.NORTH;
        homePanel.add(btnLogout, gbc);
	}
	
	@SuppressWarnings("unchecked")
	public JPanel createCountryPanel(Market market) {
		Insets insets = new Insets(0, 0, 10, 50);
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel JSPane = new JPanel();
		JSPane.setLayout(new GridBagLayout());
		addLabel(JSPane, "Company Name", 0, 0, insets);
		addLabel(JSPane, "Ticker Symbol", 1, 0, insets);
		addLabel(JSPane, "Value", 2, 0, insets);
		addLabel(JSPane, "Status", 3, 0, insets);
		try {
			String result = remoteObj.retrieveMarketCache(market.name(),this);

			// SG MARKET PAGE
			if (result.equals("empty")) {
				System.out.println("db does not have any row");
	
			} else if (result.equals("error fetching")) {
				System.out.println("had some issue fetching from server");
	
			} else {
				ArrayList<Stock> arrayListStocks = (ArrayList<Stock>) deserializeString(result,"stock");
				
				for(int i = 0; i < arrayListStocks.size(); i++) {
					Stock s = arrayListStocks.get(i);
					addLabel(JSPane, s.getCompanyName(), 0, i+1, insets);
					addLabel(JSPane, s.getTickerSymbol(), 1, i+1, insets);
					addLabel(JSPane, s.getCurrentValue()+"", 2, i+1, insets);
					if(s.isStatus()) {
						addLabel(JSPane, "open", 3, i+1, insets);
					}else
						addLabel(JSPane, "closed", 3, i+1, insets);
					JButton submitBtn = new JButton("Trade " + s.getTickerSymbol());
					submitBtn.addActionListener(new ActionListener() {
			            public void actionPerformed(ActionEvent e) {
			            	currentDisplayStockId = s.getStockId();
			            	currentDisplayMarket = market;
			            	chartPanel = createChartPanel();
			            	switchPanel(chartPanel);
							currentPage = Page.STOCK;
			            }
			        });
			        gbc.fill = GridBagConstraints.HORIZONTAL;
			        gbc.gridx = 4;
			        gbc.gridy = i+1;
			        gbc.insets = defaultInsets;
			        JSPane.add(submitBtn, gbc);
				}
			}
			

		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return JSPane;
	}

//	public JPanel createChartPanel(
//			OHLCChart chart,
//			XChartPanel<OHLCChart> ohlcChart,
//			List<Date> xData,
//			List<Double> openData,
//			List<Double> highData,
//			List<Double> lowData,
//			List<Double> closeData) {
	public JPanel createChartPanel() {
		
//        frame.setBounds(100, 100, 1000, 900);
		Insets insets = new Insets(0, 0, 10, 50);
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel chartPanel = new JPanel();
		if(currentDisplayStockId != -1) {
			addLabel(chartPanel, currentDisplayStockId+"", 0, 0, defaultInsets);

			JPanel infoPanel = new JPanel();
			JTabbedPane orderPane = new JTabbedPane();
			JPanel buyOrderBookPanel = new JPanel();
			infoPanel.add("Buy Order Book", orderPane);
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.gridx = 0;
	        gbc.gridy = 0;
	        gbc.insets = defaultInsets;
			orderPane.add(new JScrollPane(buyOrderBookPanel), gbc);

			JPanel sellOrderBookPanel = new JPanel();
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.gridx = 1;
	        gbc.gridy = 0;
	        gbc.insets = defaultInsets;
			orderPane.add(new JScrollPane(sellOrderBookPanel), gbc);

			addLabel(buyOrderBookPanel, "Type", 0, 0, insets);
			addLabel(buyOrderBookPanel, "Quantity", 1, 0, insets);
			addLabel(buyOrderBookPanel, "Price", 2, 0, insets);
			
			addLabel(sellOrderBookPanel, "Type", 0, 0, insets);
			addLabel(sellOrderBookPanel, "Quantity", 1, 0, insets);
			addLabel(sellOrderBookPanel, "Price", 2, 0, insets);

			int buyOBCount = 0;
			int sellOBCount = 0;
			for(int i = 0; i < arrayListOrderBook.size(); i++) {
				OrderBook ob = arrayListOrderBook.get(i);
				if(ob.getType().equals("BUY")) {
					buyOBCount++;
					addLabel(buyOrderBookPanel, ob.getType(), 0, buyOBCount, insets);
					addLabel(buyOrderBookPanel, ob.getQuantity() + "", 1, buyOBCount, insets);
					addLabel(buyOrderBookPanel, ob.getPrice() + "", 2, buyOBCount, insets);
				}
				else {
					sellOBCount++;
					addLabel(sellOrderBookPanel, ob.getType(), 0, sellOBCount, insets);
					addLabel(sellOrderBookPanel, ob.getQuantity() + "", 1, sellOBCount, insets);
					addLabel(sellOrderBookPanel, ob.getPrice() + "", 2, sellOBCount, insets);
				}
			}
			JTabbedPane openOrderPane = new JTabbedPane();
			infoPanel.add("Open Orders", openOrderPane);
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.gridx = 0;
	        gbc.gridy = 1;
	        gbc.insets = defaultInsets;
			chartPanel.add(infoPanel,gbc);
		}
//		chartPanel.setLayout(new GridBagLayout());
//		// Create Chart
//		
//		// Customize Chart
//		chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
//		chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
//		
//		xData = null;
//		chart.addSeries("Series", xData, openData, highData, lowData, closeData);
//		chart.getStyler().setToolTipsEnabled(true);
//		ohlcChart =  new XChartPanel<OHLCChart>(chart);
//		JPanel ohlcPanel = ohlcChart;
//
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.insets = defaultInsets;
//		chartPanel.add(ohlcPanel, gbc);
		return chartPanel;
	}
	
	@SuppressWarnings({ "unchecked", "resource" })
	public static void main(String[] args) {
		System.out.println("client main method");

		try {
			RemoteInterface remoteObj = (RemoteInterface) Naming.lookup("rmi://localhost:1099/RemoteServer");
			ClientInt cc = new Client();

			// Interface for login
//			String username = "demo";
//			String pw = "password";
//			Scanner stdin = new Scanner(System.in); // Init scanner
//			String input = null;
//			String secondInput = null;
//
//			String resAccountDetails = remoteObj.getAccountDetailsByUsernameAndPW(cc, username, pw);
//			// convert json string to object
//			if (resAccountDetails.equals("not found")) {
//				System.out.println("your username does not exist. Try again.");
//
//			} else if (resAccountDetails.equals("problem")) {
//				System.out.println("there's a problem while accessing the server");
//
//			} else if (resAccountDetails.equals("wrong pw")) {
//				System.out.println("your pw is wrong");
//
//			} else {
//				// Successfully logged in cause all cases checked.
//				ObjectMapper objectMapper = new ObjectMapper();
//				StringBuilder sb = new StringBuilder();
//
//				// Unmarshall json string to object.
//				AccountDetails accountDetailsObj = objectMapper.readValue(resAccountDetails, AccountDetails.class);
//
//				//
//				System.out.println("AFTER GET ACCOUNT DETAILS!!" + accountDetailsObj);
//				// Assign accountId value to global variable so can use on other
//				// methods.
//				accountId = accountDetailsObj.getAccountId();
//				remoteObj.getAccountHoldingsById(accountId);

//				String returnedHkStocks = remoteObj.retrieveMarketCache("HK");
//				String returnedUSStocks = remoteObj.retrieveMarketCache("US");
//				String returnedSGkStocks = remoteObj.retrieveMarketCache("SG");
//
//				// HK MARKET PAGE
//				if (returnedHkStocks.equals("empty")) {
//					System.out.println("db does not have any row");
//
//				} else if (returnedHkStocks.equals("error fetching")) {
//					System.out.println("had some issue fetching from server");
//
//				} else {
//					ArrayList<Stock> arrayListHkStocks = (ArrayList<Stock>) deserializeString(returnedHkStocks,
//							"stock");
////					System.out.println("after deserialize HK");
////					System.out.println(arrayListHkStocks.toString());
//
//				}
//
//				// US MARKET PAGE
//				if (returnedUSStocks.equals("empty")) {
//					System.out.println("db does not have any row");
//
//				} else if (returnedUSStocks.equals("error fetching")) {
//					System.out.println("had some issue fetching from server");
//
//				} else {
//					ArrayList<Stock> arrayListUSStocks = (ArrayList<Stock>) deserializeString(returnedUSStocks,
//							"stock");
////					System.out.println("after deserialize US");
////					System.out.println(arrayListUSStocks.toString());
//
//				}
//
//				// SG MARKET PAGE
//				if (returnedSGkStocks.equals("empty")) {
//					System.out.println("db does not have any row");
//
//				} else if (returnedSGkStocks.equals("error fetching")) {
//					System.out.println("had some issue fetching from server");
//
//				} else {
//					ArrayList<Stock> arrayListSGStocks = (ArrayList<Stock>) deserializeString(returnedSGkStocks,
//							"stock");
////					System.out.println("after deserialize SG ");
////					System.out.println(arrayListSGStocks.toString());
//
//				}

				// Below methods are not implemented and are subjected to change.
				// When go into a stock page by itself:
				// retrieve order list per stock, retrieve price per stock

//				String stockOrderList = remoteObj.retrievePendingOrders(accountId, "SG", 4); // Retrieve By StockId
//				String orderCompleted = remoteObsj.retrieveCompletedOrders(accountId, "SG", 4); // Retrieve By StockId
//				if (stockOrderList.equals("empty")) {
//
//				} else if (stockOrderList.equals("error fetching")) {
//
//				} else {
//					ArrayList<MarketPending> arrayListPendingOrders = (ArrayList<MarketPending>) deserializeString(
//							stockOrderList, "pendingOrders");
//
////					System.out.println("PENDING ORDERS ");
////					System.out.println(arrayListPendingOrders.toString());
//
//				}
//
//				if (orderCompleted.equals("empty")) {
//
//				} else if (orderCompleted.equals("error fetching")) {
//
//				} else {
//
//					ArrayList<MarketComplete> arrayListCompleteOrders = (ArrayList<MarketComplete>) deserializeString(
//							orderCompleted, "completeOrders");
////					System.out.println("COMPLETED ORDERS");
////					System.out.println(arrayListCompleteOrders.toString());
//
//				}

				// Send Order
				// Please send in format (accountId, "US", "StockId, SellerId, BuyerId, Qty,
				// Price")
//				remoteObj.sendOrder(2, "HK", "5,-1,2,100,23.3", false); // -1 to indicate null, i will change to null on
//																	// backend.
//
//				// Exit
//				remoteObj.removeFromClientHashMap(accountId);
//			}

		} catch (Exception e) {
			System.out.format("Error obtaining remoteServer/remoteInterface from registry");
			e.printStackTrace();
			System.exit(1);
		}
	}
}