#!/bin/bash

problems="OA_AJHotDraw
OA_AJHsqldb
OA_HealthWatcher
OA_TollSystems
OO_BCEL
OO_JBoss
OO_JHotDraw
OO_MyBatis"

for problem in $problems
do
	java -cp dist/MECBA-Hyp.jar hyperheuristics.main.NSGAIIHyperheuristicMain 100 25000 0.95 0.02 1.0 0.08 TwoPointsCrossover,MultiMaskCrossover,PMXCrossover SwapMutation,SimpleInsertionMutation $problem ChoiceFunction > /dev/null &
done
