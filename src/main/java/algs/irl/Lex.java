package algs.irl;

import domains.Action;
import domains.FeatureSet;
import domains.Features;
import domains.tetris.TetrisAction;
import domains.tetris.TetrisFeatures;
import domains.tetris.TetrisState;
import domains.tetris.TetrisTaskLines;
import models.CustomPolicy;
import org.apache.commons.math3.util.Pair;
import policy.PickAction;
import policy.SingleCue;
import policy.StackedPick;
import util.Pick;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Lex {


    public CustomPolicy fitPolicy(List<List<Pair<TetrisState, TetrisAction>>> trainingSet, FeatureSet featureSet, Random random, int numCues){
        StackedPick lex = new StackedPick(new ArrayList<>());
        List<PickAction> cueStack = new ArrayList<>();
        System.out.println("Fitting Lex Rule:");
        for (int i = 0; i < numCues; i++) {
            PickAction singleCue = findBestPredictiveCue(trainingSet, featureSet, random, lex, i+1, numCues);
            cueStack.add(singleCue);
            lex = new StackedPick(cueStack);
        }
        return new CustomPolicy(lex, new TetrisTaskLines(1), random);
    }

    private PickAction findBestPredictiveCue(List<List<Pair<TetrisState, TetrisAction>>> trainingSet, FeatureSet featureSet, Random random, StackedPick stackedPick, int cueIndex, int numCues) {
        PickAction bestSingleCue = null;
        double bestScore = 0;
        List<PickAction> singleCues = new ArrayList<>();
        for (int i = 0; i < featureSet.featureNames().size()*2; i++) {
            if(i < featureSet.featureNames().size()) {
                PickAction singleCue = new SingleCue(-1, i, featureSet, 0, random, new TetrisTaskLines(1));
                singleCues.add(singleCue);
            }else{
                PickAction singleCue = new SingleCue(1, i - featureSet.featureNames().size(), featureSet, 0, random, new TetrisTaskLines(1));
                singleCues.add(singleCue);
            }
        }

        for (PickAction singleCue : singleCues) {
            CustomPolicy policy = new CustomPolicy(singleCue, new TetrisTaskLines(1), random);
            double score = 0;
            for (List<Pair<TetrisState, TetrisAction>> trajectory : trainingSet) {
                for (Pair<TetrisState, TetrisAction> pair : trajectory) {
                    TetrisState state = pair.getFirst();
                    List<Pair<TetrisAction, TetrisFeatures>> actionsFeaturesListTetris = state.getActionsFeaturesList();
                    List<Pair<Action, Features>> actionsFeaturesList = actionsFeaturesListTetris.stream().map(p -> new Pair<Action, Features>(p.getFirst(), p.getSecond())).collect(Collectors.toList());
                    int[] filteredActionIndices = stackedPick.pick(state, actionsFeaturesList);
                    List<Pair<Action, Features>> filteredActions = new ArrayList<>();
                    for (int filteredActionIndex : filteredActionIndices)
                        filteredActions.add(actionsFeaturesList.get(filteredActionIndex));

                    int[] cuePick = policy.getPick().pick(state, filteredActions);
                    int[] absoluteIndices = new int[cuePick.length];
                    for (int j = 0; j < cuePick.length; j++)
                        absoluteIndices[j] = filteredActionIndices[cuePick[j]];

                    score+=score(absoluteIndices.length, cueIndex, numCues, listContainsPick(absoluteIndices, actionsFeaturesList, pair.getSecond()));
                }
            }
            if(score > bestScore){
                bestSingleCue = singleCue;
                bestScore = score;
            }
        }

        System.out.println(featureSet.featureNames().get(((SingleCue)bestSingleCue).getFeatureIdx()) +":"+ ((SingleCue)bestSingleCue).getDirection());
        return bestSingleCue;
    }

    private double listContainsPick(int[] absoluteIndices, List<Pair<Action, Features>> actionsFeaturesList, TetrisAction action) {
        for (int absoluteIndex : absoluteIndices) {
            if(actionsFeaturesList.get(absoluteIndex).getFirst().equals(action))
                return 1;
        }
        return 0;
    }

    double score(int nAlternativesInSet, int cueIndex, int numCues, double alternativeInSet){
        if(numCues == 1){
            return alternativeInSet * (1/nAlternativesInSet);
        }
        return alternativeInSet * (Math.pow(1/nAlternativesInSet,(cueIndex - 1)));
    }


}
