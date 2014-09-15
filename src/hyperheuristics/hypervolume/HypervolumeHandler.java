package hyperheuristics.hypervolume;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import jmetal.base.SolutionSet;
import jmetal.qualityIndicator.Hypervolume;
import jmetal.qualityIndicator.util.MetricsUtil;

/**
 *
 * @author giovaniguizzo
 */
public class HypervolumeHandler {

    private SolutionSet population;
    private final MetricsUtil metricUtil;
    private final Hypervolume hypervolume;
    private double[][] worstFront;

    public HypervolumeHandler() {
        this.population = new SolutionSet();
        this.metricUtil = new MetricsUtil();
        this.hypervolume = new Hypervolume();
    }

    public void addParetoFront(SolutionSet front) {
        population = population.union(front);
        worstFront = null;
    }

    public void addParetoFront(String path) {
        addParetoFront(metricUtil.readNonDominatedSolutionSet(path));
    }

    public double calculateHypervolume(String frontPath, int numberOfObjectives) {
        return calculateHypervolume(metricUtil.readNonDominatedSolutionSet(frontPath), numberOfObjectives);
    }

    public double calculateHypervolume(SolutionSet front, int numberOfObjectives) {
        if (population.size() != 0) {
            return hypervolume.hypervolume(front.writeObjectivesToMatrix(), getWorstFront(numberOfObjectives), numberOfObjectives);
        }
        return 0D;
    }

    public double[][] getWorstFront(int numberOfObjectives) {
        if (worstFront == null) {
            double[][] populationMatrix = population.writeObjectivesToMatrix();
            if (populationMatrix.length != 0) {
                double[] max = metricUtil.getMaximumValues(populationMatrix, numberOfObjectives);
                worstFront = new double[numberOfObjectives][numberOfObjectives];
                for (int i = 0; i < numberOfObjectives; i++) {
                    double objective = max[i];
                    worstFront[i][i] = objective + 1;
                    for (int j = 0; j < numberOfObjectives; j++) {
                        if (i != j) {
                            worstFront[i][j] = 0;
                        }
                    }
                }
            }
        }
        return worstFront;
    }

}
