/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hyperheuristics.comparators;

import hyperheuristics.lowlevelheuristic.LowLevelHeuristic;
import java.util.Comparator;

/**
 *
 * @author giovaniguizzo
 */
public class LowLevelHeuristicComparatorFactory {

    public static final String NAME = "name";
    public static final String CHOICE_FUNCTION = "ChoiceFunction";
    public static final String MULTI_ARMED_BANDIT = "MultiArmedBandit";

    public static Comparator<LowLevelHeuristic> createComparator(String name) {
        switch (name) {
            case CHOICE_FUNCTION:
                return new LowLevelHeuristicChoiceFunctionComparator();
            case NAME:
                return new LowLevelHeuristicNameComparator();
            case MULTI_ARMED_BANDIT:
                return new LowLevelHeuristicMultiArmedBanditComparator();
            default:
                return null;
        }
    }

}
