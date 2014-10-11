/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hyperheuristics.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import jmetal.qualityIndicator.R2;
import jmetal.qualityIndicator.util.MetricsUtil;

/**
 *
 * @author giovaniguizzo
 */
public class CalculaR2Script {

    /**
     * Calcula R2.
     *
     * @param args args[0] - Problem name, args[1] - Function name;
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {
        String problem = args[0];
        String function = args[1];

        double maxFitness0 = 29062;
        double maxFitness1 = 4251;

        MetricsUtil mu = new MetricsUtil();

        double[] maximumValues = new double[]{maxFitness0, maxFitness1};
        double[] minimumValues = new double[]{0, 0};

        double[][] front = mu.readFront("experiment/" + function + "/" + problem + "/FUN.txt");
        front = mu.getNormalizedFront(front, maximumValues, minimumValues);

        R2 r2 = new R2(10000);
        System.out.println(r2.R2(front, front));
    }

}
