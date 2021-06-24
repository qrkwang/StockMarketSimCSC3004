import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

import classes.AccountDetails;

public class Client extends java.rmi.server.UnicastRemoteObject implements ClientInt {
	public Client() throws RemoteException {

	}

	private static int accountId;

	// Client will need to display account details, send buy/sell order, list of
	// account stock holdings, polling of stock price per interval (not necessarily
	// need polling, can be on update) when on that page, stock page orders.

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
				// Unmarshall json string to object.
				AccountDetails accountDetailsObj = objectMapper.readValue(resAccountDetails, AccountDetails.class);

				// Assign accountId value to global variable so can use on other
				// methods.
				accountId = accountDetailsObj.getAccountId();
				remoteObj.getAccountHoldingsById(accountId);

				remoteObj.getAllStocksByMarket("US"); // return array list of stock object
				remoteObj.getAllStocksByMarket("HK");
				remoteObj.getAllStocksByMarket("SG");

				// When go into a stock page by itself:
				// retrieve order list per stock, retrieve price per stock
				String stockPrice = remoteObj.retrievePrice("SG", "5"); // Retrieve By StockId

				ArrayList stockOrderList = remoteObj.retrieveOrders("SG", "4"); // Retrieve By StockId

				String stockPrice1 = remoteObj.retrievePrice("US", "3"); // Retrieve By StockId
				ArrayList stockOrderList1 = remoteObj.retrieveOrders("US", "2"); // Retrieve By StockId
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