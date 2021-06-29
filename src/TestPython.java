import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.UnknownHostException;

public class TestPython {
	private static String USServerIPAddress = "192.168.1.16";
	private static String SGServerIPAddress = "192.168.1.17";
	private static String HKServerIPAddress = "192.168.1.18";
	
	public static void main(String[] args) {
		
		String failedServer = null;
		boolean usServerup = false;
		boolean sgServerup = false;
		boolean hkServerup = false;
		boolean usRequiredRecovery = false;
		boolean hkRequiredRecovery =false;
		boolean sgRequiredRecovery= false;
		
	 //Ping all the three servers to check if alive
	while(true) {
		try {
			if(sendPingRequest(USServerIPAddress) == false) {
				failedServer = "US";	
				System.out.println("Cannot ping US");
				usServerup = false;
			}
			else {
				usServerup = true;
			}
			if(sendPingRequest(SGServerIPAddress) == false) {
				failedServer = "SG";
				System.out.println("Cannot ping SG");
				sgServerup = false;
			}
			else {
				sgServerup = true;
			}
			if (sendPingRequest(HKServerIPAddress) == false) {
				failedServer = "HK";
				System.out.println("Cannot ping HK");
				hkServerup = false;
			}
			else {
				hkServerup = true;
			}
			
			if (failedServer != null && usRequiredRecovery == false && sgRequiredRecovery == false && hkRequiredRecovery == false)
			{
				executeFile( "src/sshRecoverAfterFail.py",failedServer);
				if(failedServer.equals("US"))
				{
					usRequiredRecovery = true;
				}
				else if (failedServer.equals("HK"))
				{
					hkRequiredRecovery = true;
				}
				else if (failedServer.equals("SG")) 
				{
					sgRequiredRecovery = true;
				}
				
			}
			if((usRequiredRecovery == true && usServerup == true) ||( sgServerup == true && sgRequiredRecovery == true) || (hkServerup == true && hkRequiredRecovery == true)) {
				executeFile( "src/sshRecoverOriginalServer.py", failedServer);
				failedServer = null;
				usRequiredRecovery = false;
				sgRequiredRecovery = false;
				hkRequiredRecovery = false;
				
			}
			Thread.sleep(5);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 
	 //Get a status of the servers that die
		
	 //if one of the server is down
		
	 //retrieve the server that failed
		
	//Run the sshRecoverAfterFail
		
	//keep ping the server, if up
	
	//run the sshRecoverOriginalServer
		
	}
	public static void executeFile(String fileName, String failedServer)
	{
		try {
			String[] cmd = {
				      "python",
				      fileName,
				      failedServer
				    };
			
			Process p = Runtime.getRuntime().exec(cmd);

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			System.out.println("running file " + fileName);
		
			while ((line = reader.readLine()) != null) {
			    System.out.println(line + "\n");
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
	
	}
	
	public static boolean sendPingRequest(String ipAddress)
            throws UnknownHostException, IOException
		{
		  InetAddress host = InetAddress.getByName(ipAddress);
		  
		  System.out.println("Sending Ping Request to " + ipAddress);
		  if (host.isReachable(5000))
		   return true;
		  else
		    return false;
		}
}
