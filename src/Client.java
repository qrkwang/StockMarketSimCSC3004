import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Client extends java.rmi.server.UnicastRemoteObject implements ClientInt {

	public Client() throws RemoteException {

	}

	// Client will need to display account details, send buy/sell order, list of
	// account stock holdings, polling of stock price per interval (not necessarily
	// need polling, can be on update) when on that page.

	public static void main(String[] args) {
		System.out.println("client main method");

		try {
			RemoteInterface remoteObj = (RemoteInterface) Naming.lookup("rmi://localhost:1099/RemoteServer");

			Scanner stdin = new Scanner(System.in); // Init scanner
			String input = null;
			String secondInput = null;

			remoteObj.testMethod("test method");

		} catch (Exception e) {
			System.out.format("Error obtaining remoteServer/remoteInterface from registry");
			e.printStackTrace();
			System.exit(1);
		}
	}
}