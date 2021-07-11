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

start_time="$(date -u +%s)"

myCount=$(mysql -s -N $databaseName -u$dbUser -p$dbPassword -e"SELECT Count(*) FROM stock")

count=$(($myCount/2))
if mysqldump --user=$dbUser --password=$dbPassword --databases $databaseName --no-create-db --routines -w "StockId <$count" >$fileNamePart1; then

	echo SUCCESS
else
	echo "Fail: could not back up the first part"
fi

if scp $fileNamePart1 $destinationServerPart1; then

	echo SUCCESS
else
	echo "Fail: could not transfer the first part"
fi

if mysqldump --user=$dbUser --password=$dbPassword --databases $databaseName --no-create-db -t -w "StockId >=$count" >$fileNamePart2; then

	echo SUCCESS
else
	echo "Fail: could not back up the second part"
fi

if scp $fileNamePart2 $destinationServerPart2; then

	echo SUCCESS
else
	echo "Fail: could not transfer the second part"
fi

end_time="$(date -u +%s)"

elapsed="$(($end_time-$start_time))"
echo "The process took $elapsed seconds to run"
