package ampi;


import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RolloutUtil {

    public static List<Object> getRolloutSet(Game game, int n, List<Double> beta, String featureSet) {
        int k = 2;
        List<Object> rslt = game.getRandomStates(k*n, beta, featureSet);
        Collections.shuffle(rslt);
        return rslt.subList(0, n);
    }

    public static List<Object> getRolloutSetTetris(GameTetris game, int n, List<Double> beta, String featureSet, Util.ActionType actionType) {
        int k = 2;
        List<Object> rslt = game.getRandomStates(k*n, beta, featureSet, 5, actionType);
        Collections.shuffle(rslt);
        return rslt.subList(0, n);
    }

    //Overloaded method, when feature sets and weights are the same in the whole rollout.
    public static double doRollout(Pair<Object, String> stateAction, int n, Game game, List<Double> beta, double gamma, String featureSet, Util.ActionType rolloutActionType) {
        return doRollout(stateAction, n, game, beta, beta, gamma, featureSet, featureSet, rolloutActionType);
    }

    //Used when the rollout chooses actions according to classification betas and the last value comes from regression betas.
    public static double doRollout(Pair<Object, String> stateAction, int n, Game game,  List<Double> betaReg, List<Double> betaCl, double gamma,  String featureSetReg, String featureSetCl, Util.ActionType rolloutActionType) {
        assert n > 0;
        Object s = stateAction.getFirst();
        String a = stateAction.getSecond();
        //String a = getBestAction(s);

        Pair<Object, Double> pair = game.getNewStateAndReward(s, a);
        Object newState = pair.getFirst();
        double reward = pair.getSecond();

        String bestAction = getBestAction(newState, betaCl, game, featureSetCl, rolloutActionType);

        if(bestAction == "")
            return reward + gamma * getValue(stateAction, betaReg, game, featureSetReg);

        Pair<Object, String> newStateAction = new Pair<>(newState, bestAction);

        if (n == 1)
            return reward + gamma * getValue(newStateAction, betaReg, game, featureSetReg);

        return reward + gamma * doRollout(newStateAction, n-1, game, betaReg, betaCl, gamma, featureSetReg, featureSetCl, rolloutActionType) ;
    }

    //Used when the rollout chooses actions according to classification betas and the last value comes from regression betas.
    public static double doRolloutTetris(Pair<Object, String> stateAction, int n, GameTetris game,  List<Double> betaReg, List<Double> betaCl, double gamma,  String featureSetReg, String featureSetCl, Util.ActionType rolloutActionType) {
        assert n >= 0;
        Object s = stateAction.getFirst();
        String a = stateAction.getSecond();
        //String a = getBestAction(s);

        Pair<Object, Double> pair = game.getNewStateAndReward(s, a);
        Object newState = pair.getFirst();
        double reward = pair.getSecond();

        Pair<String, Double> bestActionValuePair = getBestActionTetris(newState, betaCl, game, featureSetCl, rolloutActionType);

        if(bestActionValuePair.getFirst() == "")//All possible actions lead to gameover.
            return reward;

        Pair<Object, String> newStateAction = new Pair<>(newState, bestActionValuePair.getFirst());

        if (n == 0)//Rollout ends.
            return reward + gamma * getValue(newStateAction, betaReg, game, featureSetReg);;

        //Rollout continues:
        return reward + gamma * doRolloutTetris(newStateAction, n-1, game, betaReg, betaCl, gamma, featureSetReg, featureSetCl, rolloutActionType) ;
    }

    //returns: argmax_a(Q(s, a))
    public static String getBestAction(Object state, List<Double> beta, Game game,  String featureSet, Util.ActionType actionType) {
        String bestAction = "";
        double bestValue = Double.NEGATIVE_INFINITY;
        for (String action : game.getActions(state, actionType)) {
            double value = getValue(new Pair<Object, String>(state, action), beta, game, featureSet);//qStateAction(state, action);
            if (value >= bestValue) {
                bestValue = value;
                bestAction = action;
            }
        }
        //debuging purposes:
        if(bestAction.equals("")){
            List<String> actions = game.getActions(state, actionType);
            for (String action : game.getActions(state, actionType)) {
                double value = getValue(new Pair<Object, String>(state, action), beta, game, featureSet);//qStateAction(state, action);
                if (value >= bestValue) {
                    bestValue = value;
                    bestAction = action;
                }
            }
        }
        assert !bestAction.equals("");
        return bestAction;
    }

    public static Pair<String,Double> getBestActionTetris(Object state, List<Double> beta, GameTetris game,  String featureSet, Util.ActionType actionType) {
        String bestAction = "";
        double bestValue = Double.NEGATIVE_INFINITY;
        for (Pair<String,List<Double>> action : game.getStateActionFeatureValues(featureSet, state, actionType)) {
            double value = Util.dotproduct(beta, action.getSecond());//qStateAction(state, action);
            if (value >= bestValue) {
                bestValue = value;
                bestAction = action.getFirst();
            }
        }

//        assert !bestAction.equals("");
        if(bestAction.equals(""))
            return new Pair(bestAction, 0.);

        return new Pair(bestAction, bestValue);
    }

    // returns: b0 + b1*f1 + b2*f2 + ... + bn*fn
    // where f1,..,fn are features of state and b0..bn is beta vector.
    public static double getValue(Pair<Object, String> stateAction, List<Double> beta, Game game, String featureSet ) {
        List<Double> xs = new ArrayList<>();
        Object state = stateAction.getFirst();
        String action = stateAction.getSecond();
        xs.addAll(game.getFeatureValues(featureSet, state, action));
        return ampi.Util.dotproduct(xs, beta);
    }

    // returns: b0 + b1*f1 + b2*f2 + ... + bn*fn
    // where f1,..,fn are features of state and b0..bn is beta vector.
    public static double getValue(Pair<Object, String> stateAction, List<Double> beta, GameTetris game, String featureSet ) {
        List<Double> xs = new ArrayList<>();
        Object state = stateAction.getFirst();
        String action = stateAction.getSecond();
        xs.addAll(game.getFeatureValues(featureSet, state, action));
        return ampi.Util.dotproduct(xs, beta);
    }
}
