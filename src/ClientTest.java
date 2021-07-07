import static org.junit.Assert.*;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientTest extends java.rmi.server.UnicastRemoteObject {
	
	private RemoteInterface remoteObj;
	public ClientTest() throws RemoteException {
	}


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			RemoteServer server = new RemoteServer();
			server.main(null); // calling the main method to trigger the server and registry
		} catch (Exception e) {
			throw e;
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRemoveFromClientHashMap() {
		fail("Not yet implemented");
	}

	@Test
	public void testStartLeaderElectionAlgo() {
		boolean algoResult = false;
		try {
			algoResult = remoteObj.startLeaderElectionAlgo();
			Assert.assertFalse("Fail to run the leader election (no leader server is selected)",algoResult);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testStartDataRedundancyAlgo() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAccountDetailsByUsernameAndPW() {
		fail("Not yet implemented");
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

}
