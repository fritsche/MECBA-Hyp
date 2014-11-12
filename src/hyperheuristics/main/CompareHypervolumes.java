/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hyperheuristics.main;

import hyperheuristics.hypervolume.HypervolumeHandler;
import hyperheuristics.lowlevelheuristic.LowLevelHeuristic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author giovaniguizzo
 */
public class CompareHypervolumes {

    public static int EXECUTIONS = 30;
    public static String cfpath = "experiment/";
    public static String mabpath = "experiment/";

    public static void main(String[] args) throws IOException, InterruptedException {
        
        String[] problems;
        if(args.length == 0){
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
        }else {
            EXECUTIONS = Integer.parseInt(args[0]);
            cfpath = args[1];
            mabpath = args[2];
            problems = Arrays.copyOfRange(args, 3, args.length);
        }

        String[] heuristicFunctions = new String[]{
            LowLevelHeuristic.CHOICE_FUNCTION,
            LowLevelHeuristic.MULTI_ARMED_BANDIT
        };

        int[] numberOfObjectivesArray = new int[]{2, 4};

        for (int numberOfObjectives : numberOfObjectivesArray) {
//            hypervolumeComparison(problems, heuristicFunctions, numberOfObjectives);
            hypervolumeHyperheuristicsComparison(problems, heuristicFunctions, numberOfObjectives);
//        hypervolumeByGeneration(problems, heuristicFunctions, numberOfObjectives);
        }
    }

    private static void hypervolumeComparison(String[] problems, String[] heuristicFunctions, int numberOfObjectives) throws InterruptedException, IOException {
        for (String heuristicFunction : heuristicFunctions) {
            String path = (heuristicFunction.equals(LowLevelHeuristic.CHOICE_FUNCTION))?(cfpath):(mabpath);
            String outputDirectory = path + numberOfObjectives + "objectives/" + heuristicFunction + "/";

            try (FileWriter fileWriter = new FileWriter(outputDirectory + "HYPERVOLUMES.txt")) {

                int hyperheuristicBest = 0;
                int mecbaBest = 0;
                int tied = 0;

                int hyperheuristicBestMean = 0;
                int mecbaBestMean = 0;
                int tiedMean = 0;
                int equivalent = 0;
                for (String problem : problems) {
                    fileWriter.append("Hypervolume comparison for " + problem + ":\n");
                    fileWriter.append("\n");
                    HypervolumeHandler hypervolumeHandler = new HypervolumeHandler();

                    String hyperheuristicDirectory = outputDirectory + problem + "/";

                    String mecbaDirectory = "resultado/nsgaii/" + problem + "_Comb_" + numberOfObjectives + "obj/";

                    //Best hypervolume for PFknown
                    hypervolumeHandler.addParetoFront(hyperheuristicDirectory + "FUN.txt");
                    hypervolumeHandler.addParetoFront(mecbaDirectory + "All_FUN_nsgaii-" + problem);

                    double mecbaHypervolume = hypervolumeHandler.calculateHypervolume(mecbaDirectory + "All_FUN_nsgaii-" + problem, numberOfObjectives);
                    double hyperheuristicHypervolume = hypervolumeHandler.calculateHypervolume(hyperheuristicDirectory + "FUN.txt", numberOfObjectives);
                    fileWriter.append("MECBA PFknown: " + mecbaHypervolume + "\n");
                    fileWriter.append(heuristicFunction + " PFknown: " + hyperheuristicHypervolume + "\n");
                    if (mecbaHypervolume == hyperheuristicHypervolume) {
                        fileWriter.append("Best PFknown: Tied!\n");
                        tied++;
                    } else {
                        if (mecbaHypervolume > hyperheuristicHypervolume) {
                            fileWriter.append("Best PFknown: MECBA\n");
                            mecbaBest++;
                        } else {
                            fileWriter.append("Best PFknown: " + heuristicFunction + "\n");
                            hyperheuristicBest++;
                        }
                    }

                    //Best mean hypervolume
                    fileWriter.append("\n");

                    hypervolumeHandler.clear();

                    for (int i = 0; i < EXECUTIONS; i++) {
                        hypervolumeHandler.addParetoFront(hyperheuristicDirectory + "EXECUTION_" + i + "/FUN.txt");
                        hypervolumeHandler.addParetoFront(mecbaDirectory + "FUN_nsgaii-" + problem + "-" + i + ".NaoDominadas");
                    }

                    double[] mecbaHypervolumes = new double[EXECUTIONS];
                    double[] hyperheuristicHypervolumes = new double[EXECUTIONS];

                    mecbaHypervolume = 0;
                    hyperheuristicHypervolume = 0;

                    for (int i = 0; i < EXECUTIONS; i++) {
                        mecbaHypervolumes[i] = hypervolumeHandler.calculateHypervolume(mecbaDirectory + "FUN_nsgaii-" + problem + "-" + i + ".NaoDominadas", numberOfObjectives);
                        mecbaHypervolume += mecbaHypervolumes[i];
                        hyperheuristicHypervolumes[i] = hypervolumeHandler.calculateHypervolume(hyperheuristicDirectory + "EXECUTION_" + i + "/FUN.txt", numberOfObjectives);
                        hyperheuristicHypervolume += hyperheuristicHypervolumes[i];
                    }

                    mecbaHypervolume /= (double) EXECUTIONS;
                    hyperheuristicHypervolume /= (double) EXECUTIONS;

                    fileWriter.append("MECBA (Mean): " + mecbaHypervolume + "\n");
                    fileWriter.append(heuristicFunction + " (Mean): " + hyperheuristicHypervolume + "\n");

                    if (mecbaHypervolume == hyperheuristicHypervolume) {
                        fileWriter.append("Best (Mean): Tied!\n");
                        tiedMean++;
                    } else {
                        if (mecbaHypervolume > hyperheuristicHypervolume) {
                            fileWriter.append("Best (Mean): MECBA\n");
                            mecbaBestMean++;
                        } else {
                            fileWriter.append("Best (Mean): " + heuristicFunction + "\n");
                            hyperheuristicBestMean++;
                        }

                        String script = "";

                        script += "MECBA <- c(";
                        for (double value : mecbaHypervolumes) {
                            script += value + ",";
                        }
                        script = script.substring(0, script.lastIndexOf(",")) + ")";

                        script += "\n";

                        script += "MECBA_Hyp <- c(";
                        for (double value : hyperheuristicHypervolumes) {
                            script += value + ",";
                        }
                        script = script.substring(0, script.lastIndexOf(",")) + ")";

                        script += "\n";

                        script += "require(pgirmess)\n";
                        script += "AR1 <- cbind(MECBA, MECBA_Hyp)\n";
                        script += "result <- friedman.test(AR1)\n";
                        script += "m <- data.frame(result$statistic,result$p.value)\n";
                        script += "pos_teste <- friedmanmc(AR1)\n";
                        script += "print(pos_teste)";

                        try (FileWriter scriptWriter = new FileWriter(hyperheuristicDirectory + "temp_input.txt")) {
                            scriptWriter.append(script);
                        }
                        ProcessBuilder processBuilder = new ProcessBuilder("R", "--no-save");

                        File tempOutput = new File(hyperheuristicDirectory + "temp_output.txt");
                        processBuilder.redirectOutput(tempOutput);

                        File tempInput = new File(hyperheuristicDirectory + "temp_input.txt");
                        processBuilder.redirectInput(tempInput);

                        Process process = processBuilder.start();
                        process.waitFor();

                        Scanner scanner = new Scanner(tempOutput);
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            if (line.contains("FALSE")) {
                                equivalent++;
                                fileWriter.append("Statistical Equivalents (Friedman 5%)\n");
                                break;
                            }
                        }

                        tempInput.delete();
                        tempOutput.delete();
                    }

                    fileWriter.append("\n");
                    fileWriter.append("----------\n");
                    fileWriter.append("\n");
                }
                fileWriter.append("Problems: " + problems.length + "\n");
                fileWriter.append("\n");
                fileWriter.append("Tied PFknown: " + tied + "\n");
                fileWriter.append("MECBA PFknown: " + mecbaBest + "\n");
                fileWriter.append(heuristicFunction + " PFknown: " + hyperheuristicBest + "\n");
                fileWriter.append("\n");
                fileWriter.append("Tied (Mean): " + tiedMean + "\n");
                fileWriter.append("MECBA (Mean): " + mecbaBestMean + "\n");
                fileWriter.append(heuristicFunction + " (Mean): " + hyperheuristicBestMean + "\n");
                fileWriter.append("Statistically Equivalent: " + equivalent + "\n");
            }
        }
    }

    private static void hypervolumeHyperheuristicsComparison(String[] problems, String[] heuristicFunctions, int numberOfObjectives) throws InterruptedException, IOException {
        String outputDirectory = cfpath + numberOfObjectives + "objectives/";

        int mecbaBestCount = 0;
        int[] hyperheuristicBestCount = new int[heuristicFunctions.length];
        Arrays.fill(hyperheuristicBestCount, 0);

        int mecbaBestMeanCount = 0;
        int[] hyperheuristicBestMeanCount = new int[heuristicFunctions.length];
        Arrays.fill(hyperheuristicBestMeanCount, 0);

        try (FileWriter fileWriter = new FileWriter(outputDirectory + "HYPERVOLUMES.txt")) {
            for (String problem : problems) {
                fileWriter.append("Hypervolume comparison for " + problem + ":\n");
                fileWriter.append("\n");

                HypervolumeHandler hypervolumeHandler = new HypervolumeHandler();
                String mecbaDirectory = "resultado/nsgaii/" + problem + "_Comb_" + numberOfObjectives + "obj/";

                //Best PFknown hypervolume
                pfKnown:
                {
                    //Populate HypervolueHandler
                    hypervolumeHandler.addParetoFront(mecbaDirectory + "All_FUN_nsgaii-" + problem);

                    for (String heuristicFunction : heuristicFunctions) {
                        String path = (heuristicFunction.equals(LowLevelHeuristic.CHOICE_FUNCTION))?(cfpath):(mabpath);
                        path += numberOfObjectives + "objectives/";
                        String hyperheuristicDirectory = path + heuristicFunction + "/" + problem + "/";
                        hypervolumeHandler.addParetoFront(hyperheuristicDirectory + "FUN.txt");
                    }

                    //Calculate Hypervolume
                    double mecbaHypervolume = hypervolumeHandler.calculateHypervolume(mecbaDirectory + "All_FUN_nsgaii-" + problem, numberOfObjectives);

                    double[] hyperheuristicHypervolumes = new double[heuristicFunctions.length];
                    Arrays.fill(hyperheuristicHypervolumes, 0D);
                    for (int i = 0; i < heuristicFunctions.length; i++) {
                        String heuristicFunction = heuristicFunctions[i];
                        String path = (heuristicFunction.equals(LowLevelHeuristic.CHOICE_FUNCTION))?(cfpath):(mabpath);
                        path += numberOfObjectives + "objectives/";
                        String hyperheuristicDirectory = path + heuristicFunction + "/" + problem + "/";
                        hyperheuristicHypervolumes[i] = hypervolumeHandler.calculateHypervolume(hyperheuristicDirectory + "FUN.txt", numberOfObjectives);
                    }

                    //Write PFknown results
                    fileWriter.append("MECBA PFknown: " + mecbaHypervolume + "\n");
                    double maxHypervolume = mecbaHypervolume;

                    for (int i = 0; i < heuristicFunctions.length; i++) {
                        String heuristicFunction = heuristicFunctions[i];

                        fileWriter.append(heuristicFunction + " PFknown: " + hyperheuristicHypervolumes[i] + "\n");
                        if (hyperheuristicHypervolumes[i] > maxHypervolume) {
                            maxHypervolume = hyperheuristicHypervolumes[i];
                        }
                    }

                    fileWriter.append("Best PFknown:");

                    if (mecbaHypervolume == maxHypervolume) {
                        fileWriter.append(" MECBA");
                        mecbaBestCount++;
                    }

                    for (int i = 0; i < heuristicFunctions.length; i++) {
                        String heuristicFunction = heuristicFunctions[i];
                        if (hyperheuristicHypervolumes[i] == maxHypervolume) {
                            fileWriter.append(" " + heuristicFunction);
                            hyperheuristicBestCount[i]++;
                        }
                    }
                    fileWriter.append("\n");
                }

                //Best mean hypervolume
                mean:
                {
                    fileWriter.append("\n");

                    hypervolumeHandler.clear();

                    for (int i = 0; i < EXECUTIONS; i++) {
                        hypervolumeHandler.addParetoFront(mecbaDirectory + "FUN_nsgaii-" + problem + "-" + i + ".NaoDominadas");
                    }

                    for (String heuristicFunction : heuristicFunctions) {
                        String path = (heuristicFunction.equals(LowLevelHeuristic.CHOICE_FUNCTION))?(cfpath):(mabpath);
                        path += numberOfObjectives + "objectives/";
                        String hyperheuristicDirectory = path + heuristicFunction + "/" + problem + "/";
                        for (int j = 0; j < EXECUTIONS; j++) {
                            hypervolumeHandler.addParetoFront(hyperheuristicDirectory + "EXECUTION_" + j + "/FUN.txt");
                        }
                    }

                    double[] mecbaHypervolumes = new double[EXECUTIONS];
                    double mecbaHypervolume = 0;

                    double[][] hyperheuristicHypervolumes = new double[heuristicFunctions.length][EXECUTIONS];
                    for (double[] hyperheuristicHypervolume : hyperheuristicHypervolumes) {
                        Arrays.fill(hyperheuristicHypervolume, 0D);
                    }

                    double[] hyperheuristicMeanHypervolumes = new double[heuristicFunctions.length];
                    Arrays.fill(hyperheuristicMeanHypervolumes, 0D);

                    for (int i = 0; i < EXECUTIONS; i++) {
                        mecbaHypervolumes[i] = hypervolumeHandler.calculateHypervolume(mecbaDirectory + "FUN_nsgaii-" + problem + "-" + i + ".NaoDominadas", numberOfObjectives);
                        mecbaHypervolume += mecbaHypervolumes[i];
                        for (int j = 0; j < heuristicFunctions.length; j++) {
                            String path = (heuristicFunctions[j].equals(LowLevelHeuristic.CHOICE_FUNCTION))?(cfpath):(mabpath);
                            path += numberOfObjectives + "objectives/";
                            String hyperheuristicDirectory = path + heuristicFunctions[j] + "/" + problem + "/";
                            hyperheuristicHypervolumes[j][i] = hypervolumeHandler.calculateHypervolume(hyperheuristicDirectory + "EXECUTION_" + i + "/FUN.txt", numberOfObjectives);
                            hyperheuristicMeanHypervolumes[j] += hyperheuristicHypervolumes[j][i];
                        }
                    }

                    mecbaHypervolume /= (double) EXECUTIONS;
                    for (int i = 0; i < hyperheuristicMeanHypervolumes.length; i++) {
                        hyperheuristicMeanHypervolumes[i] /= (double) EXECUTIONS;
                    }

                    fileWriter.append("MECBA (Mean): " + mecbaHypervolume + "\n");
                    double maxHypervolume = mecbaHypervolume;
                    for (int i = 0; i < heuristicFunctions.length; i++) {
                        String heuristicFunction = heuristicFunctions[i];
                        fileWriter.append(heuristicFunction + " (Mean): " + hyperheuristicMeanHypervolumes[i] + "\n");
                        if (hyperheuristicMeanHypervolumes[i] > maxHypervolume) {
                            maxHypervolume = hyperheuristicMeanHypervolumes[i];
                        }
                    }

                    fileWriter.append("Best (Mean):");

                    if (mecbaHypervolume == maxHypervolume) {
                        fileWriter.append(" MECBA");
                        mecbaBestMeanCount++;
                    }

                    for (int i = 0; i < heuristicFunctions.length; i++) {
                        String heuristicFunction = heuristicFunctions[i];
                        if (hyperheuristicMeanHypervolumes[i] == maxHypervolume) {
                            fileWriter.append(" " + heuristicFunction);
                            hyperheuristicBestMeanCount[i]++;
                        }
                    }
                    fileWriter.append("\n");
                }
                fileWriter.append("\n");
                fileWriter.append("----------\n");
                fileWriter.append("\n");
            }
            fileWriter.append("Problems: " + problems.length + "\n");
            fileWriter.append("\n");
            fileWriter.append("Problems MECBA PFknown problems: " + mecbaBestCount + "\n");
            for (int i = 0; i < heuristicFunctions.length; i++) {
                String heuristicFunction = heuristicFunctions[i];
                fileWriter.append(heuristicFunction + " PFknown problems: " + hyperheuristicBestCount[i] + "\n");
            }
            fileWriter.append("\n");
            fileWriter.append("MECBA (Mean): " + mecbaBestMeanCount + "\n");
            for (int i = 0; i < heuristicFunctions.length; i++) {
                String heuristicFunction = heuristicFunctions[i];
                fileWriter.append(heuristicFunction + " (Mean): " + hyperheuristicBestMeanCount[i] + "\n");
            }
        }
    }

    private static void hypervolumeByGeneration(String[] problems, String[] heuristicFunctions, int numberOfObjectives) {
        for (String heuristicFunction : heuristicFunctions) {

            String path = (heuristicFunction.equals(LowLevelHeuristic.CHOICE_FUNCTION))?(cfpath):(mabpath);
            String outputDirectory = path + numberOfObjectives + "objectives/" + heuristicFunction + "/";
            for (String problem : problems) {
                HypervolumeHandler hypervolumeHandler = new HypervolumeHandler();
                String problemDirectory = outputDirectory + problem + "/";

                for (int i = 0; i < EXECUTIONS; i++) {
                    String executionDirectory = problemDirectory + "/EXECUTION_" + i + "/";

                    File generationDirectory = new File(executionDirectory + "GENERATIONS/");

                    for (File generation : generationDirectory.listFiles()) {
                        if (generation.getName().startsWith("GEN")) {
                            hypervolumeHandler.addParetoFront(generation.getPath());
                        }
                    }

                }

                for (int i = 0; i < EXECUTIONS; i++) {
                    String executionDirectory = problemDirectory + "/EXECUTION_" + i + "/";

                    File generationDirectory = new File(executionDirectory + "GENERATIONS/");

                    try (FileWriter hypervolumeWriter = new FileWriter(executionDirectory + "GENERATIONS_HYPERVOLUME.txt")) {
                        int generations = generationDirectory.list().length;

                        double[] hypervolumes = new double[generations];

                        for (int j = 1; j <= generations; j++) {
                            double hypervolume = hypervolumeHandler.calculateHypervolume(generationDirectory.getPath() + "/GEN_" + j + ".txt", numberOfObjectives);
                            hypervolumes[j - 1] = hypervolume;
                            hypervolumeWriter.append(hypervolume + "\n");
                        }
                        hypervolumeWriter.append("\nLast change to hypervolume in generation: ");

                        int lastGeneration = generations;
                        while (hypervolumes[lastGeneration - 1] == hypervolumes[generations - 1] && lastGeneration > 0) {
                            lastGeneration--;
                        }
                        lastGeneration++;
                        hypervolumeWriter.append(lastGeneration + "\n");
                    } catch (IOException ex) {
                        Logger.getLogger(NSGAIIHyperheuristicMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

}
