#!/bin/bash

find -name "*.java" > sources.txt
javac @sources.txt

jar cf MECBA-Hyp.jar .

rm sources.txt
