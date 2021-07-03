import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RemoteInterface extends Remote {
	// Client will need to display account details, send buy/sell order, list of
	// account stock holdings, polling of stock price per interval (not necessarily
	// need polling, can be on update) when on that page, stock page orders.

	public String getAllStocksByMarket(String market) throws RemoteException;

	public String getAccountDetailsByUsernameAndPW(ClientInt cc, String username, String pw) throws RemoteException;

	public ArrayList getAccountHoldingsById(int accountId) throws RemoteException;

	public String sendOrder(int accountId, String market, String order) throws RemoteException;

	public void startLeaderElectionAlgo() throws RemoteException;

	public void startDataRedundancyAlgo() throws RemoteException;

	public void removeFromClientHashMap(int accountId) throws RemoteException;

	public String retrieveCache(String market, int stockid) throws RemoteException;

}
