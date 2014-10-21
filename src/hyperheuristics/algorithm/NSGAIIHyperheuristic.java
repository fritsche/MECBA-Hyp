//  NSGAII.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
package hyperheuristics.algorithm;

import hyperheuristics.comparators.LowLevelHeuristicComparatorFactory;
import hyperheuristics.lowlevelheuristic.LowLevelHeuristic;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.base.Algorithm;
import jmetal.base.Operator;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.comparator.CrowdingComparator;
import jmetal.problems.CITO_CAITO;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Ranking;

/**
 * Implementation of NSGA-II. This implementation of NSGA-II makes use of a QualityIndicator object to obtained the convergence speed of the algorithm. This version is used in the paper: A.J. Nebro, J.J. Durillo, C.A. Coello Coello, F. Luna, E. Alba "A Study of Convergence Speed in Multi-Objective Metaheuristics." To be presented in: PPSN'08. Dortmund. September 2008.
 */
public class NSGAIIHyperheuristic extends Algorithm {

    private final List<LowLevelHeuristic> lowLevelHeuristics;
    private final CITO_CAITO problem_;
    private FileWriter lowLevelHeuristicsRankWriter;
    private FileWriter lowLevelHeuristicsTimeWriter;
    private FileWriter qDebugWriter;
    private FileWriter auxDebugWriter;
    private String generationsOutputDirectory;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public NSGAIIHyperheuristic(CITO_CAITO problem) {
        this.lowLevelHeuristics = new ArrayList<>();
        this.problem_ = problem;
    } // NSGAII

    public LowLevelHeuristic addLowLevelHeuristic(HashMap<String, Object> parameters) {
        LowLevelHeuristic lowLevelHeuristic = new LowLevelHeuristic(parameters);
        if (!lowLevelHeuristics.contains(lowLevelHeuristic)) {
            lowLevelHeuristics.add(lowLevelHeuristic);
            return lowLevelHeuristic;
        } else {
            return null;
        }
    }

    public LowLevelHeuristic removeLowLevelHeuristic(String name) {
        for (int i = 0; i < lowLevelHeuristics.size(); i++) {
            LowLevelHeuristic lowLevelHeuristic = lowLevelHeuristics.get(i);
            if (lowLevelHeuristic.getName().equals(name)) {
                return lowLevelHeuristics.remove(i);
            }
        }
        return null;
    }

    public void clearLowLeverHeuristics() {
        lowLevelHeuristics.clear();
    }

    public void clearLowLeverHeuristicsValues() {
        LowLevelHeuristic.clearAllStaticValues();
        for (LowLevelHeuristic lowLevelHeuristic : lowLevelHeuristics) {
            lowLevelHeuristic.clearAllValues();
        }
    }

    public int[] getLowLevelHeuristicsNumberOfTimesApplied() {
        int[] allTimesApplied = new int[lowLevelHeuristics.size()];
        for (int i = 0; i < lowLevelHeuristics.size(); i++) {
            LowLevelHeuristic lowLevelHeuristic = lowLevelHeuristics.get(i);
            allTimesApplied[i] = lowLevelHeuristic.getNumberOfTimesApplied();
        }
        return allTimesApplied;
    }

    public int getLowLevelHeuristicsSize() {
        return lowLevelHeuristics.size();
    }

    public void setLowLevelHeuristicsRankPath(String path) throws IOException {
        if (lowLevelHeuristicsRankWriter != null) {
            lowLevelHeuristicsRankWriter.close();
        }
        lowLevelHeuristicsRankWriter = new FileWriter(path);
    }

    public void setLowLevelHeuristicsTimePath(String path) throws IOException {
        if (lowLevelHeuristicsTimeWriter != null) {
            lowLevelHeuristicsTimeWriter.close();
        }
        lowLevelHeuristicsTimeWriter = new FileWriter(path);
    }

    public void setDebugPath(String path) throws IOException {
        if (qDebugWriter != null) {
            qDebugWriter.close();
        }
        qDebugWriter = new FileWriter(path+"_q.txt");
         if (auxDebugWriter != null) {
            auxDebugWriter.close();
        }
        auxDebugWriter = new FileWriter(path+"_aux.txt");
    }

    public void setGenerationsOutputDirectory(String path) throws IOException {
        generationsOutputDirectory = path;
    }

    public void printLowLevelHeuristicsInformation(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (LowLevelHeuristic lowLevelHeuristic : lowLevelHeuristics) {
                writer.append("Name: " + lowLevelHeuristic.getName() + ":\n");
                writer.append("\tRank: " + lowLevelHeuristic.getRank() + "\n");
                writer.append("\tElapsed Time: " + lowLevelHeuristic.getElapsedTime() + "\n");
                writer.append("\tChoice Value: " + lowLevelHeuristic.getChoiceFunctionValue() + "\n");
                writer.append("\tNumber of Times Applied: " + lowLevelHeuristic.getNumberOfTimesApplied() + "\n");
                writer.append("\n");
            }
            writer.append("----------------------\n\n");
        } catch (IOException ex) {
            Logger.getLogger(NSGAIIHyperheuristic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Runs the NSGA-II algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions as a result of the algorithm execution
     * @throws JMException
     * @throws java.lang.ClassNotFoundException
     */
    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize;
        int maxEvaluations;
        int evaluations;
        Comparator<LowLevelHeuristic> heuristicFunctionComparator;

        QualityIndicator indicators; // QualityIndicator object
        int requiredEvaluations; // Use in the example of use of the
        // indicators object (see below)

        SolutionSet population;
        SolutionSet offspringPopulation;
        SolutionSet union;

        Distance distance = new Distance();

        //Read the parameters
        populationSize = ((Integer) getInputParameter("populationSize"));
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations"));
        String heuristicFunction = (String) getInputParameter("heuristicFunction");
        heuristicFunctionComparator = LowLevelHeuristicComparatorFactory.createComparator(heuristicFunction);
        indicators = (QualityIndicator) getInputParameter("indicators");

        //Get the selection operator
        Operator selectionOperator = operators_.get("selection");

        //Initialize the variables
        population = new SolutionSet(populationSize);
        evaluations = 0;

        requiredEvaluations = 0;

        // Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations++;
            population.add(newSolution);
        } //for  

        int generation = 0;
        // Generations 
        while (evaluations < maxEvaluations) {
            generation++;
            // Create the offSpring solutionSet      
            offspringPopulation = new SolutionSet(populationSize);
            Solution[] parents = new Solution[2];
            for (int i = 0; i < (populationSize / 2);) {
                //Get the best hyperheuristics
                List<LowLevelHeuristic> applyingHeuristics = getApplyingHeuristics(heuristicFunctionComparator);

                //Execute best hyperheuristics
                for (int j = 0; j < applyingHeuristics.size() && evaluations < maxEvaluations && i < (populationSize / 2); j++) {
                    LowLevelHeuristic heuristic = applyingHeuristics.get(j);

                    //obtain parents
                    parents[0] = (Solution) selectionOperator.execute(population, problem_);
                    parents[1] = (Solution) selectionOperator.execute(population, problem_);

                    Solution[] offSpring = (Solution[]) heuristic.execute(parents, problem_);

                    problem_.evaluate(offSpring[0]);
                    problem_.evaluateConstraints(offSpring[0]);
                    problem_.evaluate(offSpring[1]);
                    problem_.evaluateConstraints(offSpring[1]);

                    //Update rank
                    heuristic.updateRank(parents, offSpring);
                    if (LowLevelHeuristic.MULTI_ARMED_BANDIT.equals(heuristicFunction)) {
                        heuristic.creditAssignment(lowLevelHeuristics);
                    }

                    offspringPopulation.add(offSpring[0]);
                    offspringPopulation.add(offSpring[1]);

                    i++;
                    evaluations += 2;
                    
                    if (qDebugWriter != null) {
                        try {
                            for (LowLevelHeuristic lowLevelHeuristic : lowLevelHeuristics) {
                                qDebugWriter.append(lowLevelHeuristic.getQ() + "\t");
                            }
                            qDebugWriter.append("\n");
                        } catch (IOException ex) {
                            Logger.getLogger(NSGAIIHyperheuristic.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }  

                    if (auxDebugWriter != null) {
                        try {
                            for (LowLevelHeuristic lowLevelHeuristic : lowLevelHeuristics) {
                                auxDebugWriter.append(lowLevelHeuristic.getAux() + "\t");
                            }
                            auxDebugWriter.append("\n");
                        } catch (IOException ex) {
                            Logger.getLogger(NSGAIIHyperheuristic.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                }

                //Update time elapsed from heuristics not executed
                for (LowLevelHeuristic lowLevelHeuristic : lowLevelHeuristics) {
                    if (!applyingHeuristics.contains(lowLevelHeuristic)) {
                        lowLevelHeuristic.notExecuted();
                    }
                }

                if (lowLevelHeuristicsRankWriter != null) {
                    try {
                        for (LowLevelHeuristic lowLevelHeuristic : lowLevelHeuristics) {
                            lowLevelHeuristicsRankWriter.append(lowLevelHeuristic.getRank() + "\t");
                        }
                        lowLevelHeuristicsRankWriter.append("\n");
                    } catch (IOException ex) {
                        Logger.getLogger(NSGAIIHyperheuristic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (lowLevelHeuristicsTimeWriter != null) {
                    try {
                        for (LowLevelHeuristic lowLevelHeuristic : lowLevelHeuristics) {
                            lowLevelHeuristicsTimeWriter.append(lowLevelHeuristic.getElapsedTime() + "\t");
                        }
                        lowLevelHeuristicsTimeWriter.append("\n");
                    } catch (IOException ex) {
                        Logger.getLogger(NSGAIIHyperheuristic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } // for

            // Create the solutionSet union of solutionSet and offSpring
            union = ((SolutionSet) population).union(offspringPopulation);

            // Ranking the union
            Ranking ranking = new Ranking(union);

            int remain = populationSize;
            int index = 0;
            SolutionSet front;
            population.clear();

            // Obtain the next front
            front = ranking.getSubfront(index);

            while ((remain > 0) && (remain >= front.size())) {
                //Assign crowding distance to individuals
                distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                //Add the individuals of this front
                for (int k = 0; k < front.size(); k++) {
                    population.add(front.get(k));
                } // for

                //Decrement remain
                remain = remain - front.size();

                //Obtain the next front
                index++;
                if (remain > 0) {
                    front = ranking.getSubfront(index);
                } // if        
            } // while

            // Remain is less than front(index).size, insert only the best one
            if (remain > 0) {  // front contains individuals to insert                        
                distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                front.sort(new CrowdingComparator());
                for (int k = 0; k < remain; k++) {
                    population.add(front.get(k));
                } // for

            } // if                               

            if (generationsOutputDirectory != null) {
                population.printObjectivesToFile(generationsOutputDirectory + "/GEN_" + generation + ".txt");
            }

            // This piece of code shows how to use the indicator object into the code
            // of NSGA-II. In particular, it finds the number of evaluations required
            // by the algorithm to obtain a Pareto front with a hypervolume higher
            // than the hypervolume of the true Pareto front.
            if ((indicators != null)
                    && (requiredEvaluations == 0)) {
                double HV = indicators.getHypervolume(population);
                if (HV >= (0.98 * indicators.getTrueParetoFrontHypervolume())) {
                    requiredEvaluations = evaluations;
                } // if
            } // if
        } // while

        // Return as output parameter the required evaluations
        setOutputParameter("evaluations", requiredEvaluations);

        // Return the first non-dominated front
        Ranking ranking = new Ranking(population);

        return ranking.getSubfront(0);
    } // execute

    private List<LowLevelHeuristic> getApplyingHeuristics(Comparator<LowLevelHeuristic> comparator) {
        List<LowLevelHeuristic> allLowLevelHeuristics = new ArrayList<>(lowLevelHeuristics);
        Collections.sort(allLowLevelHeuristics, comparator);
        List<LowLevelHeuristic> applyingHeuristics = new ArrayList<>();

        //Find the best tied heuristics
        Iterator<LowLevelHeuristic> iterator = allLowLevelHeuristics.iterator();
        LowLevelHeuristic heuristic;
        LowLevelHeuristic nextHeuristic = iterator.next();
        do {
            heuristic = nextHeuristic;
            applyingHeuristics.add(heuristic);
        } while (iterator.hasNext() && comparator.compare(heuristic, nextHeuristic = iterator.next()) == 0);

        return applyingHeuristics;
    }
} // NSGA-II
