
import java.rmi.Remote;

public interface ClientInt extends Remote {

	public void printToClient(String s) throws java.rmi.RemoteException;
	public void updateMarket(String market) throws java.rmi.RemoteException;
	public void updateStock(String market, int stockid) throws java.rmi.RemoteException;
	public void sendOrderBook(String orderbook) throws java.rmi.RemoteException;
}