#!/bin/bash

#First create the directory layout
mkdir meta
mkdir meta/gmark/
mkdir meta/gmark/result/
mkdir meta/yago
mkdir meta/yago/result

mkdir experiment
mkdir experiment/gmark/
mkdir experiment/gmark/pos_world
mkdir experiment/gmark/maintenance/
mkdir experiment/gmark/maintenance/insertion
mkdir experiment/gmark/c2d

mkdir experiment/yago/
mkdir experiment/yago/pos_world
mkdir experiment/yago/maintenance/
mkdir experiment/yago/maintenance/insertion
mkdir experiment/yago/c2d


# Shifting data files around

mv gmark/result/*.txt meta/gmark/result/
mv gmark/qList.txt meta/gmark/
mv yago/result/*.txt meta/yago/result/
mv mat_run_collector.sh experiment/gmark/maintenance/
mv comp_run_collector.sh experiment/gmark/pos_world/
mv mat_run_collector.sh experiment/yago/maintenance/
mv comp_run_collector.sh experiment/yago/pos_world/
