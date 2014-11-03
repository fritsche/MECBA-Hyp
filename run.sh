#!/bin/bash

problems="OA_AJHotDraw
OA_AJHsqldb
OA_HealthWatcher
OA_TollSystems
OO_BCEL
OO_JBoss
OO_JHotDraw
OO_MyBatis"

#function="MultiArmedBandit"
function="ChoiceFunction"

w=5000
c=7.0
gamma=14.0
delta=0.15

objectives=2
evaluations=60000
population=300
crossover=0.95
mutation=0.02

for problem in $problems
do
    java -cp dist/MECBA-Hyp.jar hyperheuristics.main.NSGAIIHyperheuristicMain $population $evaluations $crossover $mutation 1.0 0.08 TwoPointsCrossover,MultiMaskCrossover,PMXCrossover SwapMutation,SimpleInsertionMutation $problem $function $w $c $gamma $delta $objectives false > /dev/null &
done
