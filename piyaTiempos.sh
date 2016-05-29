#!/bin/bash

BRANCHES="unicoEstandar unicoAltoNivel unoParticionEstandar unoParticionAltoNivel unicoParaFronteras unicoParaFronterasEstandar"
BRANCHES_DYNAMICDOMAIN="unicoEstandarDominioCreciente unicoAltoNivelDominioCreciente unoPorFronteraDominioCreciente unoPorFronteraEstandarDominioCreciente"

SIZES=1000 2000 4000 8000 12000
NGENS=100 1000 2000 4000
TASKS=8
STEP=2

#For each branch
for branch in $BRANCHES; do

	git checkout $branch
	
	javac Speedup.java
	javac Time.java
	
	mkdir -p tmp/$branch
	
	for size in $SIZES; do
		echo "Running for size $size..."
		java Speedup $size $TASKS $STEP 1000 > tmp/$branch/s$size.txt
	done
done

#Dynamic domain versions
for branch in $BRANCHES_DYNAMICDOMAIN; do

	git checkout $branch
	
	javac Speedup.java
	javac Time.java
	
	mkdir -p tmp/$branch
	
	for it in $NGENS; do
		echo "Running $it generations..."
		java Speedup 4000 $TASKS $STEP $it > tmp/$branch/i$it.txt
	done
done

git checkout master
