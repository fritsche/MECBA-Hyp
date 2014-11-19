#!/bin/bash

problems="OA_AJHotDraw
OA_AJHsqldb
OA_HealthWatcher
OA_TollSystems
OO_BCEL
OO_JBoss
OO_JHotDraw
OO_MyBatis"

functions="ChoiceFunction
MultiArmedBandit"

# alpha=1.0
# beta=0.021

# w=12000
# c=7.0

# objectivesArray="2
# 4"

evaluations=60000
population=300
crossover=0.95
mutation=0.02

executions=30
path="experiment/"

rm -f run.txt

# for objectives in $objectivesArray
# do
    #echo "java -cp dist/MECBA-Hyp.jar jmetal.experiments.Combined_NSGAII_"$objectives"obj" >> run.txt
    for function in $functions
    do
        for problem in $problems
        do
        	# 2 objectives
            echo "java -cp dist/MECBA-Hyp.jar hyperheuristics.main.NSGAIIHyperheuristicMain $population $evaluations $crossover $mutation 1.0 0.1027 TwoPointsCrossover,MultiMaskCrossover,PMXCrossover SwapMutation,SimpleInsertionMutation $problem $function 150 3.1043 2 false $executions $path" >> run.txt
            # 4 objectives
            echo "java -cp dist/MECBA-Hyp.jar hyperheuristics.main.NSGAIIHyperheuristicMain $population $evaluations $crossover $mutation 1.0 0.0223 TwoPointsCrossover,MultiMaskCrossover,PMXCrossover SwapMutation,SimpleInsertionMutation $problem $function 150 2.2274 4 false $executions $path" >> run.txt
        done
    done
# done

cat run.txt | xargs -I CMD -P 8 bash -c CMD >> output.log &
wait

rm -f run.txt

java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path $path $path 2 ${problems}

java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path $path $path 4 ${problems}

zenity --info --text="Execuções finalizadas!"
