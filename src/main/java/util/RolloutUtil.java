package util;


import algs.Game;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RolloutUtil {

    public static List<Object> getRolloutSetTetris(Game game, int n, List<Double> beta, String featureSet, UtilAmpi.ActionType actionType, String paretoFeatureSet, double[] paretoWeights, Random random) {
        int k = 2;
        List<Object> rslt = game.getRandomStates(k*n, beta, featureSet, 5, actionType, paretoFeatureSet, paretoWeights);
        Collections.shuffle(rslt, random);
        return rslt.subList(0, n);
    }

    //Used when the rollout chooses actions according to classification betas and the last value comes from regression betas.
    public static double doRolloutTetris(Pair<Object, String> stateAction, int n, Game game, List<Double> betaReg, List<Double> betaCl, double gamma, String featureSetReg, String featureSetCl, UtilAmpi.ActionType rolloutActionType, Random random, String paretoFeatureSet, double[] paretoWeights) {
        assert n >= 0;
        Object s = stateAction.getFirst();
        String a = stateAction.getSecond();
        //String a = getBestAction(s);

        Pair<Object, Double> pair = game.getNewStateAndReward(s, a);
        Object newState = pair.getFirst();
        double reward = pair.getSecond();

        Pair<String, Double> bestActionValuePair = getBestActionTetris(newState, betaCl, game, featureSetCl, rolloutActionType, random, paretoFeatureSet, paretoWeights);

        if(bestActionValuePair.getFirst().equals("") || game.isGameover(newState))//All possible actions lead to gameover.
            return reward;

        Pair<Object, String> newStateAction = new Pair<>(newState, bestActionValuePair.getFirst());

        if (n == 0)//Rollout ends.
            return reward + gamma * getValue(newStateAction, betaReg, game, featureSetReg);;

        //Rollout continues:
        return reward + gamma * doRolloutTetris(newStateAction, n-1, game, betaReg, betaCl, gamma, featureSetReg, featureSetCl, rolloutActionType, random, paretoFeatureSet, paretoWeights) ;
    }

    public static Pair<String,Double> getBestActionTetris(Object state, List<Double> beta, Game game, String featureSet, UtilAmpi.ActionType actionType, Random random, String paretoFeatureSet, double[] paretoWeights) {
        String bestAction = "";
        double bestValue;
        List<Pair<String, List<Double>>> stateActionFeatureValues = game.getStateActionFeatureValues(featureSet, state, actionType, paretoFeatureSet, paretoWeights);
        double[] value = new double[stateActionFeatureValues.size()];
        for (int i = 0; i < stateActionFeatureValues.size(); i++) {
            value[i] = UtilAmpi.dotproduct(beta, stateActionFeatureValues.get(i).getSecond());//qStateAction(state, action);
        }

        if(stateActionFeatureValues.isEmpty())
            return new Pair("", 0.);

        int[] indicesOfMax = Compute.indicesOfMax(value);
        int actionIndex = indicesOfMax[random.nextInt(indicesOfMax.length)];
        bestAction = stateActionFeatureValues.get(actionIndex).getFirst();
        bestValue = value[actionIndex];
        return new Pair(bestAction, bestValue);
    }


    // returns: b0 + b1*f1 + b2*f2 + ... + bn*fn
    // where f1,..,fn are features of state and b0..bn is beta vector.
    public static double getValue(Pair<Object, String> stateAction, List<Double> beta, Game game, String featureSet ) {
        List<Double> xs = new ArrayList<>();
        Object state = stateAction.getFirst();
        String action = stateAction.getSecond();
        xs.addAll(game.getFeatureValues(featureSet, state, action));
        return UtilAmpi.dotproduct(xs, beta);
    }
}
