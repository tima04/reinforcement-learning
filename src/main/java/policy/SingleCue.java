package policy;

import domains.*;
import domains.tetris.TetrisAction;
import domains.tetris.TetrisFeatureSet;
import domains.tetris.TetrisFeatures;
import domains.tetris.TetrisState;
import org.apache.commons.math3.util.Pair;
import util.Compute;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SingleCue implements PickAction{

    int sign;
    int featureIdx;
    FeatureSet featureSet;
    int ply;
    Task task;
    Random random;

    public SingleCue(double sign, int featureIdx, FeatureSet featureSet, int ply, Random random, Task task){
        this.sign = (int) (sign/Math.abs(sign));
        this.featureIdx = featureIdx;
        this.featureSet = featureSet;
        this.ply = ply;
        this.random = random;
        this.task = task;
    }
    /**
     * Single cue
     * @param actions
     * @return
     */
    public int pick(State state, List<Pair<Action, Features>> actions){
        return decisionNode(state, actions, ply).getFirst();
    }



    /**
     * Single cue

     * @param actions
     * @return
     */
    int singleCue(List<Pair<Action, Features>> actions){
        int[] indicesOfMax;
        if(actions.size() == 0)
            return 0;

        indicesOfMax = findIndicesOfBestValue(actions, sign, featureSet, featureIdx);
        return indicesOfMax[random.nextInt(indicesOfMax.length)];
    }





    /**
     * returns pair:
     * Integer = index of best action.
     * Double = value of feature.
     * @param state
     * @param actions
     * @param ply
     * @return
     */
    Pair<Integer, Double> decisionNode(State state, List<Pair<Action, Features>> actions, int ply){
        if(ply <= 0){
            if(actions.size() == 0)
                return new Pair(0, 0.);
            int actionIdx = singleCue(actions);
            return new Pair(actionIdx, featureSet.make(actions.get(actionIdx).getSecond()).get(featureIdx));
        }else{
            int[] indicesOfMax;
            if(actions.size() == 0)
                return new Pair(0,0.);


            indicesOfMax = findIndicesOfBestValue(actions, sign, featureSet, featureIdx);
            double[] values = new double[indicesOfMax.length];
            for (int i = 0; i < indicesOfMax.length; i++) {
                values[i] = expectationNode(state, actions.get(indicesOfMax[i]).getFirst(), ply);
            }
            int[] max = Compute.indicesOfMax(values);
            int randomMax = random.nextInt(max.length);
            return new Pair(indicesOfMax[randomMax], values[randomMax]);
        }
    }

    private static int[] findIndicesOfBestValue(List<Pair<Action, Features>> actions, int sign, FeatureSet featureSet, int featureIdx) {
        double[][] objects = new double[actions.size()][featureSet.featureNames().size()];
        //fill objects only with all cues
        for (int i = 0; i < actions.size(); i++) {
            List<Double> valuesList = featureSet.make(actions.get(i).getSecond());
            for (int j = 0; j < valuesList.size(); j++) {
                objects[i][j] = valuesList.get(j);
            }
        }

        double[] signedFeatureValue = new double[objects.length];
        for (int i = 0; i < objects.length; i++)
            signedFeatureValue[i] = objects[i][featureIdx]*sign;

        return Compute.indicesOfMax(signedFeatureValue);
    }

    double expectationNode(State state, Action action, int ply) {
        double expectation = 0;
        List<State> sprimes = new ArrayList<>();
        List<Double> probs = new ArrayList<>();
        state.getEffect(action, sprimes, probs);
        double prob = 1/7;
        for (State tetrisState : sprimes) {
            List<Pair<Action, Features>> actions = tetrisState.getActionFeaturesList();
            actions = actions.stream().filter(p -> task.taskEnds(p.getSecond())).collect(Collectors.toList());//filter out gameover actions.
            double value = decisionNode(tetrisState, actions, ply - 1).getSecond();
            expectation += value * prob;
        }
        return expectation;
    }
}
