#!/bin/bash
if [ "$#" -ne  3 ]; then
	echo "usage <root database user> <root database password> <dbpassword for notifier_user>"
	exit
fi

#creates mysql user notifier_user and grants required permission to rif_notifier database
#mysql -e "DROP DATABASE rif_notifier" --user $1 --password=$2 
mysql -e "CREATE DATABASE IF NOT EXISTS rif_notifier" --user $1 --password=$2
mysql -e "CREATE USER IF NOT EXISTS 'notifier_user'@'localhost' identified by '$3'" --user $1 --password=$2
mysql -e "GRANT ALL PRIVILEGES ON rif_notifier.* TO 'notifier_user'@'localhost' " --user $1 --password=$2
mysql -e "FLUSH PRIVILEGES" --user $1 --password=$2

#clones the repo from github
#git clone https://github.com/rsksmart/rif-notifier rif-notifier
