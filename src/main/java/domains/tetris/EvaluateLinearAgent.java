package domains.tetris;


import domains.FeatureSet;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.util.Pair;
import util.Compute;
import util.UtilAmpi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EvaluateLinearAgent {

    public static int gamesTetris(int numGames, Random random, FeatureSet featureSet, List<Double> weights, UtilAmpi.ActionType actionType, FeatureSet paretoFeatureSet, double[] paretoWeights, boolean display) {
        int totalScore = 0;
        int maxScore = 0;
        int minScore = Integer.MAX_VALUE;
        double[] scores = new double[numGames];
        for (int g = 0; g < numGames; g++) {
            TetrisState state = new TetrisState(random);
            int score = 0;
            while (!state.features.gameOver) {
                List<Pair<TetrisAction, TetrisFeatures>> actionsWithGameover = state.getActionsFeaturesList();
                List<Pair<TetrisAction, TetrisFeatures>> actions = actionsWithGameover.stream()
                                                                .filter(p -> !p.getSecond().gameOver).collect(Collectors.toList());


                List<Pair<String,List<Double>>> featureSetParetoValues = actions.stream().map(p -> new Pair<String,List<Double>>(p.getFirst().name(), paretoFeatureSet.make(p.getSecond()))).collect(Collectors.toList());
                if(actions.isEmpty())
                    break;

                boolean[] is_pareto = UtilAmpi.paretoList(featureSetParetoValues, actionType, paretoWeights);
                List<Pair<TetrisAction, TetrisFeatures>> actionPareto = new ArrayList<>();
                for (int i = 0; i < actions.size(); i++) {
                    if(is_pareto[i])
                        actionPareto.add(actions.get(i));
                }
                double[] values = new double[actionPareto.size()];
                for (int i = 0; i < actionPareto.size(); i++) {
                    TetrisFeatures features = actionPareto.get(i).getSecond();
                    values[i] = UtilAmpi.dotproduct(weights, featureSet.make(features));
                }
                int[] maxIndices = Compute.indicesOfMax(values);
                int chosenAction = random.nextInt(maxIndices.length);
                TetrisAction action = actionPareto.get(maxIndices[chosenAction]).getFirst();
                state.nextState(action.col, action.rot, random);
                score += state.features.nClearedLines;
            }
            totalScore = totalScore + score;
            if(score > maxScore) maxScore = score;
            if(score < minScore) minScore = score;
            scores[g] = score;
        }

        if(display) {
            System.out.println();
            System.out.println("mean: " + totalScore / numGames);
            System.out.println("min: " + minScore);
            System.out.println("max: " + maxScore);
            Median median = new Median();
            System.out.println("median: " + median.evaluate(scores));
        }
        return totalScore/numGames;
    }

}
