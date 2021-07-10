import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import classes.StockOwned;

public class ClientTest extends java.rmi.server.UnicastRemoteObject  implements ClientInt {
	
	private RemoteInterface remoteObj;
	private ClientInt cc = new Client();
	public ClientTest() throws RemoteException {
		remoteObj = new RemoteServant();
		System.out.println("runnning set up claassssssssssssssssssssssssssssssssssssss heeeeeeeeeeeeeeeeeeeeee");
		
	}

	public void setUp() throws Exception , RemoteException {
		System.out.println("runnning set up claassssssssssssssssssssssssssssssssssssss heeeeeeeeeeeeeeeeeeeeee");
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
		// no return value . will need to check again
		System.out.println("testing 1234567890");
		fail("Not yet implemented");
	}

	
	
	@Test
	public void testStartLeaderElectionAlgo() {
		boolean algoResult = false;
		try {
			algoResult = remoteObj.startLeaderElectionAlgo();
			System.out.println("testing inside");
			Assert.assertFalse("Fail to run the leader election (no leader server is selected)",algoResult);	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Test
	public void testStartDataRedundancyAlgo() {
		// no return value and will keep on running in background
		fail("Not yet implemented");
	}
	

	@Test
	public void testGetAccountDetailsByUsernameAndPW_FalseCondition() {
		String username = "demo";
		String pw = "";
		try {
			String accountDetails = remoteObj.getAccountDetailsByUsernameAndPW(cc,  username, pw);
			Assert.assertFalse("Fail to get accountDetails", accountDetails.isEmpty());	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void testGetAccountDetailsByUsernameAndPW_TrueCondition() {
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
	public void testGetAccountHoldingsById_TrueCondition() {
		int accountId = 0;
		try {
			ArrayList<StockOwned> resultAccount = remoteObj.getAccountHoldingsById(accountId);
			Assert.assertNotNull("The account Id exist", resultAccount);	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetAccountHoldingsById_FalseCondition() {
		int accountId = 0;
		try {
			ArrayList<StockOwned> resultAccount = remoteObj.getAccountHoldingsById(accountId);
			Assert.assertNull("The account Id is wrong", resultAccount);	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testSendOrder() {
		fail("Not yet implemented");
	}
	
/*
	@Test
	public void testRetrieveCache() {
		fail("Not yet implemented");
	}
*/
	
	@Override
	public void printToClient(String s) throws RemoteException {
		// TODO Auto-generated method stub	
	}

	
}
