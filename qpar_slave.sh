#!/bin/sh

if [ "$1" = "--help" -o "$1" = "-h" ]; then
	echo ""
	echo "Usage:"
	echo "qpar_slave.sh [master host] "
	echo ""
	exit
fi

java -classpath bin:lib/commons-io-1.4.jar:lib/commons-exec-1.1.jar:lib/commons-lang3-3.0-beta.jar:lib/mail.jar:lib/junit-4.8.2.jar:lib/log4j-1.2.15.jar -Xss5m -Xms64m -Xmx1024m -Dlogfile=slave qpar.slave.Slave $*
