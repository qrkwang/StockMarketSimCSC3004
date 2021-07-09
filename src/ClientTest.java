import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientTest extends java.rmi.server.UnicastRemoteObject  implements ClientInt {
	
	private RemoteInterface remoteObj;
	private ClientInt cc = new Client();
	public ClientTest() throws RemoteException {
		remoteObj = new RemoteServant();
	}

	@Before
	public void setUpBeforeClass() throws Exception , RemoteException {
		int port = 1099;
		try {
			RemoteServer server = new RemoteServer();
			LocateRegistry.createRegistry(port);
			Naming.rebind("rmi://localhost:" + port + "/RemoteServer", remoteObj);
			remoteObj = (RemoteInterface) Naming.lookup("rmi://localhost:1099/RemoteServer");
		} catch (Exception e) {
			throw e;
		}
	}

	

	@Test
	public void testRemoveFromClientHashMap() {
		System.out.println("testing 1234567890");
		fail("Not yet implemented");
	}

	@Test
	public void testStartLeaderElectionAlgo() {
		boolean algoResult = false;
		try {
			algoResult = remoteObj.startLeaderElectionAlgo();
			System.out.println("testing inside");
			Assert.assertFalse("Fail to run the leader election (no leader server is selected)",algoResult == false);	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	@Test
	public void testStartDataRedundancyAlgo() {
		fail("Not yet implemented");
	}
	*/

	@Test
	public void testGetAccountDetailsByUsernameAndPW() {
		String username = "demo";
		String pw = "password";
		try {
			String accountDetails = remoteObj.getAccountDetailsByUsernameAndPW(cc,  username, pw);
			Assert.assertFalse("Fail to get accountDetails", accountDetails.isEmpty());	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetAccountHoldingsById() {
		fail("Not yet implemented");
	}

	@Test
	public void testSendOrder() {
		fail("Not yet implemented");
	}

	@Test
	public void testRetrieveCache() {
		fail("Not yet implemented");
	}

	@Override
	public void printToClient(String s) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

}
