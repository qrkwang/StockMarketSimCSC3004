import paramiko

# backup all the databases and split the database into two part
# each part go into one db server


# initialize the SSH client
client = paramiko.SSHClient()
# add to known hosts
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

username = "joy"
# database username and password
dbUser = "root"
dbPassword = "root"

# US Server Info
usHostname = "192.168.1.16"
usDatabaseName = "USStockMarket"
usFileNamePart1 = "USPart1.sql"
usDestinationServerPart1 = "joy@192.168.1.17:/home/joy/USPart1.sql"
usFileNamePart2 = "USPart2.sql"
usDestinationServerPart2 = "joy@192.168.1.18:/home/joy/USPart2.sql"

# SG Server Info
sgHostname = "192.168.1.17"
sgDatabaseName = "SGStockMarket"
sgFileNamePart1 = "SGPart1.sql"
sgDestinationServerPart1 = "joy@192.168.1.18:/home/joy/SGPart1.sql"
sgFileNamePart2 = "SGPart2.sql"
sgDestinationServerPart2 = "joy@192.168.1.16:/home/joy/SGPart2.sql"

# HK Server Info
hkHostname = "192.168.1.18"
hkDatabaseName = "HKStockMarket"
hkFileNamePart1 = "HKPart1.sql"
hkDestinationServerPart1 = "joy@192.168.1.16:/home/joy/HKPart1.sql"
hkFileNamePart2 = "HKPart2.sql"
hkDestinationServerPart2 = "joy@192.168.1.17:/home/joy/HKPart2.sql"

try:
    # Connecting to US server via SSH
    client.connect(hostname=usHostname, username=username)
    print("US Server Connect Successfully")

    # Command to execute bash script
    execCommand = "bash USServerBackup.sh -a '" + usDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + usFileNamePart1 + "' -e '" + usDestinationServerPart1+"' -f '" + usFileNamePart2 + "' -g '" + usDestinationServerPart2 + "'"

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

    # Connecting to SG server via SSH
    client.connect(hostname=sgHostname, username=username)
    print("SG Server Connect Successfully")
    # Command to execute bash script
    execCommand = "bash SGServerBackup.sh -a '" + sgDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + sgFileNamePart1 + "' -e '" + sgDestinationServerPart1 + "' -f '" + sgFileNamePart2 + "' -g '" + sgDestinationServerPart2 + "'"

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

    # Connecting to HK server via SSH
    client.connect(hostname=hkHostname, username=username)
    print("HK Server Connect Successfully")
    # Command to execute bash script
    execCommand = "bash HKServerBackup.sh -a '" + hkDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + hkFileNamePart1 + "' -e '" + hkDestinationServerPart1 + "' -f '" + hkFileNamePart2 + "' -g '" + hkDestinationServerPart2 + "'"

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
    print("Cannot connect to the SSH Server")
    exit()
