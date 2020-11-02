#!/bin/bash

fileName=$1

cut -f 1,2,3,4,5 run1/$fileName.txt > update_$fileName

for run in {2..10}
do	
	cut -f 5 run$run/$fileName.txt > tmp
	paste update_$fileName tmp > tmp1
	mv tmp1 update_$fileName
done
