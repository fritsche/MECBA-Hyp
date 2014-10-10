/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hyperheuristics.lowlevelheuristic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import jmetal.base.Operator;
import jmetal.base.Solution;
import jmetal.base.operator.comparator.DominanceComparator;
import jmetal.base.operator.crossover.Crossover;
import jmetal.base.operator.mutation.Mutation;
import jmetal.problems.CITO_CAITO;
import jmetal.util.JMException;

/**
 *
 * @author giovaniguizzo
 */
public class LowLevelHeuristic extends Operator {

    //Constants - Avaliable Heuristic Functions.
    public static final String CHOICE_FUNCTION = "ChoiceFunction";
    public static final String MULTI_ARMED_BANDIT = "MultiArmedBandit";

    /**
     * Rank weight.
     */
    private double alpha = 1;

    /**
     * Elapsed time weight.
     */
    private double beta = 1;

    //Sliding Time Window.
    private static int w = 0;
    private static double c = 0;
    private static double gamma = 0;
    private static double delta = 0;
    private static LowLevelHeuristic slidingWindowHeuristics[];
    private static double slidingWindowImprovement[];
    private static int i = 0;
    private static int it = 0;
    private static List<Integer> reboots = new ArrayList<>();

    //Empirical Rewards.
    private double q = 0;
    private double r = 0;
    private double biggest = 0;

    //Usage
    private final Comparator dominanceComparator;

    //Attributes
    private String name;
    private double rank;
    private double elapsedTime;
    private int numberOfTimesApplied;

    //Aggregation
    private Crossover crossoverOperator;
    private Mutation mutationOperator;

    public LowLevelHeuristic(HashMap<String, Object> parameters) {
        super();
        this.parameters_ = parameters;
        this.dominanceComparator = new DominanceComparator();

        this.rank = 0;
        this.elapsedTime = 0;
        this.numberOfTimesApplied = 0;

        if (parameters.containsKey("name")) {
            this.name = (String) parameters.get("name");
        }

        if (parameters.containsKey("crossover")) {
            this.crossoverOperator = (Crossover) parameters.get("crossover");
        }

        if (parameters.containsKey("mutation")) {
            this.mutationOperator = (Mutation) parameters.get("mutation");
        }

        if (parameters.containsKey("alpha")) {
            alpha = (double) parameters.get("alpha");
        }

        if (parameters.containsKey("beta")) {
            beta = (double) parameters.get("beta");
        }

        if (parameters.containsKey("w")) {
            w = (int) parameters.get("w");
            if (slidingWindowHeuristics == null) {
                slidingWindowHeuristics = new LowLevelHeuristic[w];
                slidingWindowImprovement = new double[w];
            }
        }

        if (parameters.containsKey("c")) {
            c = (double) parameters.get("c");
        }
        if (parameters.containsKey("gamma")) {
            gamma = (double) parameters.get("gamma");
        }
        if (parameters.containsKey("delta")) {
            delta = (double) parameters.get("delta");
        }
    }

    /*
     Getters and setters.
     */
    public double getRank() {
        return rank;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public int getNumberOfTimesApplied() {
        return numberOfTimesApplied;
    }

    public Crossover getCrossoverOperator() {
        return crossoverOperator;
    }

    public Mutation getMutationOperator() {
        return mutationOperator;
    }

    public String getName() {
        return name;
    }

    /*
     Methods for updating values.
     */
    public void executed() {
        updateElapsedTime(true);
        this.numberOfTimesApplied++;
        it++;
    }

    public void notExecuted() {
        updateElapsedTime(false);
    }

    public void updateElapsedTime(boolean executed) {
        if (executed) {
            this.elapsedTime = 0;
        } else {
            this.elapsedTime += 1;
        }
    }

    public void updateRank(Solution[] parents, Solution[] offsprings) {
        rank = 0;
        for (Solution parent : parents) {
            for (Solution offspring : offsprings) {
                rank += dominanceComparator.compare(parent, offspring);
            }
        }
    }

    public double getChoiceFunctionValue() {
        return (alpha * rank) + (beta * elapsedTime);
    }

    public void clearAllValues() {
        this.rank = 0;
        this.elapsedTime = 0;
        this.numberOfTimesApplied = 0;
    }

    /*
     Override standard methods.
     */
    @Override
    public String toString() {
        return name + " - R: " + rank + ", T: " + elapsedTime + ", CH: " + getChoiceFunctionValue() + ", NA: " + numberOfTimesApplied;
    }

    @Override
    public Object execute(Object object, CITO_CAITO problem) throws JMException {
        Solution[] parents = (Solution[]) object;

        Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents, problem);
        mutationOperator.execute(offSpring[0], problem);
        mutationOperator.execute(offSpring[1], problem);

        executed();

        return offSpring;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LowLevelHeuristic other = (LowLevelHeuristic) obj;
        return Objects.equals(this.name, other.name);
    }

    public void updateReward() {
        double max = -Double.MAX_VALUE;
        int j = 0;
        LowLevelHeuristic h = slidingWindowHeuristics[j];
        double im = slidingWindowImprovement[j];
        for (; j < w && h != null; j++) {
            if (h.equals(this) && im > max) {
                max = im;
            }
            h = slidingWindowHeuristics[j];
            im = slidingWindowImprovement[j];
        }
        r = max;
    }

    public void creditAssignment() {
        if (w != 0) {
            LowLevelHeuristic temp = slidingWindowHeuristics[i];
            slidingWindowHeuristics[i] = this;
            slidingWindowImprovement[i] = this.rank;
            if (temp != null) {
                temp.updateReward();
            }
            this.updateReward();
            i++;
            i %= w;

            q = (r + q * numberOfTimesApplied) / numberOfTimesApplied;
            double m = r - q + delta;
            if (m >= biggest) {
                biggest = m;
            }
            if (biggest - m > gamma) { // restart
                q = 0;
                r = 0;
                biggest = 0;
                slidingWindowHeuristics = new LowLevelHeuristic[w];
                slidingWindowImprovement = new double[w];
                i = 0;
                reboots.add(it);
            }
        }
    }

    public double getMultiArmedBanditValue() {
        return q + c * Math.sqrt((2 * Math.log(it)) / numberOfTimesApplied);
    }
}
