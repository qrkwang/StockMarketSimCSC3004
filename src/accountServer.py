import paramiko

host = "192.168.87.55"
username = "wh1901877"
command = "sudo systemctl restart mysql"

try:
    ssh_client = paramiko.SSHClient()
    ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh_client.connect(hostname=host, username=username)
    print("Connected to Account Server")
    stdin,stdout,stderr=ssh_client.exec_command(command)
    print("Connected to Account Server testing")
    for result in stdout.readlines():
        print("command manage to run" + result)
    if stderr != "":
        print(stderr)
except:
    print("error")



#https://phpraxis.wordpress.com/2016/09/27/enable-sudo-without-password-in-ubuntudebian/