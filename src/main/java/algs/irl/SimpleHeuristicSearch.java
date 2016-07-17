package algs.irl;

import domains.Action;
import domains.Features;
import domains.tetris.*;
import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.CMASolution;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;
import models.CustomPolicy;
import models.Policy;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import policy.*;
import util.Compute;
import util.Pick;
import util.UtilAmpi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;


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
                heuristics.add(generateRandomStackedPick(random.nextInt(3)+1));

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

                heuristics = generateOffspring(bestHeuristics, n);
                bestHeuristic = bestHeuristics.get(0);
                bestHeuristic.print(featureSet.featureNames());
            }
        }
        return bestHeuristic;
    }

    private List<StackedPick> generateOffspring(List<StackedPick> bestHeuristics, int n) {
        List<StackedPick> offpsring = new ArrayList<>();
        for (StackedPick bestHeuristic : bestHeuristics)
            offpsring.add(bestHeuristic);

        int motherIndex = 0;
        for (int i = offpsring.size(); i < n; i++) {
            offpsring.add(mutateStackedPick(bestHeuristics.get(motherIndex)));
            motherIndex++;
            if(motherIndex >= bestHeuristics.size())
                motherIndex = 0;
        }

        return offpsring;
    }

    private StackedPick generateRandomStackedPick(int numLevels){
        List<PickAction> picks = new ArrayList<>();
        for (int i = 0; i < numLevels; i++)
           picks.add(getRandomPick());

        StackedPick stackedPick = new StackedPick(picks);
        return stackedPick;
    }

    /**
     * Returns a mutated decision rule.
     * @param stackedPick
     * @return
     */
    private StackedPick mutateStackedPick(StackedPick stackedPick){
        List<PickAction> picks = new ArrayList<>();
        List<PickAction> motherPicks = stackedPick.getPicks();
        int mutatedPickIdx = random.nextInt(motherPicks.size());
        for (int i = 0; i < motherPicks.size(); i++) {
            PickAction motherPick = motherPicks.get(i);
            if(i == mutatedPickIdx) {
                picks.add(getRandomPick());
            }else{
                picks.add(motherPick);
            }
        }
        StackedPick mutatedStackedPick = new StackedPick(picks);
        return mutatedStackedPick;
    }

    private PickAction getRandomPick(){
        if(random.nextGaussian() > 0){//add single cue
            double signProducer = random.nextGaussian();
            int sign = (int) (signProducer / Math.abs(signProducer));
            int featureIdx = Pick.indexRandom(featureSet.featureNames().size(), random);
            SingleCue singleCue = new SingleCue(sign, featureIdx, featureSet, 0, random, new TetrisTaskLines(1));
            return singleCue;
        }else{//add double cue tally
            double signProducer = random.nextGaussian();
            int sign1 = (int) (signProducer / Math.abs(signProducer));
            int featureIdx1 = Pick.indexRandom(featureSet.featureNames().size(), random);
            signProducer = random.nextGaussian();
            int sign2 = (int) (signProducer / Math.abs(signProducer));
            int featureIdx2 = Pick.indexRandom(featureSet.featureNames().size(), random);
            Tally tally = new Tally(new int[]{sign1, sign2}, new int[]{featureIdx1, featureIdx2}, featureSet, random);
            return tally;
        }
    }
}
