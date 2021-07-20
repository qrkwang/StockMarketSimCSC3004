import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/*
 * Remote server to call servant and bind to registry
 */

public class RemoteServer {

	public static void main(String[] args) {
		try {
			int port = 1099;

			System.out.format("Created server, now advertising it\n");

			// Naming lookup
			LocateRegistry.createRegistry(port);
			RemoteInterface remoteObj = new RemoteServant();
			Naming.rebind("rmi://localhost:" + port + "/RemoteServer", remoteObj);
			System.out.format("Advertising completed\n");

		} catch (Exception e) {
			System.out.format("export exception - %s\n", e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

}
