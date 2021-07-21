import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knowm.xchart.OHLCChart;
import org.knowm.xchart.OHLCChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler;

import com.fasterxml.jackson.databind.ObjectMapper;

import classes.AccountDetails;
import classes.MarketComplete;
import classes.MarketPending;
import classes.OrderBook;
import classes.Stock;
import classes.StockOwned;

public class Client extends java.rmi.server.UnicastRemoteObject implements ClientInt {
	private RemoteInterface remoteObj;
	private JFrame frame;
	private JPanel loginPanel;
	private JPanel homePanel;
	private JPanel chartPanel;
	private AccountDetails accountDetailsObj;
	private ArrayList<StockOwned> accountHoldings;
	private final Insets DEFAULTINSETS = new Insets(0, 0, 10, 10);
	private final Insets LISTINSETS = new Insets(0, 0, 10, 50);

	private enum Market {
		SG, HK, US
	}

	private enum Page {
		SG, HK, US, STOCK, OTHER
	};
	//Variable to keep track of user current navigation through the application
	private int currentDisplayStockId;
	private int ownedQuantity;
	private String currentDisplayCompanyName;
	private String currentDisplayTickerSymbol;
	private Market currentDisplayMarket;
	private Page currentPage;

	public Client() throws RemoteException {
		try {
			//Retrieve the remote server
			remoteObj = (RemoteInterface) Naming.lookup("rmi://localhost:1099/RemoteServer");
			//Initialise variable
			currentDisplayStockId = -1;
			ownedQuantity = -1;
			accountDetailsObj = null;
			accountHoldings = null;
			currentDisplayCompanyName = "";
			currentDisplayTickerSymbol = "";
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

	// Unicast from server to client for notification.
	public void printToClient(String message) throws java.rmi.RemoteException {

		notificationPopUp(message);
	}
	//Notification pop up frame
	public void notificationPopUp(String message) {
		GridBagConstraints gbc = new GridBagConstraints();
		JFrame notificationFrame = new JFrame();
		notificationFrame.setBounds(450, 350, 500, 200);
		notificationFrame.setVisible(true);
		JPanel notificationPanel = new JPanel();
		notificationPanel.setLayout(new GridBagLayout());
		addLabel(notificationPanel, message, 0, 0, DEFAULTINSETS);
		JButton btnClose = new JButton("Close");

		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Send buy order
				notificationFrame.dispose();
			}
		});
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 10, 10);
		notificationPanel.add(btnClose, gbc);
		notificationFrame.add(notificationPanel);
	}
	
	//Callback to trigger market update
	public void updateMarket(String market) throws java.rmi.RemoteException {
		String m = currentPage.name();
		if (m.equals(market)) {
			JTabbedPane mPane = (JTabbedPane) homePanel.getComponent(0);
			JPanel panel = null;
			switch (currentPage) {
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
			frame.revalidate();
		}
	}
	
	//Callback trigger upon a completed transaction
	//Trigger the update of order book, account details and holdings, and market details
	//Update the owned quantity based on the stock bought/sold
	public void updateOrderBook(String market, int stockId, boolean bought, boolean sold, int quantity) throws java.rmi.RemoteException {
		//Update the stock overview panel upon completed transaction
		if (currentPage.name().equals("STOCK") && currentDisplayMarket.name().equals(market)
				&& stockId == currentDisplayStockId) {
			if(bought) {
				if(ownedQuantity == -1)
					ownedQuantity = quantity;
				else
					ownedQuantity += quantity;
			}
			if(sold) {
				ownedQuantity -= quantity;
				if(ownedQuantity == 0)
					ownedQuantity = -1;
			}
			remoteObj.cacheOrderBook(market, stockId);
			remoteObj.cacheMarket();
			createChartPanel(false);
			switchPanel(chartPanel);
		}
		//Update the account details and holding, and market details
		//Refresh the homePanel
		try {
			String resAccountDetails = remoteObj.getAccountDetailsById(this, accountDetailsObj.getAccountId());
			// convert json string to object
			switch(resAccountDetails) {
			case "not found":
				notificationPopUp("getAccountDetailsById not found");
				break;
			case "problem":
				notificationPopUp("getAccountDetailsById encountered a problem");
				break;
			default:
				// Update to homePanel
				ObjectMapper objectMapper = new ObjectMapper();
				accountDetailsObj = objectMapper.readValue(resAccountDetails, AccountDetails.class);
				accountHoldings = remoteObj.getAccountHoldingsById(accountDetailsObj.getAccountId());
				createHomePanel();
			}
		} catch (Exception ex) {
			System.out.format("Error obtaining remoteServer/remoteInterface from registry");
			ex.printStackTrace();
		}
	}

	//Callback to trigger stock overview update
	public void updateStock(String market, int stockId) throws java.rmi.RemoteException {
		if (currentPage.name().equals("STOCK") && currentDisplayMarket.name().equals(market)
				&& stockId == currentDisplayStockId) {
			createChartPanel(true);
			switchPanel(chartPanel);
		}
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
			} else if (action.equals("pendingOrders")) {
				// action is pendingOrders
				deserializedList = (ArrayList<MarketPending>) new ObjectInputStream(new InputStream() {
					@Override
					public int read() throws IOException {
						return sb.charAt(index[0]++);
					}
				}).readObject();
			} else if (action.equals("orderBook")) {
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

	//Method to add label to panel
	public void addLabel(JPanel panel, String label, int gridx, int gridy, Insets insets) {
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel lbl = new JLabel(label);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.insets = insets;
		panel.add(lbl, gbc);
	}

	//Method to add label to JTabbedPane
	public void addLabel(JTabbedPane panel, String label, int gridx, int gridy, Insets insets) {
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel lbl = new JLabel(label);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.insets = insets;
		panel.add(lbl, gbc);
	}

	//Method to add text field to panel
	public void addTextField(JPanel panel, JTextField txtField, int column, int gridx, int gridy, Insets insets) {
		GridBagConstraints gbc = new GridBagConstraints();
		txtField.setColumns(column);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.insets = insets;
		panel.add(txtField, gbc);
	}

	//Change the panel displayed to the user
	public void switchPanel(JPanel panel) {
		frame.setContentPane(new JScrollPane(panel));
		frame.validate();
		panel.setFocusable(true);
		panel.requestFocusInWindow();
	}

	//Initialise UI variables
	public void initialise() {
		frame = new JFrame("Stock");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setBounds(100, 100, 1200, 800);
		createLoginPanel();
		createHomePanel();
		chartPanel = new JPanel();
		switchPanel(loginPanel);
		currentPage = Page.OTHER;
		////System.out.println("Done initialised!");
	}

	//Create the Log in panel
	public void createLoginPanel() {
		GridBagConstraints gbc = new GridBagConstraints();
		loginPanel = new JPanel();
		loginPanel.setLayout(new GridBagLayout());
		addLabel(loginPanel, "Username :", 0, 0, DEFAULTINSETS);

		JTextField userNameTxt = new JTextField();
		addTextField(loginPanel, userNameTxt, 10, 1, 0, DEFAULTINSETS);

		addLabel(loginPanel, "Password :", 0, 1, DEFAULTINSETS);

		JTextField passwordTxt = new JPasswordField();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = DEFAULTINSETS;
		loginPanel.add(passwordTxt, gbc);

		JLabel errorLbl = new JLabel("");
		errorLbl.setForeground(Color.RED);
		errorLbl.setHorizontalAlignment(JLabel.CENTER);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.insets = DEFAULTINSETS;
		loginPanel.add(errorLbl, gbc);

		//Button to trigger log in check
		JButton btnLogin = new JButton("Login");
		Client c = this;
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkLogin(userNameTxt, passwordTxt, errorLbl, c);
			}
		});
		//Trigger log in check upon pressing "Enter"
		userNameTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == 10) {
					checkLogin(userNameTxt, passwordTxt, errorLbl, c);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
		passwordTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == 10) {
					checkLogin(userNameTxt, passwordTxt, errorLbl, c);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
		//Focus on the user name textbox upon displaying the panel
		loginPanel.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				userNameTxt.setFocusable(true);
				userNameTxt.requestFocus();
			}
		});
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.insets = DEFAULTINSETS;
		loginPanel.add(btnLogin, gbc);
		gbc.gridwidth = 0;
	}

	//Log in check
	public void checkLogin(JTextField userNameTxt, JTextField passwordTxt, JLabel errorLbl, Client c) {
		try {
			//Retrieve result from server
			String resAccountDetails = remoteObj.getAccountDetailsByUsernameAndPW(c, userNameTxt.getText(),
					passwordTxt.getText());
			//Check result
			//Show error message using error label
			switch (resAccountDetails) {
			case "not found":
				errorLbl.setText("Invalid Username or Password!");
				break;
			case "problem":
				errorLbl.setText("There's a problem while accessing the server. Please try again later.");
				break;
			default:
				// Log in success
				//Retrieve account details and holdings
				//Redirect to home panel
				ObjectMapper objectMapper = new ObjectMapper();
				accountDetailsObj = objectMapper.readValue(resAccountDetails, AccountDetails.class);
				accountHoldings = remoteObj.getAccountHoldingsById(accountDetailsObj.getAccountId());
				if (accountHoldings == null) {
					System.out.println("sql exception");
				}
				createHomePanel();
				switchPanel(homePanel);
			}
		} catch (Exception ex) {
			System.out.format("Error obtaining remoteServer/remoteInterface from registry");
			ex.printStackTrace();
		}
	}
	
	//Log out
	public void logout(){
		//Remove client from the server record
		try {
			remoteObj.removeFromClientHashMap(accountDetailsObj.getAccountId());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Redirect to login panel
		switchPanel(loginPanel);
		currentPage = Page.OTHER;
	}

	//Create the home panel
	public void createHomePanel() {
		homePanel = new JPanel();
		homePanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		JTabbedPane homePane = new JTabbedPane();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = DEFAULTINSETS;
		homePanel.add(homePane, gbc);
		JPanel dashboardPanel = new JPanel();
		dashboardPanel.setLayout(new GridBagLayout());
		//Display the account holding
		if (accountDetailsObj != null) {
			addLabel(dashboardPanel, accountDetailsObj.getUserName(), 0, 0, DEFAULTINSETS);
			addLabel(dashboardPanel, "Total Account Value: $" + accountDetailsObj.getTotalAccountValue(), 1, 0,
					DEFAULTINSETS);
			addLabel(dashboardPanel, "Available Cash: $" + accountDetailsObj.getAvailableCash(), 2, 0, DEFAULTINSETS);
			addLabel(dashboardPanel, "Securities Value: $" + accountDetailsObj.getTotalSecurityValue(), 3, 0,
					DEFAULTINSETS);

			JTabbedPane holdingPane = new JTabbedPane();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 4;
			gbc.insets = DEFAULTINSETS;
			dashboardPanel.add(holdingPane, gbc);
			gbc.gridwidth = 1;

			JPanel SGHoldingPanel = new JPanel();
			JPanel HKHoldingPanel = new JPanel();
			JPanel USHoldingPanel = new JPanel();
			SGHoldingPanel.setLayout(new GridBagLayout());
			HKHoldingPanel.setLayout(new GridBagLayout());
			USHoldingPanel.setLayout(new GridBagLayout());
			holdingPane.add("SG Holding", SGHoldingPanel);
			holdingPane.add("HK Holding", HKHoldingPanel);
			holdingPane.add("US Holding", USHoldingPanel);

			addLabel(SGHoldingPanel, "Company Name", 0, 0, LISTINSETS);
			addLabel(SGHoldingPanel, "Ticker Symbol", 1, 0, LISTINSETS);
			addLabel(SGHoldingPanel, "Quantity", 2, 0, LISTINSETS);
			addLabel(SGHoldingPanel, "Average Price", 3, 0, LISTINSETS);

			addLabel(HKHoldingPanel, "Company Name", 0, 0, LISTINSETS);
			addLabel(HKHoldingPanel, "Ticker Symbol", 1, 0, LISTINSETS);
			addLabel(HKHoldingPanel, "Quantity", 2, 0, LISTINSETS);
			addLabel(HKHoldingPanel, "Average Price", 3, 0, LISTINSETS);

			addLabel(USHoldingPanel, "Company Name", 0, 0, LISTINSETS);
			addLabel(USHoldingPanel, "Ticker Symbol", 1, 0, LISTINSETS);
			addLabel(USHoldingPanel, "Quantity", 2, 0, LISTINSETS);
			addLabel(USHoldingPanel, "Average Price", 3, 0, LISTINSETS);

			int sgCount = 1;
			int hkCount = 1;
			int usCount = 1;
			if (accountHoldings != null) {
				for (StockOwned so : accountHoldings) {
					if (so.getMarket().equals(Market.SG.name())) {
						setUpHoldingRow(SGHoldingPanel, Market.SG, so.getStockId(), so.getCompanyName(),
								so.getTickerSymbol(), so.getQuantity(), so.getAvgPrice(), sgCount);
						sgCount++;
					} else if (so.getMarket().equals(Market.HK.name())) {
						setUpHoldingRow(HKHoldingPanel, Market.HK, so.getStockId(), so.getCompanyName(),
								so.getTickerSymbol(), so.getQuantity(), so.getAvgPrice(), hkCount);
						hkCount++;
					} else if (so.getMarket().equals(Market.US.name())) {
						setUpHoldingRow(USHoldingPanel, Market.US, so.getStockId(), so.getCompanyName(),
								so.getTickerSymbol(), so.getQuantity(), so.getAvgPrice(), usCount);
						usCount++;
					}
				}
			}

		}

		//Display the markets' stocks details
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
				if (homePane.getSelectedIndex() == 1) {
					currentPage = Page.SG;
					selected = createCountryPanel(Market.SG);
				} else if (homePane.getSelectedIndex() == 2) {
					currentPage = Page.HK;
					selected = createCountryPanel(Market.HK);
				} else if (homePane.getSelectedIndex() == 3) {
					currentPage = Page.US;
					selected = createCountryPanel(Market.US);
				}
			}
		});
		JButton btnLogout = new JButton("Log out");
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logout();
			}
		});
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new Insets(20, 0, 10, 10);
		gbc.anchor = GridBagConstraints.NORTH;
		homePanel.add(btnLogout, gbc);
	}

	//Create each row of the account holdings
	public void setUpHoldingRow(JPanel holdingPanel, Market market, int stockId, String companyName,
			String tickerSymbol, int quantity, float avgPrice, int count) {
		GridBagConstraints gbc = new GridBagConstraints();
		addLabel(holdingPanel, companyName, 0, count, LISTINSETS);
		addLabel(holdingPanel, tickerSymbol, 1, count, LISTINSETS);
		addLabel(holdingPanel, quantity + "", 2, count, LISTINSETS);
		addLabel(holdingPanel, avgPrice + "", 3, count, LISTINSETS);
		JButton submitBtn = new JButton("View " + tickerSymbol);
		submitBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ownedQuantity = quantity;
				currentDisplayStockId = stockId;
				currentDisplayMarket = market;
				currentDisplayCompanyName = companyName;
				currentDisplayTickerSymbol = tickerSymbol;
				createChartPanel(true);
				currentPage = Page.STOCK;
				switchPanel(chartPanel);
			}
		});
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 4;
		gbc.gridy = count;
		gbc.insets = DEFAULTINSETS;
		holdingPanel.add(submitBtn, gbc);
	}

	//Create each markets' stocks details
	public JPanel createCountryPanel(Market market) {
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel JSPane = new JPanel();
		JSPane.setLayout(new GridBagLayout());
		addLabel(JSPane, "Company Name", 0, 0, LISTINSETS);
		addLabel(JSPane, "Ticker Symbol", 1, 0, LISTINSETS);
		addLabel(JSPane, "Value", 2, 0, LISTINSETS);
		addLabel(JSPane, "Status", 3, 0, LISTINSETS);
		try {
			String result = remoteObj.retrieveMarketCache(market.name(), this);

			// SG MARKET PAGE
			if (result.equals("empty")) {
				System.out.println("db does not have any row");
			} else if (result.equals("error fetching")) {
				System.out.println("had some issue fetching from server");
			} else {
				ArrayList<Stock> arrayListStocks = (ArrayList<Stock>) deserializeString(result, "stock");

				for (int i = 0; i < arrayListStocks.size(); i++) {
					Stock s = arrayListStocks.get(i);
					addLabel(JSPane, s.getCompanyName(), 0, i + 1, LISTINSETS);
					addLabel(JSPane, s.getTickerSymbol(), 1, i + 1, LISTINSETS);
					addLabel(JSPane, s.getCurrentValue() + "", 2, i + 1, LISTINSETS);
					if (s.isStatus()) {
						addLabel(JSPane, "open", 3, i + 1, LISTINSETS);
					} else
						addLabel(JSPane, "closed", 3, i + 1, LISTINSETS);
					JButton submitBtn = new JButton("Trade " + s.getTickerSymbol());
					submitBtn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							checkOwnHolding(market, s.getStockId());
							currentDisplayStockId = s.getStockId();
							currentDisplayCompanyName = s.getCompanyName();
							currentDisplayTickerSymbol = s.getTickerSymbol();
							currentDisplayMarket = market;
							createChartPanel(true);
							currentPage = Page.STOCK;
							switchPanel(chartPanel);
						}
					});
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridx = 4;
					gbc.gridy = i + 1;
					gbc.insets = DEFAULTINSETS;
					JSPane.add(submitBtn, gbc);
				}
			}

		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return JSPane;
	}

	//Check if client own the stock
	public void checkOwnHolding(Market market, int stockId) {
		boolean found = false;
		for (StockOwned so : accountHoldings) {
			if (so.getMarket().equals(market.name()) && so.getStockId() == stockId) {
				ownedQuantity = so.getQuantity();
				found = true;
				break;
			}
		}
		if (!found)
			ownedQuantity = -1;
	}

	//Create the stock overview panel
	public void createChartPanel(boolean callback) {
		//Initialise variable for the ohlc chart using knowm xchart
		OHLCChart chart = new OHLCChartBuilder().width(800).height(600).title(currentDisplayTickerSymbol + " - " + currentDisplayCompanyName).build();
		List<Date> xData = new ArrayList<Date>();
		List<Float> openData = new ArrayList<Float>();
		List<Float> highData = new ArrayList<Float>();
		List<Float> lowData = new ArrayList<Float>();
		List<Float> closeData = new ArrayList<Float>();
		JPanel buyOrderBookPanel = new JPanel();
		buyOrderBookPanel.setLayout(new GridBagLayout());
		JPanel sellOrderBookPanel = new JPanel();
		sellOrderBookPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		chartPanel = new JPanel();
		chartPanel.setLayout(new GridBagLayout());
		if (currentDisplayStockId != -1) {
			try {
				ArrayList<MarketComplete> arrayListCompleteOrders = null;
				HashMap<String, String> res = remoteObj.retrieveStockCache(currentDisplayMarket.name(), currentDisplayStockId,
						this, callback);
				String orderCompleted = res.get("orderCompleted");
				if (orderCompleted.equals("empty")) {
					System.out.println("db does not have any row");
				} else if (orderCompleted.equals("error fetching")) {
					System.out.println("had some issue fetching from server");
				} else {
					//Retrieve stock's history
					//Populate the chart data
					arrayListCompleteOrders = (ArrayList<MarketComplete>) deserializeString(orderCompleted,"completeOrders");
					if (arrayListCompleteOrders.size() > 0) {
						MarketComplete firstMC = arrayListCompleteOrders.get(0);
						openData.add(firstMC.getPrice());
						highData.add(firstMC.getPrice());
						lowData.add(firstMC.getPrice());
						LocalDateTime currentDT = firstMC.getTransactionDate().truncatedTo(ChronoUnit.MINUTES)
								.plusMinutes(1);
						xData.add(java.sql.Timestamp.valueOf(currentDT));
						float latestClose = firstMC.getPrice();
						for (int i = 1; i < arrayListCompleteOrders.size(); i++) {
							MarketComplete mc = arrayListCompleteOrders.get(i);
							LocalDateTime minuteRoundCeiling = mc.getTransactionDate().truncatedTo(ChronoUnit.MINUTES)
									.plusMinutes(1);
							if (!currentDT.equals(minuteRoundCeiling)) {
								xData.add(java.sql.Timestamp.valueOf(currentDT));
								closeData.add(latestClose);
								openData.add(latestClose);
								highData.add(latestClose);
								lowData.add(latestClose);

							}
							currentDT = minuteRoundCeiling;
							latestClose = mc.getPrice();
						}
						closeData.add(latestClose);
						// Customize Chart
						chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
						chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
						chart.getStyler().setToolTipsEnabled(true);
						// Add data to the chart
						chart.addSeries("Series", xData, openData, highData, lowData, closeData).setUpColor(Color.GREEN)
								.setDownColor(Color.RED);
					}

				}
				//Retrieve order book from the server
				//Populate the order book
				String orderbook = res.get("orderBook");
				if(!orderbook.equals("empty") && !orderbook.equals("error fetching")) {
					ArrayList<OrderBook> arrayListOrderBook = (ArrayList<OrderBook>) deserializeString(orderbook, "orderBook");
					int buyOBCount = 1;
					int sellOBCount = 1;
					for (int i = 0; i < arrayListOrderBook.size(); i++) {
						OrderBook ob = arrayListOrderBook.get(i);
						if (ob.getType().equals("BUY")) {
							addLabel(buyOrderBookPanel, ob.getType(), 0, buyOBCount, LISTINSETS);
							addLabel(buyOrderBookPanel, ob.getQuantity() + "", 1, buyOBCount, LISTINSETS);
							addLabel(buyOrderBookPanel, ob.getPrice() + "", 2, buyOBCount, LISTINSETS);
							buyOBCount++;
						} else {
							addLabel(sellOrderBookPanel, ob.getType(), 0, sellOBCount, LISTINSETS);
							addLabel(sellOrderBookPanel, ob.getQuantity() + "", 1, sellOBCount, LISTINSETS);
							addLabel(sellOrderBookPanel, ob.getPrice() + "", 2, sellOBCount, LISTINSETS);
							sellOBCount++;
						}
					}
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Create ohlc chart UI
			XChartPanel<OHLCChart> ohlcChart = new XChartPanel<OHLCChart>(chart);
			JPanel ohlcPanel = ohlcChart;
			ohlcChart.repaint();
			frame.repaint();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = DEFAULTINSETS;
			chartPanel.add(ohlcPanel, gbc);
			
			//Create the order book UI
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new GridBagLayout());
			JTabbedPane infoPane = new JTabbedPane();
			JPanel orderPanel = new JPanel();
			orderPanel.add(new JScrollPane(buyOrderBookPanel));
			orderPanel.add(new JScrollPane(sellOrderBookPanel));
			
			addLabel(buyOrderBookPanel, "Type", 0, 0, LISTINSETS);
			addLabel(buyOrderBookPanel, "Quantity", 1, 0, LISTINSETS);
			addLabel(buyOrderBookPanel, "Price", 2, 0, LISTINSETS);

			addLabel(sellOrderBookPanel, "Type", 0, 0, LISTINSETS);
			addLabel(sellOrderBookPanel, "Quantity", 1, 0, LISTINSETS);
			addLabel(sellOrderBookPanel, "Price", 2, 0, LISTINSETS);
			
			infoPane.add("Order Book", orderPanel);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = DEFAULTINSETS;
			bottomPanel.add(infoPane, gbc);
			
			//Create button for buy
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			JButton btnBuy = new JButton("Buy Stock");
			btnBuy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Buy stock by setting the 3rd parameter to true
					buySellStockFrame(currentDisplayMarket.name(), currentDisplayStockId, true, 0);
				}
			});
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new Insets(0, 0, 10, 10);
			buttonPanel.add(btnBuy, gbc);
			
			//Create button for sell if got owned quantity
			if (ownedQuantity != -1) {
				JButton btnSell = new JButton("Sell Stock");
				btnSell.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// Sell stock by setting the 3rd parameter to false
						buySellStockFrame(currentDisplayMarket.name(), currentDisplayStockId, false, ownedQuantity);
					}
				});
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.insets = new Insets(0, 0, 10, 10);
				buttonPanel.add(btnSell, gbc);
			}
			
			//Navigation button
			JButton btnBack = new JButton("Back to Home");
			btnBack.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Redirect to homePanel
					switchPanel(homePanel);
					currentPage = Page.OTHER;
				}
			});
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.insets = new Insets(0, 0, 10, 10);
			buttonPanel.add(btnBack, gbc);

			JButton btnLogout = new JButton("Log out");
			btnLogout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					logout();
				}
			});
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.insets = new Insets(0, 0, 10, 10);
			buttonPanel.add(btnLogout, gbc);

			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.insets = DEFAULTINSETS;
			bottomPanel.add(buttonPanel, gbc);

			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = DEFAULTINSETS;
			chartPanel.add(bottomPanel, gbc);

			chartPanel.revalidate();
			chartPanel.repaint();
			frame.revalidate();
			frame.repaint();
		}
	}

	//Create buy/sell pop up frame
	public void buySellStockFrame(String market, int StockId, boolean buy, int max) {
		GridBagConstraints gbc = new GridBagConstraints();
		JFrame orderStockFrame = new JFrame();
		orderStockFrame.setBounds(100, 100, 550, 550);
		orderStockFrame.setVisible(true);
		JPanel orderPanel = new JPanel();
		orderPanel.setLayout(new GridBagLayout());

		if (!buy) {
			addLabel(orderPanel, "Owned Quantiy: ", 0, 0, DEFAULTINSETS);
			addLabel(orderPanel, ownedQuantity + "", 1, 0, DEFAULTINSETS);
		}

		addLabel(orderPanel, "Quantity:", 0, 1, DEFAULTINSETS);

		SpinnerModel sm = new SpinnerNumberModel(0, 0, max, 1);
		//Remove max limit if buying
		if (buy)
			sm = new SpinnerNumberModel(0, 0, null, 1);
		JSpinner quantitySpinner = new JSpinner(sm);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = DEFAULTINSETS;
		orderPanel.add(quantitySpinner, gbc);

		addLabel(orderPanel, "Price:", 0, 2, DEFAULTINSETS);

		sm = new SpinnerNumberModel(0.0, 0.0, null, 0.1);
		JSpinner priceSpinner = new JSpinner(sm);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.insets = DEFAULTINSETS;
		orderPanel.add(priceSpinner, gbc);

		JButton btnSubmit = new JButton();
		if (buy)
			btnSubmit.setText("Buy");
		else
			btnSubmit.setText("Sell");

		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Retrieve user input from the textbox
				// Send order to server
				try {
					quantitySpinner.commitEdit();
					priceSpinner.commitEdit();
					int quantity = Integer.parseInt(quantitySpinner.getValue() + "");
					float price = Float.parseFloat(priceSpinner.getValue() + "");
					int accountId = accountDetailsObj.getAccountId();
					String order = "";
					if (buy)
						order = generateOrder(StockId, -1, accountId, quantity, price);
					else
						order = generateOrder(StockId, accountId, -1, quantity, price);
					remoteObj.sendOrder(accountId, market, order, false); 
					orderStockFrame.dispose();
				} catch (ParseException | RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.insets = new Insets(0, 0, 10, 10);
		orderPanel.add(btnSubmit, gbc);
		orderStockFrame.add(orderPanel);
	}

	// Generate the order for the server
	public String generateOrder(int StockId, int sellerId, int buyerId, int qty, float price) {
		return StockId + "," + sellerId + "," + buyerId + "," + qty + "," + price;
	}

	@SuppressWarnings({ "unchecked", "resource" })
	public static void main(String[] args) {
		try {
			//Create the client
			ClientInt cc = new Client();
		} catch (Exception e) {
			System.out.format("Error obtaining remoteServer/remoteInterface from registry");
			e.printStackTrace();
			System.exit(1);
		}
	}

}