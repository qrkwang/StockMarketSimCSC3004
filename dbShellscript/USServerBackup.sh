#!/bin/sh
while getopts a:b:c:d:e:f:g: flag
do
    case "${flag}" in
        a) databaseName=${OPTARG};;
        b) dbUser=${OPTARG};;
        c) dbPassword=${OPTARG};;
        d) fileNamePart1=${OPTARG};;
        e) destinationServerPart1=${OPTARG};;
        f) fileNamePart2=${OPTARG};;
        g) destinationServerPart2=${OPTARG};;
    esac
done

echo $databaseName;
echo $dbUser;
echo $dbPassword;
echo $fileNamePart1;
echo $destinationServerPart1;
echo $fileNamePart2;
echo $destinationServerPart2;

myCount=$(mysql -s -N $databaseName -u$dbUser -p$dbPassword -e"SELECT Count(*) FROM stock")

count=$(($myCount/2))
mysqldump --user=$dbUser --password=$dbPassword --databases $databaseName --no-create-db --routines -w "StockId <$count" >$fileNamePart1

echo "Successfully backing up the first part";

scp $fileNamePart1 $destinationServerPart1
echo "Successfully transferring the first part";

mysqldump --user=$dbUser --password=$dbPassword --databases $databaseName --no-create-db -t -w "StockId >=$count" >$fileNamePart2 
echo "Successfully backing up the second part";

scp $fileNamePart2 $destinationServerPart2
echo "Successfully transferring the second part";
