package util;


import algs.Game;
import domains.*;
import domains.tetris.TetrisAction;
import domains.tetris.TetrisFeatures;
import domains.tetris.TetrisState;
import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RolloutUtil {

    public static List<Object> getRolloutSetTetris(Game game, int n, List<Double> beta, FeatureSet featureSet, UtilAmpi.ActionType actionType, FeatureSet paretoFeatureSet, double[] paretoWeights, Random random) {
        int k = 2;
        List<Object> rslt = game.getRandomStates(k*n, beta, featureSet, 10, actionType, paretoFeatureSet, paretoWeights);
        Collections.shuffle(rslt, random);
        return rslt.subList(0, n);
    }

    public static List<Object> getRolloutSetTetrisGabillon(String dir, Random random) {
        try {
            File f = new File(dir);
            List<String> lines = Files.readAllLines(Paths.get(f.getAbsolutePath()));
            List<Object> states = lines.stream().map(p -> (Object)new TetrisState(ParseStateGabillon.parse(p), random)).collect(Collectors.toList());
            System.out.println("Gabillon Sample:" +states.size());
//            for (Object state : states)
//                ((TetrisState)state).print();

            return states;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static List<Object> getRolloutSetTetrisGabillon(String dir, Random random, int size) {
        try {
            File f = new File(dir);
            List<String> lines = Files.readAllLines(Paths.get(f.getAbsolutePath()));
            List<Object> states = lines.stream().map(p -> (Object)new TetrisState(ParseStateGabillon.parse(p), random)).collect(Collectors.toList());
            Collections.shuffle(states, random);
            System.out.println("Gabillon Sample:" +states.size() +" sampling:" + size);
//            for (Object state : states)
//                ((TetrisState)state).print();

            return states.subList(0, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    //Used when the rollout chooses actions according to classification betas and the last value comes from regression betas.
    public static double doRolloutTetris(Pair<Object, String> stateAction, int n, Game game, List<Double> betaReg, List<Double> betaCl, double gamma, FeatureSet featureSetReg, FeatureSet featureSetCl, UtilAmpi.ActionType rolloutActionType, Random random, FeatureSet paretoFeatureSet, double[] paretoWeights) {
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

    //Used when the rollout chooses actions according to classification betas.
    public static double doRolloutTetrisIterative(Pair<Object, Action> stateAction, int n, List<Double> betaReg, List<Double> betaCl, double gamma, FeatureSet featureSetReg, FeatureSet featureSetCl, Random random, Task task, UtilAmpi.ActionType rolloutActionType, FeatureSet paretoFeatureSet, double[] paretoWeights) {
        TetrisState state = ((TetrisState)stateAction.getFirst()).copy();
        int rolloutIndex = 0;
        double reward = 0;
        state.nextState(stateAction.getSecond(), random);
        while(rolloutIndex < n && !state.features.gameOver){
            reward = reward + Math.pow(gamma, rolloutIndex)*task.getReward(null, null, state);
            Action action = getBestActionTetris(state, betaCl, featureSetCl, random, rolloutActionType, paretoFeatureSet, paretoWeights);
            state.nextState(action, random);
            rolloutIndex++;
        }
        double lastStateValue = state.features.gameOver? 0 : getValue(state.features, betaReg, featureSetReg);
        return reward + Math.pow(gamma, n) * lastStateValue;
    }

    public static Action getBestActionTetris(Object state, List<Double> beta, FeatureSet featureSet, Random random, UtilAmpi.ActionType actionType, FeatureSet paretoFeatureSet, double[] paretoWeights) {
        List<Pair<Action, Features>> actionFeaturesList = ((State) state).getActionFeaturesList();
        actionFeaturesList = actionFeaturesList.stream().filter(p -> !((TetrisFeatures)p.getSecond()).gameOver).collect(Collectors.toList());
        List<Pair<String, List<Double>>> featureSetParetoValues = actionFeaturesList.stream().map(p -> new Pair<String,List<Double>>(p.getFirst().name(), paretoFeatureSet.make(p.getSecond()))).collect(Collectors.toList());

        boolean[] is_pareto = UtilAmpi.paretoList(featureSetParetoValues, actionType, paretoWeights);
        List<Pair<Action, Features>> actionsPareto = new ArrayList<>();

        for (int j = 0; j < actionFeaturesList.size(); j++) {
            if(is_pareto[j])
                actionsPareto.add(actionFeaturesList.get(j));
        }

        double[] value = new double[actionsPareto.size()];

//        double[] value = new double[actionFeaturesList.size()];
        for (int i = 0; i < actionsPareto.size(); i++)
            value[i] = UtilAmpi.dotproduct(beta, featureSet.make(actionsPareto.get(i).getSecond()));//qStateAction(state, action);

        if(actionFeaturesList.isEmpty())
            return new TetrisAction(0, 0);

        int[] indicesOfMax = Compute.indicesOfMax(value);
        int actionIndex = indicesOfMax[random.nextInt(indicesOfMax.length)];
        return actionsPareto.get(actionIndex).getFirst();
    }

    public static Pair<String,Double> getBestActionTetris(Object state, List<Double> beta, Game game, FeatureSet featureSet, UtilAmpi.ActionType actionType, Random random, FeatureSet paretoFeatureSet, double[] paretoWeights) {
        String bestAction;
        double bestValue;
        List<Pair<Action, List<Double>>> stateActionFeatureValues = game.getStateActionFeatureValues(featureSet, state, actionType, paretoFeatureSet, paretoWeights);
        double[] value = new double[stateActionFeatureValues.size()];
        for (int i = 0; i < stateActionFeatureValues.size(); i++) {
            value[i] = UtilAmpi.dotproduct(beta, stateActionFeatureValues.get(i).getSecond());//qStateAction(state, action);
        }

        if(stateActionFeatureValues.isEmpty())
            return new Pair("", 0.);

        int[] indicesOfMax = Compute.indicesOfMax(value);
        int actionIndex = indicesOfMax[random.nextInt(indicesOfMax.length)];
        bestAction = stateActionFeatureValues.get(actionIndex).getFirst().name();
        bestValue = value[actionIndex];
        return new Pair(bestAction, bestValue);
    }


    // returns: b0 + b1*f1 + b2*f2 + ... + bn*fn
    // where f1,..,fn are features of state and b0..bn is beta vector.
    public static double getValue(Pair<Object, String> stateAction, List<Double> beta, Game game, FeatureSet featureSet ) {
        List<Double> xs = new ArrayList<>();
        Object state = stateAction.getFirst();
        String action = stateAction.getSecond();
//        xs.addAll(game.getFeatureValues(featureSet, state, action));
        return UtilAmpi.dotproduct(xs, beta);
    }


    // returns: b0 + b1*f1 + b2*f2 + ... + bn*fn
    // where f1,..,fn are features of state and b0..bn is beta vector.
    public static double getValue(TetrisFeatures features, List<Double> beta, FeatureSet featureSet ) {
        return UtilAmpi.dotproduct(featureSet.make(features), beta);
    }
}
