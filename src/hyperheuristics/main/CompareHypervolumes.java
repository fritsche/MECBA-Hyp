/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hyperheuristics.main;

import hyperheuristics.hypervolume.HypervolumeHandler;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author giovaniguizzo
 */
public class CompareHypervolumes {

    public static void main(String[] args) throws IOException, InterruptedException {
        String[] problems = new String[]{
            //            "OA_AJHotDraw",
            "OA_AJHsqldb",
            //            "OA_HealthWatcher",
            //            "OA_TollSystems",
            "OO_BCEL",
            //            "OO_JBoss",
            //            "OO_JHotDraw",
            "OO_MyBatis"
        };

        try (FileWriter fileWriter = new FileWriter("experiment/HYPERVOLUMES.txt")) {
            int numberOfObjectives = 2;

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

                String hyperheuristicDirectory = "experiment/" + problem + "/";
                String mecbaDirectory = "resultado/nsgaii/" + problem + "_Comb_2obj/";

                //Best hypervolume for PFknown
                hypervolumeHandler.addParetoFront(hyperheuristicDirectory + "FUN.txt");
                hypervolumeHandler.addParetoFront(mecbaDirectory + "All_FUN_nsgaii-" + problem);

                double mecbaHypervolume = hypervolumeHandler.calculateHypervolume(mecbaDirectory + "All_FUN_nsgaii-" + problem, numberOfObjectives);
                double hyperheuristicHypervolume = hypervolumeHandler.calculateHypervolume(hyperheuristicDirectory + "FUN.txt", numberOfObjectives);
                fileWriter.append("MECBA (PFknown): " + mecbaHypervolume + "\n");
                fileWriter.append("MECBA-Hyp (PFknown): " + hyperheuristicHypervolume + "\n");
                if (mecbaHypervolume == hyperheuristicHypervolume) {
                    fileWriter.append("Best (PFknown): Tied!\n");
                    tied++;
                } else {
                    if (mecbaHypervolume > hyperheuristicHypervolume) {
                        fileWriter.append("Best (PFknown): MECBA\n");
                        mecbaBest++;
                    } else {
                        fileWriter.append("Best (PFknown): MECBA-Hyp\n");
                        hyperheuristicBest++;
                    }
                }

                //Best mean hypervolume
                fileWriter.append("\n");

                hypervolumeHandler.clear();

                for (int i = 0; i < 30; i++) {
                    hypervolumeHandler.addParetoFront(hyperheuristicDirectory + "EXECUTION_" + i + "/FUN.txt");
                    hypervolumeHandler.addParetoFront(mecbaDirectory + "FUN_nsgaii-" + problem + "-" + i + ".NaoDominadas");
                }

                double[] mecbaHypervolumes = new double[30];
                double[] hyperheuristicHypervolumes = new double[30];

                mecbaHypervolume = 0;
                hyperheuristicHypervolume = 0;

                for (int i = 0; i < 30; i++) {
                    mecbaHypervolumes[i] = hypervolumeHandler.calculateHypervolume(mecbaDirectory + "FUN_nsgaii-" + problem + "-" + i + ".NaoDominadas", numberOfObjectives);
                    mecbaHypervolume += mecbaHypervolumes[i];
                    hyperheuristicHypervolumes[i] = hypervolumeHandler.calculateHypervolume(hyperheuristicDirectory + "EXECUTION_" + i + "/FUN.txt", numberOfObjectives);
                    hyperheuristicHypervolume += hyperheuristicHypervolumes[i];
                }

                mecbaHypervolume /= 30D;
                hyperheuristicHypervolume /= 30D;

                fileWriter.append("MECBA (Mean): " + mecbaHypervolume + "\n");
                fileWriter.append("MECBA-Hyp (Mean): " + hyperheuristicHypervolume + "\n");

                if (mecbaHypervolume == hyperheuristicHypervolume) {
                    fileWriter.append("Best (Mean): Tied!\n");
                    tiedMean++;
                } else {
                    if (mecbaHypervolume > hyperheuristicHypervolume) {
                        fileWriter.append("Best (Mean): MECBA\n");
                        mecbaBestMean++;
                    } else {
                        fileWriter.append("Best (Mean): MECBA-Hyp\n");
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

                    try (FileWriter scriptWriter = new FileWriter("experiment/temp_input.txt")) {
                        scriptWriter.append(script);
                    }
                    ProcessBuilder processBuilder = new ProcessBuilder("R", "--no-save");

                    File tempOutput = new File("experiment/temp_output.txt");
                    processBuilder.redirectOutput(tempOutput);

                    File tempInput = new File("experiment/temp_input.txt");
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
            fileWriter.append("Tied (PFknown): " + tied + "\n");
            fileWriter.append("MECBA (PFknown): " + mecbaBest + "\n");
            fileWriter.append("MECBA-Hyp (PFknown): " + hyperheuristicBest + "\n");
            fileWriter.append("\n");
            fileWriter.append("Tied (Mean): " + tiedMean + "\n");
            fileWriter.append("MECBA (Mean): " + mecbaBestMean + "\n");
            fileWriter.append("MECBA-Hyp (Mean): " + hyperheuristicBestMean + "\n");
            fileWriter.append("Statistically Equivalent: " + equivalent + "\n");
        }
    }

}
