import paramiko

host = "192.168.87.56"
username = "wh1901877"
#password = "password"
command = "sudo /etc/init.d/mysql start"

try:
    ssh_client = paramiko.SSHClient()
    ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh_client.connect(hostname=host, username=username)
    print("Connected to Account Server")
    tdin,stdout,stderr=ssh_client.exec_command(command)
    for result in stdout.readlines():
        print("command manage to run" + result)
    if stderr != "":
        print(stderr)
except:
    print("error")
