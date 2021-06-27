import paramiko


# initialize the SSH client
client = paramiko.SSHClient()
# add to known hosts
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

usHostname = "192.168.1.16"
username = "joy"
dbUser = "root"
dbPassword = "root"
#US Server Info
usDatabaseName = "USStockMarket"
usFileNamePart1 = "USPart1.sql"
usDestinationServerPart1 = "joy@192.168.1.17:/home/joy/USPart1.sql"
usFileNamePart2 = "USPart2.sql"
usDestinationServerPart2 = "joy@192.168.1.18:/home/joy/USPart2.sql"

#SG Server Info
sgHostname = "192.168.1.17"
sgDatabaseName = "SGStockMarket"
sgFileNamePart1 = "SGPart1.sql"
sgDestinationServerPart1 = "joy@192.168.1.18:/home/joy/SGPart1.sql"
sgFileNamePart2 = "SGPart2.sql"
sgDestinationServerPart2 = "joy@192.168.1.16:/home/joy/SGPart2.sql"

#HK Server Info
hkHostname = "192.168.1.18"
hkDatabaseName = "HKStockMarket"
hkFileNamePart1 = "HKPart1.sql"
hkDestinationServerPart1 = "joy@192.168.1.16:/home/joy/HKPart1.sql"
hkFileNamePart2 = "HKPart2.sql"
hkDestinationServerPart2 = "joy@192.168.1.17:/home/joy/HKPart2.sql"
try:
    #backup USServer
    client.connect(hostname=usHostname, username=username)
    print("US Server Connect Successfully")

    execCommand = "bash USServerBackup.sh -a '" + usDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + usFileNamePart1 + "' -e '" + usDestinationServerPart1+"' -f '" + usFileNamePart2 + "' -g '" + usDestinationServerPart2 + "'"
    print(execCommand)
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

    #backup SGServer
    client.connect(hostname=sgHostname, username=username)
    print("SG Server Connect Successfully")

    execCommand = "bash SGServerBackup.sh -a '" + sgDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + sgFileNamePart1 + "' -e '" + sgDestinationServerPart1 + "' -f '" + sgFileNamePart2 + "' -g '" + sgDestinationServerPart2 + "'"
    print(execCommand)
    # execute the BASH script
    stdin, stdout, stderr = client.exec_command(execCommand)
    # read the standard output and print it
    print(stdout.read().decode())
    # print errors if there are any
    err = stderr.read().decode()
    if err:
        print("Here got err");
        print(err)
    # close the connection
    client.close()


    #backup HKServer
    client.connect(hostname=hkHostname, username=username)
    print("HK Server Connect Successfully")

    execCommand = "bash HKServerBackup.sh -a '" + hkDatabaseName + "' -b '" + dbUser + "' -c '" + dbPassword + "' -d '" + hkFileNamePart1 + "' -e '" + hkDestinationServerPart1 + "' -f '" + hkFileNamePart2 + "' -g '" + hkDestinationServerPart2 + "'"
    print(execCommand)
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
    print("Have error")
    print(err)
    print("[!] Cannot connect to the SSH Server")
    exit()
