package util;


import domains.*;
import domains.tictactoe.helpers.TicTacToeFeatureSet;
import models.Policy;
import org.apache.commons.math3.util.Pair;

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

    public static List<Double> calculateFeatureExpectations(List<List<Pair<State, Features>>> trajectories, double lambda, FeatureSet featureSet) {
        List<Double> featureExpectations = new ArrayList<>();
        for (List<Pair<State, Features>> trajectory : trajectories) {
            double discount = 1;
            for (Pair<State, Features> stateFeaturesPair : trajectory) {
                discount = discount*lambda;
                List<Double> featureValues = featureSet.make(stateFeaturesPair.getSecond());
                for (int i = 0; i < featureValues.size(); i++) {
                    if(featureExpectations.size() <= i){
                        featureExpectations.add(discount * featureValues.get(i));
                    }else {
                        featureExpectations.set(i, featureExpectations.get(i) + discount * featureValues.get(i));
                    }
                }
            }
        }
        featureExpectations = featureExpectations.stream().map(p -> p/trajectories.size()).collect(Collectors.toList());
        return featureExpectations;
    }

}
