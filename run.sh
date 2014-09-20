#!/bin/bash

rm -r experiment > /dev/null

problems="OA_AJHotDraw
OA_AJHsqldb
OA_HealthWatcher
OA_TollSystems
OO_BCEL
OO_JBoss
OO_JHotDraw
OO_MyBatis"


algorithm="MultiArmedBandit" # MultiArmedBandit or ChoiceFunction
w=20
c=7.0
gamma=14.0
delta=0.15

for problem in $problems
do
	java -cp dist/MECBA-Hyp.jar hyperheuristics.main.NSGAIIHyperheuristicMain 100 25000 0.95 0.02 1.0 0.08 TwoPointsCrossover,MultiMaskCrossover,PMXCrossover SwapMutation,SimpleInsertionMutation $problem $algorithm $w $c $gamma $delta > /dev/null &
done
