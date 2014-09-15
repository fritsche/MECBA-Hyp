package hyperheuristics.main;

import hyperheuristics.algorithm.NSGAIIHyperheuristic;
import hyperheuristics.comparators.LowLevelHeuristicComparatorFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import jmetal.base.Operator;
import jmetal.base.SolutionSet;
import jmetal.base.operator.crossover.Crossover;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.mutation.Mutation;
import jmetal.base.operator.mutation.MutationFactory;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.problems.CITO_CAITO;
import jmetal.problems.Combined2Objetives;
import jmetal.util.JMException;

public class NSGAIIHyperheuristicMain {

    public static void main(String[] args) throws
            JMException,
            SecurityException,
            IOException,
            ClassNotFoundException {

        String[] problems;

        String[] crossovers;

        String[] mutations;

        int populationSize;
        int maxEvaluations;
        double crossoverProbability;
        double mutationProbability;
        double alpha;
        double beta;
        String heuristicFunction;

        if (args.length == 10) {
            populationSize = Integer.parseInt(args[0]);
            maxEvaluations = Integer.parseInt(args[1]);
            crossoverProbability = Double.parseDouble(args[2]);
            mutationProbability = Double.parseDouble(args[3]);
            alpha = Double.parseDouble(args[4]);
            beta = Double.parseDouble(args[5]);
            crossovers = args[6].split(",");
            mutations = args[7].split(",");
            problems = args[8].split(",");
            heuristicFunction = args[9];
        } else {
            System.out.println("Not enough parameters. Inform the following:");
            System.out.println("\t 1 - Population Size (int);");
            System.out.println("\t 2 - Max Evaluations (int);");
            System.out.println("\t 3 - Crossover Probability (double);");
            System.out.println("\t 4 - Mutation Probability (double);");
            System.out.println("\t 5 - Alpha (double);");
            System.out.println("\t 6 - Beta (double);");
            System.out.println("\t 7 - Crossover Operators (String[] - comma separated, no spaces);");
            System.out.println("\t 8 - Mutation Operators (String[] - comma separated, no spaces);");
            System.out.println("\t 9 - Problems (String[] - comma separated, no spaces);");
            System.out.println("\t 10 - Heuristic Function (ChoiceFunction or MultiArmedBandit);");
            System.out.println();
            System.out.println("Would you like to execute the default parameters ('y' for 'yes' or anything for 'no')?");

            Scanner scanner = new Scanner(System.in);
            if (!"y".equals(scanner.nextLine())) {
                System.exit(0);
            }
            System.out.println();

            problems = new String[]{
                "OA_AJHotDraw",
                "OA_AJHsqldb",
                "OA_HealthWatcher",
                "OA_TollSystems",
                "OO_BCEL",
                "OO_JBoss",
                "OO_JHotDraw",
                "OO_MyBatis"
            };

            crossovers = new String[]{
                "TwoPointsCrossover",
                "MultiMaskCrossover",
                "PMXCrossover"
            };

            mutations = new String[]{
                "SwapMutation",
                "SimpleInsertionMutation"
            };

            populationSize = 100;
            maxEvaluations = 25000;
            crossoverProbability = 0.95;
            mutationProbability = 0.02;
            alpha = 1;
            beta = 4D / ((double) populationSize / 2D);

            heuristicFunction = LowLevelHeuristicComparatorFactory.CHOICE_FUNCTION;
        }

        System.out.println("Initializing experiments.");
        System.out.println("Parameters:");
        System.out.println("\tPopulationSize = " + populationSize);
        System.out.println("\tMax Evaluations = " + maxEvaluations);
        System.out.println("\tCrossover Probability = " + crossoverProbability);
        System.out.println("\tMutation Probability = " + mutationProbability);
        System.out.println("\tAlpha = " + alpha);
        System.out.println("\tBeta = " + beta);
        System.out.println("\tCrossover Operators = " + Arrays.toString(crossovers));
        System.out.println("\tMutation Operators = " + Arrays.toString(mutations));
        System.out.println("\tProblems = " + Arrays.toString(problems));
        System.out.println("\tHeuristic Function = " + heuristicFunction);

        for (String problemName : problems) {
            System.out.println();
            System.out.println("-------------------");
            System.out.println();
            System.out.println("Problem: " + problemName);
            System.out.println();

            String outputDirectory = "experiment/" + problemName + "/";
            createDirectory(outputDirectory);

            CITO_CAITO problem; // The problem to solve
            NSGAIIHyperheuristic algorithm; // The algorithm to use
            Operator selection; // Selection operator

            problem = new Combined2Objetives("problemas/" + problemName + ".txt");
            algorithm = new NSGAIIHyperheuristic(problem);

            //algorithm = new ssNSGAII(problem);
            // Algorithm parameters
            algorithm.setInputParameter("populationSize", populationSize);
            algorithm.setInputParameter("maxEvaluations", maxEvaluations);
            algorithm.setInputParameter("heuristicFunction", heuristicFunction);

            // Selection Operator 
            selection = SelectionFactory.getSelectionOperator("BinaryTournament2");

            // Add the operators to the algorithm
            algorithm.addOperator("selection", selection);

            //Create low level heuristics
            int lowLevelHeuristicNumber = 1;
            for (String crossoverName : crossovers) {
                for (String mutationName : mutations) {
                    HashMap<String, Object> parameters = new HashMap<>();

                    parameters.put("name", "h" + lowLevelHeuristicNumber++ + " [" + crossoverName + ", " + mutationName + "]");
                    parameters.put("alpha", alpha);
                    parameters.put("beta", beta);

                    Crossover crossover = CrossoverFactory.getCrossoverOperator(crossoverName);
                    crossover.setParameter("probability", crossoverProbability);
                    parameters.put("crossover", crossover);

                    Mutation mutation = MutationFactory.getMutationOperator(mutationName);
                    mutation.setParameter("probability", mutationProbability);
                    parameters.put("mutation", mutation);

                    algorithm.addLowLevelHeuristic(parameters);
                }
            }

            new File(outputDirectory + "LLH.txt").delete();
            try (FileWriter timeWriter = new FileWriter(outputDirectory + "TIME_EXECUTION.txt")) {

                SolutionSet allRuns = new SolutionSet();
                long allExecutionTime = 0;
                int[] allTimesApplied = new int[algorithm.getLowLevelHeuristicsSize()];

                for (int execution = 0; execution < 30; execution++) {
                    System.out.println("Execution: " + (execution + 1));
                    algorithm.clearLowLeverHeuristicsValues();
                    algorithm.setLowLevelHeuristicsRankPath(outputDirectory + "RANK_" + execution + ".txt");
                    algorithm.setLowLevelHeuristicsTimePath(outputDirectory + "TIME_" + execution + ".txt");

                    // Execute the Algorithm
                    long initTime = System.currentTimeMillis();
                    SolutionSet population = algorithm.execute();
                    long estimatedTime = System.currentTimeMillis() - initTime;

                    problem.removeDominadas(population);
                    problem.removeRepetidas(population);

                    // Result messages
                    population.printVariablesToFile(outputDirectory + "VAR_" + execution + ".txt");
                    population.printObjectivesToFile(outputDirectory + "FUN_" + execution + ".txt");
                    algorithm.printLowLevelHeuristicsInformation(outputDirectory + "LLH.txt", true);

                    timeWriter.append(estimatedTime + "\n");
                    allExecutionTime += estimatedTime;

                    allRuns = allRuns.union(population);
                    int[] executionTimesApplied = algorithm.getLowLevelHeuristicsNumberOfTimesApplied();
                    for (int i = 0; i < executionTimesApplied.length; i++) {
                        allTimesApplied[i] += executionTimesApplied[i];
                    }
                }

                System.out.println();
                System.out.println("End of execution for problem " + problemName + ".");
                System.out.println("Total time (seconds): " + allExecutionTime / 1000);
                System.out.println("Writing results.");
                problem.removeDominadas(allRuns);
                problem.removeRepetidas(allRuns);

                allRuns.printVariablesToFile(outputDirectory + "VAR_All.txt");
                allRuns.printObjectivesToFile(outputDirectory + "FUN_All.txt");

                timeWriter.append("\n");
                timeWriter.append("Total: " + allExecutionTime + "\n");
                timeWriter.append("Average: " + (double) ((double) allExecutionTime / (double) 30) + "\n");

                try (FileWriter timesAppliedWriter = new FileWriter(outputDirectory + "LLH.txt", true)) {
                    for (int i = 0; i < allTimesApplied.length; i++) {
                        int value = allTimesApplied[i];
                        timesAppliedWriter.append("h" + i + 1 + " " + value + "\n");
                    }
                }
            }
        }
    } //main

    private static void createDirectory(String directory) {
        File outputDirectoryFile = new File(directory);
        if (!outputDirectoryFile.exists()) {
            if (!outputDirectoryFile.getParentFile().exists()) {
                outputDirectoryFile.mkdirs();
            }
            outputDirectoryFile.mkdir();
        }
    }
} // NSGAII_main
