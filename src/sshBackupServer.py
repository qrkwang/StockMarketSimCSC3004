import paramiko

# backup all the databases and split the database into two part
# each part go into one db server

# Declare a constant Ip Address
usIPAddress = "192.168.1.16"
sgIPAddress = "192.168.1.17"
hkIPAddress = "192.168.1.18"


# initialize the SSH client
client = paramiko.SSHClient()
# add to known hosts
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

username = "joy"
# database username and password
dbUser = "root"
dbPassword = "root"

# US Server Info
usHostname = usIPAddress
usDatabaseName = "USStockMarket"
usFileNamePart1 = "USPart1.sql"
usDestinationServerPart1 = "joy@" + sgIPAddress + ":/home/joy/USPart1.sql"
usFileNamePart2 = "USPart2.sql"
usDestinationServerPart2 = "joy@" + hkIPAddress + ":/home/joy/USPart2.sql"

# SG Server Info
sgHostname = sgIPAddress
sgDatabaseName = "SGStockMarket"
sgFileNamePart1 = "SGPart1.sql"
sgDestinationServerPart1 = "joy@" + hkIPAddress + ":/home/joy/SGPart1.sql"
sgFileNamePart2 = "SGPart2.sql"
sgDestinationServerPart2 = "joy@"+usIPAddress+":/home/joy/SGPart2.sql"

# HK Server Info
hkHostname = hkIPAddress
hkDatabaseName = "HKStockMarket"
hkFileNamePart1 = "HKPart1.sql"
hkDestinationServerPart1 = "joy@"+usIPAddress+":/home/joy/HKPart1.sql"
hkFileNamePart2 = "HKPart2.sql"
hkDestinationServerPart2 = "joy@"+sgIPAddress+":/home/joy/HKPart2.sql"
i = 0
usTime = 0
try:
    while i < 10:
        print("iteration: " ,i +1)
        # Connecting to US server via SSH
        client.connect(hostname=usHostname, username=username)
        #print("US Server Connect Successfully")

        # Command to execute bash script
        execCommand = "time bash USServerBackup.sh -a '" + usDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + usFileNamePart1 + "' -e '" + usDestinationServerPart1+"' -f '" + usFileNamePart2 + "' -g '" + usDestinationServerPart2 + "'"

        # execute the BASH script
        stdin, stdout, stderr = client.exec_command(execCommand)
        # read the standard output and print it
        #print(stdout.read().decode())
        # print errors if there are any
        err = stderr.read().decode()
        # if err:
        #     print(err)

        x = err.split("\t")
        #print(x)
        #print("Timing to backup the US Server: ", x[1])
        x1 = x[1].split("\n")
        print("Timing to backup the US Server: ", x1[0])
        #print(x1)
       # usTime += x1[0]



        # close the connection
        client.close()

        # Connecting to SG server via SSH
        client.connect(hostname=sgHostname, username=username)
      #  print("SG Server Connect Successfully")
        # Command to execute bash script
        execCommand = "time bash SGServerBackup.sh -a '" + sgDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + sgFileNamePart1 + "' -e '" + sgDestinationServerPart1 + "' -f '" + sgFileNamePart2 + "' -g '" + sgDestinationServerPart2 + "'"

        # execute the BASH script
        stdin, stdout, stderr = client.exec_command(execCommand)
        # read the standard output and print it
        #print(stdout.read().decode())
        # print errors if there are any
        err = stderr.read().decode()
        # if err:
        #     print(err)
        x = err.split("\t")
        #print(x)

        x1 = x[1].split("\n")
        print("Timing to backup the SG Server: ", x1[0])
        USTime = x1[0].split("m")
        USMinute = USTime[0]
        # close the connection
        client.close()

        # Connecting to HK server via SSH
        client.connect(hostname=hkHostname, username=username)
        #print("HK Server Connect Successfully")
        # Command to execute bash script
        execCommand = "time bash HKServerBackup.sh -a '" + hkDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + hkFileNamePart1 + "' -e '" + hkDestinationServerPart1 + "' -f '" + hkFileNamePart2 + "' -g '" + hkDestinationServerPart2 + "'"

        # execute the BASH script
        stdin, stdout, stderr = client.exec_command(execCommand)
        # read the standard output and print it
        #print(stdout.read().decode())
        # print errors if there are any
        err = stderr.read().decode()
        # if err:
        #     print("There is an error", err)
        #print(err)
        x = err.split("\t")

        #print(x[1])
        #print(x[1])
        x1 = x[1].split("\n")
        print("Timing to backup the HK Server: ", x1[0])
        # print(x1[0])
        # close the connection
        client.close()

        i = i+1
except:
    #print(err)
   # print("Cannot connect to the SSH Server")
    exit()
