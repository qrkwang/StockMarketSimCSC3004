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
HkIPAddress = "192.168.1.18"

# initialize the SSH client
client = paramiko.SSHClient()
# add to known hosts
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

# Server that fail
failedServer = "US"
username = "joy"
# Database password
dbUser = "root"
dbPassword = "root"

# enter HK server to make a temporary DB for SG
# SG server is down
sgHostname = HkIPAddress
sgDatabaseName = "SGStockMarket"
sgReceivingFile = "SGPart2.sql"
# Retrieve the other half of the file
sgReceivingServer = "joy@" + usIPAddress + ":/home/joy/SGPart2.sql"
# the file has already stored in hk server
sgCurrentFile = "SGPart1.sql"

# enter SG server to make a temporary DB for US
# US server is down
usHostname = sgIPAddress
usDatabaseName = "USStockMarket"
usReceivingFile = "USPart2.sql"
# Retrieve the other half of the file
usReceivingServer = "joy@"+HkIPAddress + ":/home/joy/USPart2.sql"
# the file has already stored in SG server
usCurrentFile = "USPart1.sql"

# enter US server to make a temporary DB for HK
# HK server is down
hkHostname = usIPAddress
hkDatabaseName = "HKStockMarket"
hkReceivingFile = "HKPart2.sql"
# Retrieve the other half of the file
hkReceivingServer = "joy@" + sgIPAddress + ":/home/joy/HKPart2.sql"
# the file has already stored in US server
hkCurrentFile = "HKPart1.sql"
i = 0
try:
    if failedServer == "SG":
        # Connecting to HK server via SSH
        client.connect(hostname=sgHostname, username=username)
        #print("HK Server Connected successfully")
        # Command to execute bash script
        execCommand = "time bash SGServerRecoveryInHK.sh -a '" + sgDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + sgReceivingFile + "' -e '" + sgReceivingServer + "' -f '" + sgCurrentFile + "'"
        # execute the BASH script
        stdin, stdout, stderr = client.exec_command(execCommand)
        # read the standard output and print it
        print(stdout.read().decode())
        # print errors if there are any
        err = stderr.read().decode()
        x = err.split("\t")
        # print(x)
        # print("Timing to backup the US Server: ", x[1])
        x1 = x[1].split("\n")
        print("Timing to set up the temporary SG Server: ", x1[0])
        # close the connection
        client.close()

    elif failedServer == "US":
        # Connecting to SG server via SSH
        client.connect(hostname=usHostname, username=username)
        #print("SG Server Connected successfully")
        # Command to execute bash script
        execCommand = "time bash USServerRecoveryInSG.sh -a '" + usDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + usReceivingFile + "' -e '" + usReceivingServer + "' -f '" + usCurrentFile + "'"
        # execute the BASH script
        stdin, stdout, stderr = client.exec_command(execCommand)
        # read the standard output and print it
        print(stdout.read().decode())
        # print errors if there are any
        err = stderr.read().decode()
        x = err.split("\t")
        # print(x)
        # print("Timing to backup the US Server: ", x[1])
        x1 = x[1].split("\n")
        print("Timing to set up the temporary US Server: ", x1[0])
        # close the connection
        client.close()

    elif failedServer == "HK":
        # Connecting to US server via SSH
        client.connect(hostname=hkHostname, username=username)
        #print("US Server Connected successfully")
        # Command to execute bash script
        execCommand = "time bash HKServerRecoveryInUS.sh -a '" + hkDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + hkReceivingFile + "' -e '" + hkReceivingServer + "' -f '" + hkCurrentFile + "'"
        # execute the BASH script
        stdin, stdout, stderr = client.exec_command(execCommand)
        # read the standard output and print it
        print(stdout.read().decode())
        # print errors if there are any
        err = stderr.read().decode()
        # if err:
        #     print(err)

        x = err.split("\t")
        # print(x)
        # print("Timing to backup the US Server: ", x[1])
        x1 = x[1].split("\n")
        print("Timing to set up the temporary HK Server: ", x1[0])
        # close the connection
        client.close()

except err:
    print(err)
    exit()
