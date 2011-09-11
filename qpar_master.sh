#!/bin/sh

if [ "$1" = "--help" -o "$1" = "-h" ]; then
	echo ""
	echo "Usage:"
	echo "qpar_master.sh [-i=BATCHFILE]"
	echo ""
	exit
fi
echo $*
java -classpath bin:lib/commons-io-1.4.jar:lib/log4j-1.2.15.jar:lib/commons-exec-1.1.jar:lib/mail.jar:lib/commons-lang3-3.0-beta.jar -Xss5m -Xms64m -Xmx512m -Djava.rmi.server.useLocalHostname=true -Dlogfile=master qpar.master.Master $*
