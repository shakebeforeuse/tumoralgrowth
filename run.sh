#!/bin/bash
TASKS=32
STEP=2

for size in 1000 1500 2000 3000 4000 8000 12000
do
	echo "Running for size $size..."
	out="$(java -Xmx2G Speedup $size $TASKS $STEP 1000 2>&1)"
	echo -e "$out" >> "tmp/$size.txt"
done
