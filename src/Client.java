import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

import classes.AccountDetails;
import classes.MarketComplete;
import classes.MarketPending;
import classes.Stock;

public class Client extends java.rmi.server.UnicastRemoteObject implements ClientInt {
	public Client() throws RemoteException {

	}

	private static int accountId;

	// Client will need to display account details, send buy/sell order, list of
	// account stock holdings, polling of stock price per interval (not necessarily
	// need polling, can be on update) when on that page, stock page orders.

	@SuppressWarnings({ "unchecked", "resource", "rawtypes" })
	private static ArrayList<?> deserializeString(String sb, String action) {
		final int[] index = { 0 };
		ArrayList deserializedList = null;

		if (action.equals("stock")) {
			try {
				deserializedList = (ArrayList<Stock>) new ObjectInputStream(new InputStream() {
					@Override
					public int read() throws IOException {
						return sb.charAt(index[0]++);
					}
				}).readObject();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (action.equals("completeOrders")) {
			try {
				deserializedList = (ArrayList<MarketComplete>) new ObjectInputStream(new InputStream() {
					@Override
					public int read() throws IOException {
						return sb.charAt(index[0]++);
					}
				}).readObject();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// action is pendingOrders
			try {
				deserializedList = (ArrayList<MarketPending>) new ObjectInputStream(new InputStream() {
					@Override
					public int read() throws IOException {
						return sb.charAt(index[0]++);
					}
				}).readObject();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return deserializedList;
	}

	@SuppressWarnings({ "unchecked", "resource" })
	public static void main(String[] args) {
		System.out.println("client main method");

		try {
			RemoteInterface remoteObj = (RemoteInterface) Naming.lookup("rmi://localhost:1099/RemoteServer");

			// Interface for login
			String username = "demo";
			String pw = "password";
			Scanner stdin = new Scanner(System.in); // Init scanner
			String input = null;
			String secondInput = null;

			System.out.println("before call method");
			String resAccountDetails = remoteObj.getAccountDetailsByUsernameAndPW(username, pw);
			System.out.println("after call method");
			System.out.println("result in client is " + resAccountDetails);
			// convert json string to object
			if (resAccountDetails.equals("not found")) {
				System.out.println("your username does not exist. Try again.");

			} else if (resAccountDetails.equals("problem")) {
				System.out.println("there's a problem while accessing the server");

			} else if (resAccountDetails.equals("wrong pw")) {
				System.out.println("your pw is wrong");

			} else {
				// Successfully logged in cause all cases checked.
				ObjectMapper objectMapper = new ObjectMapper();
				StringBuilder sb = new StringBuilder();

				// Unmarshall json string to object.
				AccountDetails accountDetailsObj = objectMapper.readValue(resAccountDetails, AccountDetails.class);

				// Assign accountId value to global variable so can use on other
				// methods.
				accountId = accountDetailsObj.getAccountId(); // not done yet
				remoteObj.getAccountHoldingsById(accountId);

				String returnedHkStocks = remoteObj.getAllStocksByMarket("HK");
				String returnedUSStocks = remoteObj.getAllStocksByMarket("US");
				String returnedSGkStocks = remoteObj.getAllStocksByMarket("SG");

				// HK MARKET PAGE
				if (returnedHkStocks.equals("empty")) {
					System.out.println("db does not have any row");

				} else if (returnedHkStocks.equals("error fetching")) {
					System.out.println("had some issue fetching from server");

				} else {
					ArrayList<Stock> arrayListHkStocks = (ArrayList<Stock>) deserializeString(returnedHkStocks,
							"stock");
					System.out.println("after deserialize HK");
					System.out.println(arrayListHkStocks.toString());

				}

				// US MARKET PAGE
				if (returnedUSStocks.equals("empty")) {
					System.out.println("db does not have any row");

				} else if (returnedUSStocks.equals("error fetching")) {
					System.out.println("had some issue fetching from server");

				} else {
					ArrayList<Stock> arrayListUSStocks = (ArrayList<Stock>) deserializeString(returnedUSStocks,
							"stock");
					System.out.println("after deserialize US");
					System.out.println(arrayListUSStocks.toString());

				}

				// SG MARKET PAGE
				if (returnedSGkStocks.equals("empty")) {
					System.out.println("db does not have any row");

				} else if (returnedSGkStocks.equals("error fetching")) {
					System.out.println("had some issue fetching from server");

				} else {
					ArrayList<Stock> arrayListSGStocks = (ArrayList<Stock>) deserializeString(returnedSGkStocks,
							"stock");
					System.out.println("after deserialize SG ");
					System.out.println(arrayListSGStocks.toString());

				}

				// Below methods are not implemented and are subjected to change.
				// When go into a stock page by itself:
				// retrieve order list per stock, retrieve price per stock

				String stockOrderList = remoteObj.retrievePendingOrders("SG", 4); // Retrieve By StockId
				String orderCompleted = remoteObj.retrieveCompletedOrders("SG", 4); // Retrieve By StockId
				if (stockOrderList.equals("empty")) {

				} else if (stockOrderList.equals("error fetching")) {

				} else {
					ArrayList<MarketPending> arrayListPendingOrders = (ArrayList<MarketPending>) deserializeString(
							stockOrderList, "pendingOrders");

					System.out.println("PENDING ORDERS ");
					System.out.println(arrayListPendingOrders.toString());

				}

				if (orderCompleted.equals("empty")) {

				} else if (orderCompleted.equals("error fetching")) {

				} else {

					ArrayList<MarketComplete> arrayListCompleteOrders = (ArrayList<MarketComplete>) deserializeString(
							orderCompleted, "completeOrders");
					System.out.println("COMPLETED ORDERS");
					System.out.println(arrayListCompleteOrders.toString());

				}
			}

			// Send Order
			// Please send in format ("US", "StockId", "SellerId", "BuyerId", "Qty",
			// "Price"), or u can just do true/false on 3rd parameter.
			remoteObj.sendOrder(1, "US,1,5,0,100,23.3"); // 0 to indicate null, i will change to null on backend.

		} catch (Exception e) {
			System.out.format("Error obtaining remoteServer/remoteInterface from registry");
			e.printStackTrace();
			System.exit(1);
		}
	}
}