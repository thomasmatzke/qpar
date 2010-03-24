#!/bin/bash

# converts all .qbf files in direcotry boole to .qpro files
# in directory qpro. uses boole2qpro for that.

for f in boole/*.qbf
do
	echo "converting " $f "..."
	./boole2qpro < $f > qpro/"`basename "$f" .qbf`.qpro"
done
