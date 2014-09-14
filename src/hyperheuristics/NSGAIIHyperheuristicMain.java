package hyperheuristics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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

        String[] software = {
            "OA_AJHotDraw",
            "OA_AJHsqldb",
            "OA_HealthWatcher",
            "OA_TollSystems",
            "OO_BCEL",
            "OO_JBoss",
            "OO_JHotDraw",
            "OO_MyBatis"
        };

        String[] crossovers = {
            "TwoPointCrossOver"
        };

        String[] mutations = {
            "Swap"
        };

        int populationSize = 100;
        int maxEvaluations = 25000;
        double crossoverProbability = 0.95;
        double mutationProbability = 0.02;
        double alpha = 1;
        double beta = 4 / (populationSize / 2);

        for (String executingSoftware : software) {

            String outputDirectory = "experiment/" + executingSoftware + "/";

            CITO_CAITO problem; // The problem to solve
            NSGAIIHyperheuristic algorithm; // The algorithm to use
            Operator selection; // Selection operator

            problem = new Combined2Objetives(executingSoftware);
            algorithm = new NSGAIIHyperheuristic(problem);

            //algorithm = new ssNSGAII(problem);
            // Algorithm parameters
            algorithm.setInputParameter("populationSize", populationSize);
            algorithm.setInputParameter("maxEvaluations", maxEvaluations);

            // Selection Operator 
            selection = SelectionFactory.getSelectionOperator("BinaryTournament2");

            // Add the operators to the algorithm
            algorithm.addOperator("selection", selection);

            int lowLevelHeuristicNumber = 1;
            for (String crossoverName : crossovers) {
                for (String mutationName : mutations) {
                    HashMap<String, Object> parameters = new HashMap<>();

                    parameters.put("name", "h" + lowLevelHeuristicNumber++);
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

//            // Mutation and Crossover for Real codification 
//            crossover = CrossoverFactory.getCrossoverOperator("TwoPointsCrossover");
//            crossover.setParameter("probability", 0.95);
//            // Mutation
//            mutation = MutationFactory.getMutationOperator("SwapMutation");
//            mutation.setParameter("probability", 0.02);
            try (FileWriter timeWriter = new FileWriter(outputDirectory + "TIME.txt"); FileWriter timesAppliedWriter = new FileWriter(outputDirectory + "LLH.txt")) {

                SolutionSet allRuns = new SolutionSet();
                long allExecutionTime = 0;
                int[] allTimesApplied = new int[algorithm.getLowLevelHeuristicsSize()];

                for (int execution = 0; execution < 30; execution++) {
                    algorithm.clearLowLeverHeuristicsValues();

                    // Execute the Algorithm
                    long initTime = System.currentTimeMillis();
                    SolutionSet population = algorithm.execute();
                    long estimatedTime = System.currentTimeMillis() - initTime;

                    problem.removeDominadas(population);
                    problem.removeRepetidas(population);

                    // Result messages
                    population.printVariablesToFile(outputDirectory + "VAR_" + execution + ".txt");
                    population.printObjectivesToFile(outputDirectory + "FUN_" + execution + ".txt");
                    timesAppliedWriter.append("Execution: " + execution + "\n\n");
                    algorithm.printLowLevelHeuristicsInformation(outputDirectory + "LLH.txt", true);
                    timesAppliedWriter.append("-------------------\n\n");

                    timeWriter.append(estimatedTime + "\n");
                    allExecutionTime += estimatedTime;

                    allRuns = allRuns.union(population);
                    int[] executionTimesApplied = algorithm.getLowLevelHeuristicsNumberOfTimesApplied();
                    for (int i = 0; i < executionTimesApplied.length; i++) {
                        allTimesApplied[i] += executionTimesApplied[i];
                    }
                }

                problem.removeDominadas(allRuns);
                problem.removeRepetidas(allRuns);

                allRuns.printVariablesToFile(outputDirectory + "VAR_All.txt");
                allRuns.printObjectivesToFile(outputDirectory + "FUN_All.txt");

                timeWriter.append("\n");
                timeWriter.append("Total: " + allExecutionTime + "\n");
                timeWriter.append("Average: " + (double) ((double) allExecutionTime / (double) 30) + "\n");

                for (int i = 0; i < allTimesApplied.length; i++) {
                    int value = allTimesApplied[i];
                    timesAppliedWriter.append(value + "\n");
                }
            }
        }
    } //main
} // NSGAII_main
