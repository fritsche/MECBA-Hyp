#!/bin/bash

problems="OA_AJHotDraw
OA_AJHsqldb
OA_HealthWatcher
OA_TollSystems
OO_BCEL
OO_JBoss
OO_JHotDraw
OO_MyBatis"

algorithm="MultiArmedBandit"
#algorithm="ChoiceFunction"
w=5000
c=7.0
gamma=14.0
delta=0.15

objectives=2

for problem in $problems
do
    java -cp MECBA-Hyp.jar hyperheuristics.main.NSGAIIHyperheuristicMain 100 25000 0.95 0.02 1.0 0.08 TwoPointsCrossover,MultiMaskCrossover,PMXCrossover SwapMutation,SimpleInsertionMutation $problem $algorithm $w $c $gamma $delta $objectives # > /dev/null &
done
