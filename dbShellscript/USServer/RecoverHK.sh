#!/bin/sh
while getopts a:b:c:d:e:f:g: flag
do
    case "${flag}" in
        a) databaseName=${OPTARG};;
        b) dbUser=${OPTARG};;
        c) dbPassword=${OPTARG};;
        d) fileName=${OPTARG};;
        e) destinationServer=${OPTARG};;
        f) destinationIP=${OPTARG};;
    esac
done
start_time="$(date -u +%s)"

# Backing up the whole database
mysqldump --user=$dbUser --password=$dbPassword --databases $databaseName --no-create-db --routines > $fileName

# Drop the db after backing up
mysql -u$dbUser -p$dbPassword -e"DROP DATABASE IF EXISTS $databaseName"

# transfer the backup to the original server
scp $fileName $destinationServer

# ssh back to the server to execute the script to bring back the db 
ssh $destinationIP "mysql -u $dbUser -p$dbPassword -e'DROP DATABASE if EXISTS $databaseName; CREATE DATABASE $databaseName';
mysql -u $dbUser -p$dbPassword   < $fileName $databaseName"

end_time="$(date -u +%s)"

elapsed="$(($end_time-$start_time))"
echo "The process took $elapsed seconds to run"









