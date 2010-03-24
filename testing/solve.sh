#!/bin/bash

# solves all .qpro formulas in diretory qpro.
# needs qpro local or in path

echo "solving formulas in qpro/"

for f in qpro/*.qpro
do
	echo `basename $f`
	qpro $f
done
