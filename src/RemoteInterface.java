import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RemoteInterface extends Remote {
	// Client will need to display account details, send buy/sell order, list of
	// account stock holdings, polling of stock price per interval (not necessarily
	// need polling, can be on update) when on that page, stock page orders.

	public String testMethod(String s) throws RemoteException;

	public String getAllStocksByMarket(String market) throws RemoteException;

	public String getAccountDetailsByUsernameAndPW(String username, String pw) throws RemoteException;

	public ArrayList getAccountHoldingsById(int accountId) throws RemoteException;

	public String sendOrder(int accountId, String order) throws RemoteException;

	public String retrievePendingOrders(String market, int stockId) throws RemoteException;

	public String retrieveCompletedOrders(String market, int stockId) throws RemoteException;

	public void startLeaderElectionAlgo() throws RemoteException;
}
