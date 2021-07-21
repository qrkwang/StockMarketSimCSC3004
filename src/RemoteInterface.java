import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface RemoteInterface extends Remote {
	public String getAccountDetailsByUsernameAndPW(ClientInt cc, String username, String pw) throws RemoteException;
	public String getAccountDetailsById(ClientInt cc, int accountId) throws RemoteException;
	public ArrayList getAccountHoldingsById(int accountId) throws RemoteException;

	public String sendOrder(int accountId, String market, String order,  boolean randomGeneration) throws RemoteException;

	public void removeFromClientHashMap(int accountId) throws RemoteException;
	
	public String retrieveMarketCache(String market, ClientInt client)  throws RemoteException;
	public String cacheOrderBook(String market, int stockId)  throws RemoteException;
	public void cacheMarket() throws RemoteException;
	public HashMap<String, String> retrieveStockCache(String market, int stockid, ClientInt client, boolean callback) throws RemoteException;
}
