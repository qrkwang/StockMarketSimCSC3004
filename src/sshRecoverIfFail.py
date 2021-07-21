import sys
import paramiko

# this file will help to recover the db server when one of the db server is down.
# this is to ensure that operation still goes on even if one of the db is down
# US died, Recover US server in sg
# SG died, Recover SG server in hk
# HK died, Recover HK server in us

# Declare a constant Ip Addresses
usIPAddress = "192.168.1.16"
sgIPAddress = "192.168.1.17"
hkIPAddress = "192.168.1.18"

# initialize the SSH client
client = paramiko.SSHClient()
# add to known hosts
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

# Server that fail
failedServer = sys.argv[1]
username = "a"
# Database password
dbUser = "root"
dbPassword = "root"

# enter HK server to make a temporary DB for SG
# SG server is down
sgHostname = hkIPAddress
sgDatabaseName = "SGStockMarket"
sgReceivingFile = "SGPart2.sql"
# Retrieve the other half of the file
sgReceivingServer = "a@" + usIPAddress + ":/home/a/SGPart2.sql"
# the file has already stored in hk server
sgCurrentFile = "SGPart1.sql"

# enter SG server to make a temporary DB for US
# US server is down
usHostname = sgIPAddress
usDatabaseName = "USStockMarket"
usReceivingFile = "USPart2.sql"
# Retrieve the other half of the file
usReceivingServer = "a@"+hkIPAddress + ":/home/a/USPart2.sql"
# the file has already stored in SG server
usCurrentFile = "USPart1.sql"

# enter US server to make a temporary DB for HK
# HK server is down
hkHostname = usIPAddress
hkDatabaseName = "HKStockMarket"
hkReceivingFile = "HKPart2.sql"
# Retrieve the other half of the file
hkReceivingServer = "a@" + sgIPAddress + ":/home/a/HKPart2.sql"
# the file has already stored in US server
hkCurrentFile = "HKPart1.sql"
try:
    if failedServer == "SG":
        # Connecting to HK server via SSH
        client.connect(hostname=sgHostname, username=username)
        print("HK Server Connected successfully")
        # Command to execute bash script
        execCommand = "bash SGServerRecoveryInHK.sh -a '" + sgDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + sgReceivingFile + "' -e '" + sgReceivingServer + "' -f '" + sgCurrentFile + "'"
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

    elif failedServer == "US":
        # Connecting to SG server via SSH
        client.connect(hostname=usHostname, username=username)
        print("SG Server Connected successfully")
        # Command to execute bash script
        execCommand = "bash USServerRecoveryInSG.sh -a '" + usDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + usReceivingFile + "' -e '" + usReceivingServer + "' -f '" + usCurrentFile + "'"
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

    elif failedServer == "HK":
        # Connecting to US server via SSH
        client.connect(hostname=hkHostname, username=username)
        print("US Server Connected successfully")
        # Command to execute bash script
        execCommand = "bash HKServerRecoveryInUS.sh -a '" + hkDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + hkReceivingFile + "' -e '" + hkReceivingServer + "' -f '" + hkCurrentFile + "'"
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

except err:
    print(err)
    exit()
