/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author giovaniguizzo
 */
public class GeraHypervolume {

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
        try (FileWriter hypervolumeWriter = new FileWriter("resultado/HYPERVOLUME.txt")) {
            for (String problem : PROBLEMS) {
                hypervolumeWriter.write("\\multirow{2}{*}{" + problem.replace("_", "\\_") + "}");
                for (int numberOfObjectives : OBJECTIVES) {
                    hypervolumeWriter.write(" & " + numberOfObjectives);
                    HypervolumeHandler hypervolume = new HypervolumeHandler();

                    for (String algorithm : ALGORITHMS) {
                        String outputDir = "resultado/" + algorithm + "/" + problem + "_Comb_" + numberOfObjectives + "obj/";
                        hypervolume.addParetoFront(outputDir + "All_FUN_" + algorithm + "-" + problem);
                    }

                    for (String algorithm : ALGORITHMS) {
                        String outputDir = "resultado/" + algorithm + "/" + problem + "_Comb_" + numberOfObjectives + "obj/";
                        hypervolumeWriter.write(" & " + String.format("%.2e", hypervolume.getHypervolume((outputDir + "All_FUN_" + algorithm + "-" + problem), numberOfObjectives)).replace(".", ",").replace("e", "E"));
                    }
                    hypervolumeWriter.write(" \\\\ \\hline\n");
                }
            }
        }
    }

}
