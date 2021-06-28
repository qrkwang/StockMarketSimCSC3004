import paramiko
import sys

host = sys.argv[1] #ip address
username = sys.argv[2] # username for ip address 
command = "sudo systemctl restart mysql"
print("inside python script")
try:
    ssh_client = paramiko.SSHClient()
    ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh_client.connect(hostname=host, username=username)
    print("Connected to Account Server")
    stdin,stdout,stderr=ssh_client.exec_command(command)
    for result in stdout.readlines():
        print("command manage to run" + result)
    if stderr != "":
        print(stderr)
except:
    print("error")


#python accountServer.py 192.168.87.56 wh1901877 example for calling
#https://phpraxis.wordpress.com/2016/09/27/enable-sudo-without-password-in-ubuntudebian/