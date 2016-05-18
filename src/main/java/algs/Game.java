package algs;


import domains.tetris.TetrisAction;
import domains.tetris.TetrisFeatureSet;
import domains.tetris.TetrisFeatures;
import domains.tetris.TetrisState;
import org.apache.commons.math3.util.Pair;
import util.Compute;
import util.UtilAmpi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Game {
    Random random;

    final String paretoFeatureSet = "bcts";
    final double[] paretoWeights = new double[]{-13.08, -19.77, -9.22, -10.49, 6.60, -12.63, -24.04, -1.61};

    public Game(long seed){
        random = new Random(seed);
    }

    public List<Double> getFeatureValues(String featureSet, Object state) {
        TetrisState tState = (TetrisState) state;
        return TetrisFeatureSet.make(tState.features, featureSet);
    }

    public List<Double> getFeatureValues(String featureSet, Object state, String action) {
        TetrisState tState = (TetrisState) state;
        List<Pair<TetrisAction,TetrisFeatures>> actionFeatures = tState.getActionsFeaturesList();

        for (Pair<TetrisAction,TetrisFeatures> actionFeature : actionFeatures)
            if( actionFeature.getFirst().name().equals(action))
                return TetrisFeatureSet.make(actionFeature.getSecond(), featureSet);

        return new ArrayList<>();
    }

    public List<Double> getFeatureValuesIncludeGameOver(String featureSet, Object state, String action) {
        TetrisState tState = (TetrisState) state;
        List<Pair<TetrisAction,TetrisFeatures>> actionFeatures = tState.getActionsFeaturesList();
        for (Pair<TetrisAction,TetrisFeatures> actionFeature : actionFeatures)
            if( actionFeature.getFirst().name().equals(action))
                return TetrisFeatureSet.make(actionFeature.getSecond(), featureSet);

        return new ArrayList<>();
    }

    public List<Pair<String, List<Double>>> getStateActionFeatureValues(String featureSet, Object state) {
        return getStateActionFeatureValues(featureSet, state, UtilAmpi.ActionType.ANY);
    }

    public List<Pair<String, List<Double>>> getStateActionFeatureValues(String featureSet, Object state, UtilAmpi.ActionType actionType) {
        TetrisState tState = (TetrisState) state;
        List<Pair<TetrisAction,TetrisFeatures>> actionFeatures = tState.getActionsFeaturesList();
        List<Pair<String, List<Double>>> featureSetValues = actionFeatures
                .stream()
                .filter(p -> !p.getSecond().gameOver)
                .map(p -> new Pair<String,List<Double>>(p.getFirst().name(), TetrisFeatureSet.make(p.getSecond(), featureSet)))
                .collect(Collectors.toList());
        List<Pair<String, List<Double>>> featureSetParetoValues = actionFeatures
                .stream()
                .filter(p -> !p.getSecond().gameOver)
                .map(p -> new Pair<String,List<Double>>(p.getFirst().name(), TetrisFeatureSet.make(p.getSecond(), paretoFeatureSet)))
                .collect(Collectors.toList());
        boolean[] is_pareto = UtilAmpi.paretoList(featureSetParetoValues, actionType, paretoWeights);
        List<Pair<String, List<Double>>> actionFeaturesReturn = new ArrayList<>();
        for (int i = 0; i < featureSetValues.size(); i++) {
            if(is_pareto[i])
                actionFeaturesReturn.add(featureSetValues.get(i));
        }
        return featureSetValues;
    }

    public List<Pair<String, List<Double>>> getStateActionFeatureValuesIncludingGameover(String featureSet, Object state, UtilAmpi.ActionType actionType) {
        TetrisState tState = (TetrisState) state;
        List<Pair<TetrisAction,TetrisFeatures>> actionFeatures = tState.getActionsFeaturesList();
        List<Pair<String, List<Double>>> featureSetValues = actionFeatures
                .stream()
                .map(p -> new Pair<String,List<Double>>(p.getFirst().name(), TetrisFeatureSet.make(p.getSecond(), featureSet)))
                .collect(Collectors.toList());
        List<Pair<String, List<Double>>> featureSetParetoValues = actionFeatures
                .stream()
                .map(p -> new Pair<String,List<Double>>(p.getFirst().name(), TetrisFeatureSet.make(p.getSecond(), paretoFeatureSet)))
                .collect(Collectors.toList());
        boolean[] is_pareto = UtilAmpi.paretoList(featureSetParetoValues, actionType, paretoWeights);
        List<Pair<String, List<Double>>> actionFeaturesReturn = new ArrayList<>();
        for (int i = 0; i < actionFeatures.size(); i++) {
            if(is_pareto[i])
                actionFeaturesReturn.add(featureSetValues.get(i));
        }
        return featureSetValues;
    }

    public List<String> getFeatureNames(String featureSetName) {
        return TetrisFeatureSet.featureNames(featureSetName);
    }

    public List<Object> getRandomStates(int n, List<Double> beta, String featureSet, int modulusFactor , UtilAmpi.ActionType actionType) {
        TetrisState state = new TetrisState(random);
        List states = new ArrayList<>();
        for (int i = 0; i < n * modulusFactor; i++) {

//            List<Pair<TetrisAction, TetrisFeatures>> actions = state.getActionsFeaturesList(); //We include actions that end the game immediatly.
            List<Pair<TetrisAction, TetrisFeatures>> actionsWithGameover = state.getActionsFeaturesList();
            List<Pair<TetrisAction, TetrisFeatures>> actions = actionsWithGameover.stream()
                    .filter(p -> !p.getSecond().gameOver).collect(Collectors.toList());

            if(actions.isEmpty()) {
                state.nextState(0, 0);
                if(i % modulusFactor == 0) {
                    states.add(state.copy());
                    i++;
                }
                state = new TetrisState(random);
                if(i % modulusFactor == 0) {
                    states.add(state.copy());
                    i++;
                }
                continue;
            }

            List<Pair<String, List<Double>>> featureSetParetoValues = actions.stream().map(p -> new Pair<String,List<Double>>(p.getFirst().name(), TetrisFeatureSet.make(p.getSecond(), paretoFeatureSet))).collect(Collectors.toList());

            boolean[] is_pareto = UtilAmpi.paretoList(featureSetParetoValues, actionType, paretoWeights);
            List<Pair<TetrisAction, TetrisFeatures>> actionsPareto = new ArrayList<>();

            for (int j = 0; j < actions.size(); j++) {
                if(is_pareto[j])
                    actionsPareto.add(actions.get(j));
            }

            double[] values = new double[actionsPareto.size()];
            for (int j = 0; j < actionsPareto.size(); j++) {
                TetrisFeatures features = actionsPareto.get(j).getSecond();
                List<Double> featureValues = TetrisFeatureSet.make(features, featureSet);
                values[j] = UtilAmpi.dotproduct(beta, featureValues);
            }
            int[] maxIndices = Compute.indicesOfMax(values);
            int chosenAction = random.nextInt(maxIndices.length);
            TetrisAction action = actionsPareto.get(maxIndices[chosenAction]).getFirst();
            state.nextState(action.col, action.rot);

            if(state.features.gameOver)
                state = new TetrisState(random);

            if(i % modulusFactor == 0)
                states.add(state.copy());

        }
        return states;
    }

    public List<Object> getSampleTrajectory(int n, List<Double> beta, String featureSet, UtilAmpi.ActionType actionType) {
        TetrisState state = new TetrisState(random);
        List states = new ArrayList<>();
        states.add(state.copy());

        for (int i = 0; i < n; i++) {
//            List<Pair<TetrisAction, TetrisFeatures>> actions = state.getActionsFeaturesList(); //We include actions that end the game immediatly.
            List<Pair<TetrisAction, TetrisFeatures>> actionsWithGameover = state.getActionsFeaturesList();
            List<Pair<TetrisAction, TetrisFeatures>> actions = actionsWithGameover.stream()
                    .filter(p -> !p.getSecond().gameOver).collect(Collectors.toList());
            if(actions.isEmpty()) {
                state.nextState(0, 0);
                states.add(state.copy());
                state = new TetrisState(random);
                states.add(state.copy());
                continue;
            }

            List<Pair<String, List<Double>>> featureSetParetoValues = actions.stream().map(p -> new Pair<String,List<Double>>(p.getFirst().name(), TetrisFeatureSet.make(p.getSecond(), paretoFeatureSet))).collect(Collectors.toList());

            boolean[] is_pareto = UtilAmpi.paretoList(featureSetParetoValues, actionType, paretoWeights);
            List<Pair<TetrisAction, TetrisFeatures>> actionsPareto = new ArrayList<>();

            for (int j = 0; j < actions.size(); j++) {
                if(is_pareto[j])
                    actionsPareto.add(actions.get(j));
            }

            double[] values = new double[actionsPareto.size()];
            for (int j = 0; j < actionsPareto.size(); j++) {
                TetrisFeatures features = actionsPareto.get(j).getSecond();
                List<Double> featureValues = TetrisFeatureSet.make(features, featureSet);
                values[j] = UtilAmpi.dotproduct(beta, featureValues);
            }

            int[] maxIndices = Compute.indicesOfMax(values);
            int chosenAction = random.nextInt(maxIndices.length);
            TetrisAction action = actionsPareto.get(maxIndices[chosenAction]).getFirst();
            state.nextState(action.col, action.rot);

            states.add(state.copy());

            if(state.features.gameOver) {
                state = new TetrisState(random);
                states.add(state.copy());
            }
        }
        return states;
    }

    public List<Object> getRandomStates(int n) {
        return null;//unused
    }

    public Pair<Object, Double> getNewStateAndReward(Object state, String action) {
        TetrisState ts = ((TetrisState)state).copy();
        String[] actionStr = action.split("_");
        int col = Integer.parseInt(actionStr[0]);
        int rot = Integer.parseInt(actionStr[1]);
        ts.nextState(col, rot, random);
        return new Pair(ts, (double) ts.features.nClearedLines);
    }

    public List<String> getActions(Object state, UtilAmpi.ActionType actionType) {
        List<Pair<String, List<Double>>> actionFeatures = getStateActionFeatureValues("bcts", state, actionType);
        List<String> actions = new ArrayList<>();
        for (Pair<String, List<Double>> actionFeaturesPair : actionFeatures)
            actions.add(actionFeaturesPair.getFirst());

        return actions;
    }

    public List<String> getActionsIncludingGameover(Object state, UtilAmpi.ActionType actionType) {
        List<Pair<String, List<Double>>> actionFeatures = getStateActionFeatureValuesIncludingGameover("bcts", state, actionType);
        List<String> actions = new ArrayList<>();
        for (Pair<String, List<Double>> actionFeaturesPair : actionFeatures)
            actions.add(actionFeaturesPair.getFirst());

        return actions;
    }

    public double getReward(Object state) {
        TetrisState tState = (TetrisState) state;
        return tState.features.nClearedLines;
    }

    public boolean isGameover(Object stateBefore) {
        TetrisState tState = (TetrisState) stateBefore;
        return tState.features.gameOver;
    }
}
