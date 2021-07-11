import paramiko
import sys

# When the server is up, back up the temporary database and send it back to the original db
# drop the temporary database and recover the original db
# Declare a constant Ip Address
usIPAddress = "192.168.43.185"
sgIPAddress = "192.168.43.210"
HkIPaddress = "192.168.43.74"

# Database that was failed previously, going to recover now
recoveringServer = sys.argv[1]

# initialize the SSH client
client = paramiko.SSHClient()
# add to known hosts
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
username = "joy"
# database password
dbUser = "root"
dbPassword = "root"

# Server that temporary running the hk database
hkTempServer = usIPAddress
hkDatabaseName = "HKStockMarket"
hkFileName = "HKBackup.sql"
# File location to send back to the HK server
hkDestinationServer = "joy@" + HkIPaddress + ":/home/joy/HKBackup.sql"
# The original HK server
hkDestinationIP = "joy@" + HkIPaddress

# Server that temporary running the sg database
sgTempServer = HkIPaddress
sgDatabaseName = "SGStockMarket"
sgFileName = "SGBackup.sql"
# File location to send back to the SG server
sgDestinationServer = "joy@" + sgIPAddress + ":/home/joy/SGBackup.sql"
# The original SG server
sgDestinationIP = "joy@"+sgIPAddress

# Server that temporary running the US database
usTempServer = sgIPAddress
usDatabaseName = "USStockMarket"
usFileName = "USBackup.sql"
# File location to send back to the US server
usDestinationServer = "joy@"+ usIPAddress + ":/home/joy/USBackup.sql"
# The original US server
usDestinationIP = "joy@" + usIPAddress

try:
    # bringing HK DB back to the original server
    if recoveringServer == "HK":
        # Connecting to US server via SSH
        client.connect(hostname=hkTempServer, username=username)
        print("US Server Connected successfully")
        # Command to execute bash script
        execCommand = "bash RecoverHK.sh -a '" + hkDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + hkFileName + "' -e '" + hkDestinationServer + "' -f '" + hkDestinationIP + "'"
        # execute the BASH script
        stdin, stdout, stderr = client.exec_command(execCommand)
        # read the standard output and print it
        print(stdout.read().decode())
        # print errors if there are any
        err = stderr.read().decode()
        if err:
            print(err)
        # close the connection
        client.close()

    # bring SG DB back to the original server
    elif recoveringServer == "SG":
        # Connecting to HK server via SSH
        client.connect(hostname=sgTempServer, username=username)
        print("HK Server Connected successfully")
        # Command to execute bash script
        execCommand = "bash RecoverSG.sh -a '" + sgDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + sgFileName + "' -e '" + sgDestinationServer + "' -f '" + sgDestinationIP + "'"
        # execute the BASH script
        stdin, stdout, stderr = client.exec_command(execCommand)
        # read the standard output and print it
        print(stdout.read().decode())
        # print errors if there are any
        err = stderr.read().decode()
        if err:
            print(err)
        # close the connection
        client.close()
    # bring US DB back to the original server
    elif recoveringServer == "US":
        # Connecting to SG server via SSH
        client.connect(hostname=usTempServer, username=username)
        print("SG Server Connected successfully")
        # Command to execute bash script
        execCommand = "bash RecoverUS.sh -a '" + usDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + usFileName + "' -e '" + usDestinationServer + "' -f '" + usDestinationIP + "'"
        # execute the BASH script
        stdin, stdout, stderr = client.exec_command(execCommand)
        # read the standard output and print it
        print(stdout.read().decode())
        # print errors if there are any
        err = stderr.read().decode()
        if err:
            print(err)
        # close the connection
        client.close()
except:
    print(err)
    print("[!] Cannot connect to the SSH Server")
    exit()
