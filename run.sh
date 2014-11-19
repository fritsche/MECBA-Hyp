#!/bin/bash

problems="OO_MyBatis OA_AJHsqldb OA_AJHotDraw OO_BCEL OO_JHotDraw OA_HealthWatcher OA_TollSystems OO_JBoss"

functions="ChoiceFunction"
#MultiArmedBandit"

alpha=1.0
betas="0.01"

w=150
c=5.0

objectivesArray="2"

evaluations=60000
population=300
crossover=0.95
mutation=0.02

executions=30

rm -f run.txt

for beta in $betas
do
	path="experiment_"$beta"/"
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

for beta in $betas
do
	path="experiment_"$beta"/"
	for objectives in $objectivesArray
	do
		echo "java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path $path $path $objectives $problems" >> run.txt
	done
done

cat run.txt | xargs -I CMD -P 8 bash -c CMD &
wait

rm -f run.txt

zenity --info --text="Execuções finalizadas!"
