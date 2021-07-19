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
	private RemoteServant remoteServant;

	public ClientTest() throws RemoteException {
		remoteObj = new RemoteServant();
		remoteServant = new RemoteServant();	
	}
	
	private enum Market {
		SG, HK, US
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
		algoResult = remoteServant.startLeaderElectionAlgo();
		System.out.println("testing inside");
		Assert.assertTrue("Fail to run the leader election (no leader server is selected)",algoResult);
	}


	

	@Test
	public void testGetAccountDetailsByUsernameAndPW_FalseCondition() {
		String username = "demo";
		String pw = "";
		try {
			String accountDetails = remoteObj.getAccountDetailsByUsernameAndPW(cc,  username, pw);
			System.out.println(accountDetails + "  running here");
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
			System.out.println(accountDetails + "  running here");
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
	public void testretrieveMarketCache() {
		String username = "demo";
		String pw = "password";
		try {
			String accountDetails = remoteObj.getAccountDetailsByUsernameAndPW(cc,  username, pw);
			System.out.println(accountDetails + "  running here");
			Assert.assertFalse("Fail to get accountDetails", accountDetails.matches("not found"));	
			Assert.assertFalse("Fail to get accountDetails", accountDetails.matches("problem"));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//String retrievePendingOrders(String market, int stockId)
	//retrieveCompletedOrders
	
	//String retrieveMarketCache(String market, ClientInt client) throws RemoteException
	//String retrieveStockCache(String market, int stockid, ClientInt client) throws RemoteException 

	
	
	@Override
	public void printToClient(String s) throws RemoteException {
		// TODO Auto-generated method stub	
	}


	@Override
	public void updateMarket(String market) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateStock(String market, int stockid) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendOrderBook(String orderbook) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	
}
