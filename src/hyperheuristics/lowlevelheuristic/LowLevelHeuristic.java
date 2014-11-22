/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hyperheuristics.lowlevelheuristic;

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

    /**
     * Sliding window size.
     */
    private static int W = 0;
    private static double c = 0;

    /**
     * Sliding window.
     */
    private static LowLevelHeuristic SLIDING_WINDOW_HEURISTIC[];

    /**
     * Sliding window improvement.
     */
    private static double SLIDING_WINDOW_IMPROVEMENT[];

    /**
     * Sliding window index.
     */
    private static int I = 0;

    /**
     * Total number of low level heuristics executions.
     */
    private static int IT = 0;
    private static int REBOOTS = 0;
    private static double SUM_N = 0;

    //Empirical Rewards.
    private double q = 0;
    private double r = 0;
    private double aux = 0;

    //Usage
    private final Comparator dominanceComparator;

    //Attributes
    private String name;
    private double rank;
    private double elapsedTime;
    private int numberOfTimesApplied;
    private int lastTimeApplied;
    private double n;

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
        this.lastTimeApplied = 0;
        this.n = 1;

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
            W = (int) parameters.get("w");
            if (SLIDING_WINDOW_HEURISTIC == null) {
                SLIDING_WINDOW_HEURISTIC = new LowLevelHeuristic[W];
                SLIDING_WINDOW_IMPROVEMENT = new double[W];
            }
        }

        if (parameters.containsKey("c")) {
            c = (double) parameters.get("c");
        }
    }

    /*
     Getters and setters.
     */
    public double getQ() {
        return q;
    }

    public double getAux() {
        return aux;
    }

    public static int getREBOOTS() {
        return REBOOTS;
    }

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
        IT++;
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
        rank += 4;
        rank *= 0.125;
    }

    public double getChoiceFunctionValue() {
        return (alpha * rank) + (beta * elapsedTime);
    }

    public static void clearAllStaticValues() {
        reinitializeStatic();
        IT = 0;
        REBOOTS = 0;
    }

    public void clearAllValues() {
        reinitialize();
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
        if (mutationOperator != null) {
            mutationOperator.execute(offSpring[0], problem);
            mutationOperator.execute(offSpring[1], problem);
        }

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
        LowLevelHeuristic h = SLIDING_WINDOW_HEURISTIC[j];
        double im = SLIDING_WINDOW_IMPROVEMENT[j];
        for (; j < W && h != null; j++) {
            if (h.equals(this) && im > max) {
                max = im;
            }
            h = SLIDING_WINDOW_HEURISTIC[j];
            im = SLIDING_WINDOW_IMPROVEMENT[j];
        }
        r = max;
    }

    public void creditAssignment(List<LowLevelHeuristic> heuristics) {
        if (W != 0) {
            LowLevelHeuristic temp = SLIDING_WINDOW_HEURISTIC[I];
            SLIDING_WINDOW_HEURISTIC[I] = this;
            SLIDING_WINDOW_IMPROVEMENT[I] = this.rank;
            if (temp != null) {
                temp.updateReward();
            }
            this.updateReward();
            I++;
            I %= W;

            q = (double) q * (W / (double) W + (IT - lastTimeApplied)) + r * (1.0 / (double) (n + 1));
            n = (double) n * (W / ((double) W + (IT - lastTimeApplied)) + (1.0 / (double) (n + 1)));

            SUM_N = 0;
            for (LowLevelHeuristic heuristic : heuristics) {
                SUM_N += heuristic.n;
            }
        }
    }

    private void reinitialize() {
        this.rank = 0;
        this.elapsedTime = 0;
        this.numberOfTimesApplied = 0;
        this.q = 0;
        this.r = 0;
    }

    private static void reinitializeStatic() {
        SLIDING_WINDOW_HEURISTIC = new LowLevelHeuristic[W];
        SLIDING_WINDOW_IMPROVEMENT = new double[W];
        I = 0;
        IT = 0;
        REBOOTS++;
    }

    public double getMultiArmedBanditValue() {
        aux = (double) Math.sqrt((2.0 * Math.log(SUM_N)) / (double) n);
        return (double) q + c * aux;
    }
}
