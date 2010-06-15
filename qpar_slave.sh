#!/bin/sh

if [ "$1" = "--help" -o "$1" = "-h" ]; then
	echo ""
	echo "Usage:"
	echo "qpar_slave.sh [tcp://IP_ADDRESS:PORT] [-log={debugg|info}] "
	echo ""
	exit
fi

java -classpath bin:lib/activemq-all-5.2.0.jar:lib/commons-io-1.4.jar:img:lib/log4j-1.2.15.jar main.java.slave.SlaveDaemon -Xss=1024m $*
