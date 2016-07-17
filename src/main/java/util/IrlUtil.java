package util;


import domains.*;
import domains.tetris.TetrisFeatureSet;
import domains.tetris.TetrisFeatures;
import domains.tictactoe.helpers.TicTacToeFeatureSet;
import models.Policy;
import org.apache.commons.math3.util.Pair;
import policy.PickAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class IrlUtil {

    public static List<List<Pair<State, Features>>> getTrajectories(int num, State initialState, Policy policy, Task task, Random random){
        List<List<Pair<State, Features>>> trajectories = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            List<Pair<State, Features>> trajectory = getTrajectory(initialState, policy, task, random);
            trajectories.add(trajectory);
        }
        return trajectories;
    }

    public static List<List<Pair<State, Features>>> getTrajectories(int num, List<State> initialStates, Policy policy, Task task, Random random){
        List<List<Pair<State, Features>>> trajectories = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            List<Pair<State, Features>> trajectory = getTrajectory(initialStates.get(i), policy, task, random);
            trajectories.add(trajectory);
        }
        return trajectories;
    }

    static private List<Pair<State, Features>> getTrajectory(State initialState, Policy policy, Task task, Random random) {
        List<Pair<State, Features>> trajectory = new ArrayList<>();
        State state = initialState;
        trajectory.add(new Pair(state, state.features()));
        task.init();
        while(!task.taskEnds(state)){
            Action action = policy.pickAction(state);
            List<State> sprimes = new ArrayList<State>();
            List<Double> probs = new ArrayList();
            state.getEffect(action, sprimes, probs);
            State lastState = state;
            int i = Pick.indexwrtProbs(probs, random);
            state = sprimes.get(i);
            trajectory.add(new Pair(state, state.features()));
            task.getReward(lastState, action, state);
        }
        return trajectory;
    }

    public static List<Double> calculateFeatureExpectations(List<List<Pair<State, Features>>> trajectories, double gamma, FeatureSet featureSet) {
        List<Double> featureExpectations = new ArrayList<>();
        for (List<Pair<State, Features>> trajectory : trajectories) {
            double discount = 1;
            for (int i = 1; i < trajectory.size(); i++) {
                Pair<State, Features> stateFeaturesPair = trajectory.get(i);
                discount = discount*gamma;
                List<Double> featureValues = featureSet.make(stateFeaturesPair.getSecond());
                for (int j = 0; j < featureValues.size(); j++) {
                    if(featureExpectations.size() <= j){
                        featureExpectations.add(discount * featureValues.get(j));
                    }else {
                        featureExpectations.set(j, featureExpectations.get(j) + discount * featureValues.get(j));
                    }
                }
            }
        }
        featureExpectations = featureExpectations.stream().map(p -> p/trajectories.size()).collect(Collectors.toList());
        return featureExpectations;
    }

    public static List<Double> calculateFeatureExpectationsRecursively(List<State> initStates, PickAction pick, double lambda, FeatureSet featureSet, int length) {
        List<Double> featureExpectations = new ArrayList<>();
        for (State initState : initStates) {
           calculateFeatureExpectationsOneState(initState, pick, lambda, featureSet, length -1, featureExpectations);
        }
        featureExpectations = featureExpectations.stream().map(p -> p/initStates.size()).collect(Collectors.toList());
        return featureExpectations;
    }

    public static void calculateFeatureExpectationsOneState(State state, PickAction pick, double lambda, FeatureSet featureSet, int length, List<Double> featureExpectations) {
        List<Pair<Action, Features>> actionFeaturesList = state.getActionFeaturesList();
        actionFeaturesList = actionFeaturesList.stream().filter(p -> !((TetrisFeatures)p.getSecond()).gameOver).collect(Collectors.toList());
        int[] possibleActions = pick.pick(state, actionFeaturesList);
        List<State> nextStates = new ArrayList<>();
        List<Double> probs = new ArrayList<>();
        double probPickingAction = 1;
        if(possibleActions.length > 0)
             probPickingAction = 1/possibleActions.length;
        for (int i = 0; i < possibleActions.length; i++) {
            state.getEffect(actionFeaturesList.get(possibleActions[i]).getFirst(), nextStates, probs);
            for (int j = 0; j < nextStates.size(); j++) {
                List<Double> featureValues = featureSet.make(nextStates.get(j).features());
                for (int k = 0; k < featureValues.size(); k++) {
                    if(featureExpectations.size() <= k){
                        featureExpectations.add(probPickingAction * probs.get(j) * lambda * featureValues.get(k));
                    }else {
                        featureExpectations.set(k, featureExpectations.get(k) + probPickingAction * probs.get(j) * lambda * featureValues.get(k));
                    }
                }
                if(length > 0)
                    calculateFeatureExpectationsOneState(nextStates.get(j) , pick, probPickingAction * lambda * probs.get(j), featureSet, length - 1, featureExpectations);
            }

        }
    }

}
