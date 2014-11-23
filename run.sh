#!/bin/bash

problems="OO_MyBatis OA_AJHsqldb OA_AJHotDraw OO_BCEL OO_JHotDraw OA_HealthWatcher OA_TollSystems OO_JBoss"

#functions="ChoiceFunction"
functions="MultiArmedBandit"


alpha=1.0
beta="0.01"

w=150
cs="1.0 3.0 5.0 7.0 9.0"

objectivesArray="2 4"


evaluations=60000
population=300
crossover=1
mutation=1

executions=10

path="experiment/"

rm -f run.txt

for c in $cs
do
	path="experiment_"$c"/"
	for objectives in $objectivesArray
	do
		#echo "java -cp dist/MECBA-Hyp.jar jmetal.experiments.Combined_NSGAII_"$objectives"obj" >> run.txt
		for function in $functions
		do
			for problem in $problems
			do
				echo "java -cp dist/MECBA-Hyp.jar hyperheuristics.main.NSGAIIHyperheuristicMain $population $evaluations $crossover $mutation $alpha $beta TwoPointsCrossover,MultiMaskCrossover,PMXCrossover SwapMutation,SimpleInsertionMutation $problem $function $w $c $objectives false $executions $path" >> run.txt
			done
		done
	done
done

cat run.txt | xargs -I CMD -P 8 bash -c CMD &
wait

rm -f run.txt

for c in $cs
do
	path="experiment_"$c"/"
	for objectives in $objectivesArray
	do
		echo "java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path $path $path $objectives $problems" >> run.txt
	done
done

cat run.txt | xargs -I CMD -P 8 bash -c CMD &
wait

rm -f run.txt

#zenity --info --text="Execuções finalizadas!"
