import java.rmi.RemoteException;

public class RemoteServant implements RemoteInterface {

	@Override
	public String testMethod(String s) throws RemoteException {
		System.out.println(s);
		return "printed";
	}

}
