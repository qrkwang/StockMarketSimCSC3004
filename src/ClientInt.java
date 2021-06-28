
import java.rmi.Remote;

public interface ClientInt extends Remote {

	public void printToClient(String s) throws java.rmi.RemoteException;
}