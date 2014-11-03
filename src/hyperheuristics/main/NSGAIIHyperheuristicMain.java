package hyperheuristics.main;

import hyperheuristics.algorithm.NSGAIIHyperheuristic;
import hyperheuristics.lowlevelheuristic.LowLevelHeuristic;
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
import jmetal.problems.Combined4Objectives;
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

        int executions = 30;
        int populationSize;
        int maxEvaluations;
        int numberOfObjectives;
        double crossoverProbability;
        double mutationProbability;
        double alpha;
        double beta;
        String heuristicFunction;
        int w;
        double c;
        double gamma;
        double delta;
        boolean saveGenerations;

        if (args.length == 16) {
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
            w = Integer.parseInt(args[10]);
            c = Double.parseDouble(args[11]);
            gamma = Double.parseDouble(args[12]);
            delta = Double.parseDouble(args[13]);
            numberOfObjectives = Integer.parseInt(args[14]);
            saveGenerations = Boolean.parseBoolean(args[15]);
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
            System.out.println("\t 11 - Sliding window size W (int);");
            System.out.println("\t 12 - Scaling factor C (double);");
            System.out.println("\t 13 - Gamma in the PH test (double);");
            System.out.println("\t 14 - Tolerance parameter Delta (double);");
            System.out.println("\t 15 - Number of objectives (int - 2 or 4);");
            System.out.println("\t 16 - Save generations? (boolean);");
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

            numberOfObjectives = 2;
            populationSize = 100;
            maxEvaluations = 25000;
            crossoverProbability = 0.95;
            mutationProbability = 0.02;
            alpha = 1;
            beta = 4D / ((double) populationSize / 2D);

            heuristicFunction = LowLevelHeuristic.CHOICE_FUNCTION;

            w = maxEvaluations / 5; // 5000
            c = 7;
            gamma = 14;
            delta = 0.15;
            saveGenerations = false;
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
        System.out.println("\tSliding window size W = " + w);
        System.out.println("\tScaling factor C = " + c);
        System.out.println("\tGamma in the PH test = " + gamma);
        System.out.println("\tTolerance parameter Delta = " + delta);

        for (String problemName : problems) {
            System.out.println();
            System.out.println("-------------------");
            System.out.println();
            System.out.println("Problem: " + problemName);
            System.out.println();

            String outputDirectory = "experiment/" + numberOfObjectives + "objectives/" + heuristicFunction + "/" + problemName + "/";
            createDirectory(outputDirectory);

            CITO_CAITO problem; // The problem to solve
            NSGAIIHyperheuristic algorithm; // The algorithm to use
            Operator selection; // Selection operator

            if (numberOfObjectives == 2) {
                problem = new Combined2Objetives("problemas/" + problemName + ".txt");
            } else if (numberOfObjectives == 4) {
                problem = new Combined4Objectives("problemas/" + problemName + ".txt");
            } else {
                problem = null;
                System.err.println("Wrong number of objectives (" + numberOfObjectives + "). Available values: 2 or 4.");
                System.exit(1);
            }
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
            String[] lowLevelHeuristicNames = new String[crossovers.length * mutations.length];
            for (String crossoverName : crossovers) {
                for (String mutationName : mutations) {
                    HashMap<String, Object> parameters = new HashMap<>();

                    String name = "h" + lowLevelHeuristicNumber + " [" + crossoverName + ", " + mutationName + "]";
                    lowLevelHeuristicNames[lowLevelHeuristicNumber - 1] = name;

                    parameters.put("name", name);
                    parameters.put("alpha", alpha);
                    parameters.put("beta", beta);
                    parameters.put("w", w);
                    parameters.put("c", c);
                    parameters.put("gamma", gamma);
                    parameters.put("delta", delta);

                    Crossover crossover = CrossoverFactory.getCrossoverOperator(crossoverName);
                    crossover.setParameter("probability", crossoverProbability);
                    parameters.put("crossover", crossover);

                    Mutation mutation = MutationFactory.getMutationOperator(mutationName);
                    mutation.setParameter("probability", mutationProbability);
                    parameters.put("mutation", mutation);

                    algorithm.addLowLevelHeuristic(parameters);

                    lowLevelHeuristicNumber++;
                }
            }

            new File(outputDirectory + "LLH.txt").delete();
            try (FileWriter rebootWriter = new FileWriter(outputDirectory + "REBOOTS.txt")) {
                try (FileWriter timeWriter = new FileWriter(outputDirectory + "TIME_EXECUTION.txt")) {

                    SolutionSet allRuns = new SolutionSet();
                    long allExecutionTime = 0;
                    int[] allTimesApplied = new int[algorithm.getLowLevelHeuristicsSize()];

                    for (int execution = 0; execution < executions; execution++) {
                        String executionDirectory = outputDirectory + "EXECUTION_" + execution + "/";
                        createDirectory(executionDirectory);

                        System.out.println("Execution: " + (execution + 1));
                        algorithm.clearLowLeverHeuristicsValues();
                        algorithm.setLowLevelHeuristicsRankPath(executionDirectory + "RANK.txt");
                        algorithm.setLowLevelHeuristicsTimePath(executionDirectory + "TIME.txt");
                        algorithm.setDebugPath(executionDirectory + "DEBUG");

                        if (saveGenerations) {
                            String generationsDirectory = executionDirectory + "GENERATIONS/";
                            createDirectory(generationsDirectory);
                            algorithm.setGenerationsOutputDirectory(generationsDirectory);
                        }

                        // Execute the Algorithm
                        long initTime = System.currentTimeMillis();
                        SolutionSet population = algorithm.execute();
                        long estimatedTime = System.currentTimeMillis() - initTime;

                        problem.removeDominadas(population);
                        problem.removeRepetidas(population);

                        // Result messages
                        population.printVariablesToFile(executionDirectory + "VAR.txt");
                        population.printObjectivesToFile(executionDirectory + "FUN.txt");
                        algorithm.printLowLevelHeuristicsInformation(executionDirectory + "LLH.txt");

                        rebootWriter.append(LowLevelHeuristic.getREBOOTS() + "\n");
                        rebootWriter.flush();

                        timeWriter.append(estimatedTime + "\n");
                        timeWriter.flush();
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

                    allRuns.printVariablesToFile(outputDirectory + "VAR.txt");
                    allRuns.printObjectivesToFile(outputDirectory + "FUN.txt");

                    timeWriter.append("\n");
                    timeWriter.append("Total: " + allExecutionTime + "\n");
                    timeWriter.append("Average: " + (double) ((double) allExecutionTime / (double) executions) + "\n");

                    try (FileWriter timesAppliedWriter = new FileWriter(outputDirectory + "LLH.txt")) {
                        for (int i = 0; i < allTimesApplied.length; i++) {
                            timesAppliedWriter.append(lowLevelHeuristicNames[i] + " " + allTimesApplied[i] + "\n");
                        }
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
