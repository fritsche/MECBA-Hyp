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

alpha=1.0
beta=0.021

w=12000
c=7.0

objectivesArray="2
4"

evaluations=60000
population=300
crossover=0.95
mutation=0.02

executions=10
path="experiment/"


java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path $path $path 2 ${problems} &
java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions ChoiceFunction0.0801$path $path ChoiceFunction0.0801$path 4 ${problems} & 

java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions ChoiceFunction0.0701$path $path ChoiceFunction0.0701$path 2 ${problems} & 
java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions ChoiceFunction0.0701$path $path ChoiceFunction0.0701$path 4 ${problems} & 

java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path MultiArmedBandit36.7468$path MultiArmedBandit36.7468$path 2 ${problems} & 
java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path MultiArmedBandit36.7468$path MultiArmedBandit36.7468$path 4 ${problems} & 

java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path MultiArmedBandit44.4106$path MultiArmedBandit44.4106$path 2 ${problems} & 
java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path MultiArmedBandit44.4106$path MultiArmedBandit44.4106$path 4 ${problems} & 

java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path MultiArmedBanditw43447c53.9865$path MultiArmedBanditw43447c53.9865$path 2 ${problems} &
java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path MultiArmedBanditw43447c53.9865$path MultiArmedBanditw43447c53.9865$path 4 ${problems} & 

java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path MultiArmedBanditw42708c54.7191$path MultiArmedBanditw42708c54.7191$path 2 ${problems} &
java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path MultiArmedBanditw42708c54.7191$path MultiArmedBanditw42708c54.7191$path 4 ${problems} & 

java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path MultiArmedBanditw51263c20.5644$path MultiArmedBanditw51263c20.5644$path 2 ${problems} & 
java -cp dist/MECBA-Hyp.jar hyperheuristics.main.CompareHypervolumes $executions $path MultiArmedBanditw51263c20.5644$path MultiArmedBanditw51263c20.5644$path 4 ${problems} & 

