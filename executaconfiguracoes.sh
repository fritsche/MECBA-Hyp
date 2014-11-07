#!/bin/bash

problems="OA_AJHotDraw
OA_AJHsqldb
OA_HealthWatcher
OA_TollSystems
OO_BCEL
OO_JBoss
OO_JHotDraw
OO_MyBatis"

function="MultiArmedBandit"
#function="ChoiceFunction"

#w=5000
#c=7.0
#gamma=14.0
#delta=0.15

declare -a configurations=("22675 19.66  45.55  0.03" "19306 20.40  70.04  0.01" "17735 12.05  79.34  0.02" "17509 15.33 305.26  0.06" "3834 30.62 694.88  0.20" "4501 27.54 673.16  0.20" "401  31.25 721.02  0.21" "6676 35.65 775.72  0.20")

objectives=2
evaluations=60000
population=300
crossover=0.95
mutation=0.02

i=0
for configuration in "${configurations[@]}"
do
  for problem in $problems
  do
    java -cp dist/MECBA-Hyp.jar hyperheuristics.main.NSGAIIHyperheuristicMain $population $evaluations $crossover $mutation 1.0 0.08 TwoPointsCrossover,MultiMaskCrossover,PMXCrossover SwapMutation,SimpleInsertionMutation $problem $function $configuration $objectives false
    java -cp build/classes/ hyperheuristics.main.CompareHypervolumes
  done
  ((i++))
  echo $i
  mv experiment/2objectives/MultiArmedBandit/HYPERVOLUMES.txt experiment/2objectives/MultiArmedBandit/HYPERVOLUMES.txt.$i
  mv experiment/2objectives/HYPERVOLUMES.txt experiment/2objectives/HYPERVOLUMES.txt.$i
done

