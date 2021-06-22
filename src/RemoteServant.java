import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

public class RemoteServant extends UnicastRemoteObject implements RemoteInterface {
	AccountDetailsDbScript accountDetailsDb = null;
	HKDbScript hkDb = null;
	SGDbScript sgDb = null;
	USADbScript usaDb = null;

	public RemoteServant() throws RemoteException {
		super();
		accountDetailsDb = new AccountDetailsDbScript(); // Start the RabbitMQ Receiver that's in main method
		try {
			accountDetailsDb.startWaitForMsg();
		} catch (SQLException e) {
			System.out.println("error start wait for msg");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hkDb = new HKDbScript(); // Start the RabbitMQ Receiver that's in main method
		sgDb = new SGDbScript(); // Start the RabbitMQ Receiver that's in main method
		usaDb = new USADbScript(); // Start the RabbitMQ Receiver that's in main method

		System.out.format("Creating server object\n"); // Print to client that server object is being created once
														// constructor called.
	}

	@Override
	public String testMethod(String s) throws RemoteException {
		System.out.println(s);
		return "printed";
	}

	@Override
	public String getAccountDetailsByUsername(String username) throws RemoteException {
		// TODO Auto-generated method stub
		return "5";
	}

	@Override
	public ArrayList getAccountHoldingsById(int accountId) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sendOrder(int accountId, String order) throws RemoteException {
		System.out.println("sending order");
		String QUEUE_NAME = "hello";

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try (com.rabbitmq.client.Connection connection = factory.newConnection();
				Channel channel = connection.createChannel()) {
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			String message = "Hello World!";
			channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

			System.out.println(" [x] Sent '" + message + "'");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ArrayList retrieveOrders(String market, String tickerSymbol) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrievePrice(String market, String tickerSymbol) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
