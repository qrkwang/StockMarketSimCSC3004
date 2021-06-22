import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

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
			Scanner stdin = new Scanner(System.in); // Init scanner
			String input = null;
			String secondInput = null;

			String accountDetails = remoteObj.getAccountDetailsByUsername(username);
			// Will receive a string with account details delimited by "," . First item is
			// accountId. Assign accountId value to global variable so can use on other
			// methods.
			String[] splitArray = accountDetails.split(",");
			accountId = Integer.parseInt(splitArray[0]);
			remoteObj.getAccountHoldingsById(accountId);

			// getAllStocks(US), return array list of object?, stock id needed for later use
			// too.
			// getAllStocks(SG), return array list of object?, stock id needed for later use
			// too.
			// getAllStocks(HK), return array list of object?, stock id needed for later use
			// too.

			// When go into a stock page by itself:
			// retrieve order list per stock, retrieve price per stock
			String stockPrice = remoteObj.retrievePrice("SG", "SGX:A17U"); // need retrieve price per whatever interval
																			// or on update

			ArrayList stockOrderList = remoteObj.retrieveOrders("SG", "SGX:A17U"); // second parameter can enter be
																					// tickerSymbol / stockId, for now is tickerSymbol

			String stockPrice1 = remoteObj.retrievePrice("US", "NASDAQ:AAPL");
			ArrayList stockOrderList1 = remoteObj.retrieveOrders("US", "NASDAQ:AAPL");

			// When want send order.
			remoteObj.sendOrder(1, "testtest");

		} catch (Exception e) {
			System.out.format("Error obtaining remoteServer/remoteInterface from registry");
			e.printStackTrace();
			System.exit(1);
		}
	}
}