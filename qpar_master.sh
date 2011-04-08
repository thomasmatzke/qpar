#!/bin/sh

if [ "$1" = "--help" -o "$1" = "-h" ]; then
	echo ""
	echo "Usage:"
	echo "qpar_master.sh [-gui] [-i=BATCHFILE] [-log={debugg|info}] "
	echo ""
	exit
fi

java -classpath bin:lib/activemq-all-5.2.0.jar:lib/commons-io-1.4.jar:img:lib/log4j-1.2.15.jar main.java.master.MasterDaemon -Xss=1024m $*
