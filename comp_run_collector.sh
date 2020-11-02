#!/bin/bash

fileName=$1

cut -f 2,3,4,9  run1/stats_$fileName.txt > happi_$fileName

for run in {2..10}
do	
	cut -f 9 run$run/stats_$fileName.txt > tmp
	paste happi_$fileName tmp > tmp1
	mv tmp1 happi_$fileName
done


cut -f 2,3,4,10 run1/stats_$fileName.txt > pw_$fileName

for run in {2..10}
do	
	cut -f 10 run$run/stats_$fileName.txt > tmp
	paste pw_$fileName tmp > tmp1
	mv tmp1 pw_$fileName
done
