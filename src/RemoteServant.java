import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RemoteServant extends UnicastRemoteObject implements RemoteInterface {
	public RemoteServant() throws RemoteException {
		super();
		System.out.format("Creating server object\n"); // Print to client that server object is being created once
														// constructor called.
	}

	@Override
	public String testMethod(String s) throws RemoteException {
		System.out.println(s);
		return "printed";
	}

	@Override
	public String getAccountDetailsByUsername(String username) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList getAccountHoldingsById(int accountId) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sendOrder(String accountId, String order) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList retrieveOrders(String market, String tickerSymbol) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrievePrice(String market, String tickerSymbol) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
