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
	}

	public void setUp() throws Exception , RemoteException {
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
	public void testStartLeaderElectionAlgo() {
		boolean algoResult = false;
		try {
			algoResult = remoteObj.startLeaderElectionAlgo();
			System.out.println("testing inside");
			Assert.assertTrue("Fail to run the leader election (no leader server is selected)",algoResult);	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	

	@Test
	public void testGetAccountDetailsByUsernameAndPW_FalseCondition() {
		String username = "demo";
		String pw = "";
		try {
			String accountDetails = remoteObj.getAccountDetailsByUsernameAndPW(cc,  username, pw);
			Assert.assertTrue("Fail to get accountDetails", accountDetails.matches("not found"));	
			Assert.assertTrue("Fail to get accountDetails", accountDetails.matches("problem"));		
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
			Assert.assertFalse("Fail to get accountDetails", accountDetails.matches("not found"));	
			Assert.assertFalse("Fail to get accountDetails", accountDetails.matches("problem"));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Test
	public void testGetAccountHoldingsById_TrueCondition() {
		int accountId = 2;
		// cant test yet
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
		// cant test yet 
		int accountId = 3;
		try {
			ArrayList<StockOwned> resultAccount = remoteObj.getAccountHoldingsById(accountId);
			Assert.assertNull("The account Id is wrong", resultAccount);	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Test
	public void testgetAllStocksByMarket_TrueCondition() {
		String market = "HK";
		String getStock;
		try {
			getStock = remoteObj.getAllStocksByMarket(market);
			Assert.assertNotEquals("Fail to get back the stock market", getStock ,"empty");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testgetAllStocksByMarket_FailCondition() {
		String market = "tttttttttt";
		String getStock;
		try {
			getStock = remoteObj.getAllStocksByMarket(market);
			System.out.println("printttttttttttttttt heeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeere  +" + getStock);
			Assert.assertEquals("Stock Market is current empty", getStock , "empty");	
			Assert.assertEquals("Fail to get back the sotck market", getStock , "error fetching");	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// not too sure for retrieve cache
/*
	@Test
	public void testRetrieveCache() {
		String market = "";
		int stockid	 = 0;	
		try {
			String cacheResult = remoteObj.retrieveCache(market, stockid);
			Assert.assertNotNull("Cache result have been recieved", cacheResult);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// not sure what is the value in for stockId and market
	}
	

	@Test
	public void testRemoveFromClientHashMap() {
		// no return value . will need to check again
		System.out.println("testing 1234567890");
		fail("Not yet implemented");
	}

*/
	
	@Override
	public void printToClient(String s) throws RemoteException {
		// TODO Auto-generated method stub	
	}

	
}
