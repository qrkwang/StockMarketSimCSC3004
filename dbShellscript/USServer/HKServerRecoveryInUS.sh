#!/bin/sh
#getting all the arguments from python file
while getopts a:b:c:d:e:f: flag
do
    case "${flag}" in
        a) databaseName=${OPTARG};;
        b) dbUser=${OPTARG};;
        c) dbPassword=${OPTARG};;
        d) fileName=${OPTARG};;
        e) receivingServer=${OPTARG};;
        f) serverFileName=${OPTARG};;
    esac
done
start_time="$(date -u +%s)"

#receiving file from the US server
scp $receivingServer $fileName

#Host sg server in hk server to allow operation to continue if sg server died.
mysql -u$dbUser -p$dbPassword -e"CREATE DATABASE IF NOT EXISTS $databaseName"
mysql --user=$dbUser --password=$dbPassword <$serverFileName $databaseName

mysql --user=$dbUser --password=$dbPassword <$fileName $databaseName

end_time="$(date -u +%s)"

elapsed="$(($end_time-$start_time))"
echo "The process took $elapsed seconds to run"


