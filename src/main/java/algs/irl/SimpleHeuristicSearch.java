package algs.irl;

import domains.Action;
import domains.FeatureSet;
import domains.Features;
import domains.tetris.*;
import models.CustomPolicy;
import models.Policy;
import org.apache.commons.math3.util.Pair;
import policy.*;
import util.Compute;

import java.util.*;
import java.util.stream.Collectors;

import static util.StackedPickUtil.generateOffspring;
import static util.StackedPickUtil.generateRandomStackedPick;
import static util.StackedPickUtil.mutateStackedPick;


public class SimpleHeuristicSearch {

    TetrisFeatureSet featureSet = new TetrisFeatureSet("perceptible");
    Random random;
    PickAction filter;

    /**
     * @param random
     * @param featureSet
     */
    SimpleHeuristicSearch(Random random, TetrisFeatureSet featureSet){
        this.random = random;
        this.featureSet = featureSet;

    }

    public Policy fitPolicy(List<List<Pair<TetrisState, TetrisAction>>> trajectories){
        StackedPick stackedPick = searchSimpleHeuristic(trajectories);
        Policy policy = new CustomPolicy(stackedPick, new TetrisTaskLines(1.), random);
        return policy;
    }

    private StackedPick searchSimpleHeuristic(List<List<Pair<TetrisState, TetrisAction>>> trajectories){
        StackedPick bestHeuristic = null;
        int n = 100;
        int p = 10;
        int generations = 100;

        for (int iter = 0; iter < 1; iter++) {
            List<StackedPick> heuristics = new ArrayList<>();
            for (int i = 0; i < n; i++)
                heuristics.add(generateRandomStackedPick((random.nextInt(3)+1), featureSet, random));

            for (int gen = 0; gen < generations; gen++) {
                double[] values = new double[n];

                for (int i = 0; i < n; i++) {
                    double value = 0;
                    for (List<Pair<TetrisState, TetrisAction>> trajectory : trajectories) {
                        for (Pair<TetrisState, TetrisAction> stateActionPair : trajectory) {
                            List<Pair<TetrisAction, TetrisFeatures>> actionsFeaturesList = stateActionPair.getFirst().getActionsFeaturesList();
                            List<Pair<Action, Features>> actionsFeaturesListDepurated = actionsFeaturesList.stream().filter(a -> !a.getSecond().gameOver).map(a -> (Pair<Action,Features>)new Pair((Action) a.getFirst(), (Features) a.getSecond())).collect(Collectors.toList());
                            int[] actionsPicked = heuristics.get(i).pick(stateActionPair.getFirst(), actionsFeaturesListDepurated);
                            for (int j = 0; j < actionsPicked.length; j++) {
                                Pair<TetrisAction, TetrisFeatures> action = actionsFeaturesList.get(actionsPicked[j]);
                                if (action.getFirst().equals(stateActionPair.getSecond())) {
                                    value += (double)1 / (double)actionsPicked.length;
                                    break;
                                }
                            }
                        }
                    }
                    values[i] = value;
                }

                int[] orderedIndices = Compute.orderedIndices(values);
                System.out.println("Best value generation "+ values[orderedIndices[0]]);
                List<StackedPick> bestHeuristics = new ArrayList<>();
                for (int i = 0; i < p; i++)
                    bestHeuristics.add(heuristics.get(orderedIndices[i]));

                heuristics = generateOffspring(bestHeuristics, featureSet, n, random, 10);
                bestHeuristic = bestHeuristics.get(0);
                bestHeuristic.print(featureSet.featureNames());
            }
        }
        return bestHeuristic;
    }



}
