#!/bin/bash

problems="OA_AJHotDraw
OA_AJHsqldb
OA_HealthWatcher
OA_TollSystems
OO_BCEL
OO_JBoss
OO_JHotDraw
OO_MyBatis"

functions="ChoiceFunction"
#MultiArmedBandit"

alpha=1.0
beta=0.021

w=12000
c=7.0
gamma=14.0
delta=0.15

objectivesArray="2
4"

evaluations=60000
population=300
crossover=0.95
mutation=0.02

rm -f run.txt

for objectives in $objectivesArray
do
    #echo "java -cp dist/MECBA-Hyp.jar jmetal.experiments.Combined_NSGAII_"$objectives"obj" >> run.txt
    for function in $functions
    do
        for problem in $problems
        do
            echo "java -cp dist/MECBA-Hyp.jar hyperheuristics.main.NSGAIIHyperheuristicMain $population $evaluations $crossover $mutation $alpha $beta TwoPointsCrossover,MultiMaskCrossover,PMXCrossover SwapMutation,SimpleInsertionMutation $problem $function $w $c $gamma $delta $objectives false" >> run.txt
        done
    done
done

cat run.txt | xargs -I CMD -P 2 bash -c CMD > /dev/null &
wait

rm -f run.txt

java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes

zenity --info --text="Execuções finalizadas!"
