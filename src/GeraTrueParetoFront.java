/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.FileWriter;
import java.io.IOException;
import jmetal.base.SolutionSet;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.NonDominatedSolutionList;

/**
 *
 * @author giovaniguizzo
 */
public class GeraTrueParetoFront {

    private static final String[] PROBLEMS = new String[]{
        "OA_AJHotDraw",
        "OA_AJHsqldb",
        "OA_HealthWatcher",
        "OA_TollSystems",
        "OO_BCEL",
        "OO_JBoss",
        "OO_JHotDraw",
        "OO_MyBatis"
    };

    public static String[] ALGORITHMS = new String[]{
        "nsgaii",
        "paes",
        "spea2"
    };

    public static int[] OBJECTIVES = new int[]{
        2,
        4
    };

    public static void main(String[] args) throws IOException {
        MetricsUtil metricsUtil = new MetricsUtil();
        for (String problem : PROBLEMS) {
            for (int numberOfObjectives : OBJECTIVES) {
                try (FileWriter paretoWriter = new FileWriter("resultado/TRUE_PARETO_" + problem + numberOfObjectives + ".txt")) {
                    NonDominatedSolutionList list = new NonDominatedSolutionList();

                    for (String algorithm : ALGORITHMS) {
                        String outputDir = "resultado/" + algorithm + "/" + problem + "_Comb_" + numberOfObjectives + "obj/";
                        list.addAll(metricsUtil.readNonDominatedSolutionSet(outputDir + "All_FUN_" + algorithm + "-" + problem));
                    }

                    double[][] pareto = list.writeObjectivesToMatrix();
//                    for (double[] line : pareto) {
//                        for (double objective : line) {
//                            paretoWriter.write(String.valueOf(objective).replace(".", ",") + "\t");
//                        }
//                        paretoWriter.write("\n");
//                    }
//
//                    paretoWriter.write("\n");

                    paretoWriter.write(problem + " " + numberOfObjectives + "obj & " + pareto.length + " & ");

                    for (String algorithm : ALGORITHMS) {
//                        paretoWriter.write(algorithm + "\t");
                        String outputDir = "resultado/" + algorithm + "/" + problem + "_Comb_" + numberOfObjectives + "obj/";
                        SolutionSet solutionSet = metricsUtil.readNonDominatedSolutionSet(outputDir + "All_FUN_" + algorithm + "-" + problem);
                        double[][] objectivesMatrix = solutionSet.writeObjectivesToMatrix();
                        int count = 0;
                        for (double[] line : objectivesMatrix) {
                            if (metricsUtil.distanceToClosedPoint(line, pareto) == 0) {
                                count++;
                            }
                        }
                        paretoWriter.write(count + " & ");
                    }
                    paretoWriter.write("\\hline");
                }
            }
        }
    }

}
