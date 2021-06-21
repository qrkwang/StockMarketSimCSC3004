import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
	public String testMethod(String s) throws RemoteException;

}
