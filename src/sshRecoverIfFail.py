import sys
import paramiko

# this file will help to recover the db server when one of the db server is down.
# this is to ensure that operation still goes on even if one of the db is down
# US died, Recover US server in sg
# SG died, Recover SG server in hk
# HK died, Recover HK server in us

# initialize the SSH client
client = paramiko.SSHClient()
# add to known hosts
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

# Server that fail
failedServer = sys.argv[1]
username = "joy"
# Database password
dbUser = "root"
dbPassword = "root"

# enter HK server to make a temporary DB for SG
# SG server is down
sgHostname = "192.168.1.18"
sgDatabaseName = "SGStockMarket"
sgReceivingFile = "SGPart2.sql"
# Retrieve the other half of the file
sgReceivingServer = "joy@192.168.1.16:/home/joy/SGPart2.sql"
# the file has already stored in hk server
sgCurrentFile = "SGPart1.sql"

# enter SG server to make a temporary DB for US
# US server is down
usHostname = "192.168.1.17"
usDatabaseName = "USStockMarket"
usReceivingFile = "USPart2.sql"
# Retrieve the other half of the file
usReceivingServer = "joy@192.168.1.18:/home/joy/USPart2.sql"
# the file has already stored in SG server
usCurrentFile = "USPart1.sql"

# enter US server to make a temporary DB for HK
# HK server is down
hkHostname = "192.168.1.16"
hkDatabaseName = "HKStockMarket"
hkReceivingFile = "HKPart2.sql"
# Retrieve the other half of the file
hkReceivingServer = "joy@192.168.1.17:/home/joy/HKPart2.sql"
# the file has already stored in US server
hkCurrentFile = "HKPart1.sql"
try:
    if failedServer == "SG":
        # Connecting to HK server via SSH
        client.connect(hostname=sgHostname, username=username)
        print("HK Server Connected successfully")
        # Command to execute bash script
        execCommand = "bash SGServerRecoveryInHK.sh -a '" + sgDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + sgReceivingFile + "' -e '" + sgReceivingServer+"' -f '" + sgCurrentFile + "'"
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
        execCommand = "bash UKServerRecoveryInSG.sh -a '" + usDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + usReceivingFile + "' -e '" + usReceivingServer + "' -f '" + usCurrentFile + "'"
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

except:
    print(err)
    exit()
