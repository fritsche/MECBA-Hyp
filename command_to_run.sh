#!/bin/bash  

#echo "MECBA - Combined 2 objectives"
java -Xms2048m -classpath dist/MECBA.jar jmetal.experiments.Combined_NSGAII_2obj > resultado/time_Experiment_NSGAII_2obj.txt &
java -Xms2048m -classpath dist/MECBA.jar jmetal.experiments.Combined_PAES_2obj > resultado/time_Experiment_PAES_2obj.txt &
java -Xms2048m -classpath dist/MECBA.jar jmetal.experiments.Combined_SPEA2_2obj > resultado/time_Experiment_SPEA2_2obj.txt &

echo "MECBA - Combined 4 objectives"
java -Xms2048m -classpath dist/MECBA.jar jmetal.experiments.Combined_NSGAII_4obj > resultado/time_Experiment_NSGAII_4obj.txt &
java -Xms2048m -classpath dist/MECBA.jar jmetal.experiments.Combined_PAES_4obj > resultado/time_Experiment_PAES_4obj.txt &
java -Xms2048m -classpath dist/MECBA.jar jmetal.experiments.Combined_SPEA2_4obj > resultado/time_Experiment_SPEA2_4obj.txt &
