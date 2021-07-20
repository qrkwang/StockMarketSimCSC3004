import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface RemoteInterface extends Remote {
	// Client will need to display account details, send buy/sell order, list of
	// account stock holdings, polling of stock price per interval (not necessarily
	// need polling, can be on update) when on that page, stock page orders.

	public String getAccountDetailsByUsernameAndPW(ClientInt cc, String username, String pw) throws RemoteException;
	public ArrayList getAccountHoldingsById(int accountId) throws RemoteException;

	public String sendOrder(int accountId, String market, String order,  boolean randomGeneration) throws RemoteException;

	public void removeFromClientHashMap(int accountId) throws RemoteException;
	
	public String retrieveMarketCache(String market, ClientInt client)  throws RemoteException;
	public HashMap<String, String> retrieveStockCache(String market, int stockid, ClientInt client) throws RemoteException;
}
