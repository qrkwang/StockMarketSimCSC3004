import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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

}
