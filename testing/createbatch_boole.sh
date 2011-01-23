#!/bin/bash

# this script creates the batch commands to solve all formulas in the qbf
# directory with QPar. Results will be written to the testing directory one file
# per formula

((i = 1))
echo "waitforslave 2 qpro"
for f in boole/*.qbf
do
	echo "newjob testing/$f testing/`basename "$f" .qbf`.out qpro simple 2"
	echo "startjob $i"
	echo "waitforresult $i"
	((i++))
done
echo "killallslaves"
echo "quit"

