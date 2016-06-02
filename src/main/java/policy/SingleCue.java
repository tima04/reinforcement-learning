package policy;

import domains.tetris.TetrisAction;
import domains.tetris.TetrisFeatureSet;
import domains.tetris.TetrisFeatures;
import domains.tetris.TetrisState;
import org.apache.commons.math3.util.Pair;
import util.Compute;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SingleCue implements PickAction{

    int sign;
    int featureIdx;
    String featureSet;
    int ply;
    Random random;

    public SingleCue(double sign, int featureIdx, String featureSet, int ply, Random random){
        this.sign = (int) (sign/Math.abs(sign));
        this.featureIdx = featureIdx;
        this.featureSet = featureSet;
        this.ply = ply;
        this.random = random;
    }
    /**
     * Single cue
     * @param actions
     * @return
     */
    public int pick(TetrisState state, List<Pair<TetrisAction, TetrisFeatures>> actions){
        return decisionNode(state, actions, ply).getFirst();
    }



    /**
     * Single cue

     * @param actions
     * @return
     */
    int singleCue(List<Pair<TetrisAction, TetrisFeatures>> actions){
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
    Pair<Integer, Double> decisionNode(TetrisState state, List<Pair<TetrisAction, TetrisFeatures>> actions, int ply){
        if(ply <= 0){
            if(actions.size() == 0)
                return new Pair(0, 0.);
            int actionIdx = singleCue(actions);
            return new Pair(actionIdx, TetrisFeatureSet.make(actions.get(actionIdx).getSecond(), featureSet).get(featureIdx));
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

    private static int[] findIndicesOfBestValue(List<Pair<TetrisAction, TetrisFeatures>> actions, int sign, String featureSet, int featureIdx) {
        double[][] objects = new double[actions.size()][TetrisFeatureSet.featureNames(featureSet).size()];
        //fill objects only with all cues
        for (int i = 0; i < actions.size(); i++) {
            List<Double> valuesList = TetrisFeatureSet.make(actions.get(i).getSecond(), featureSet);
            for (int j = 0; j < valuesList.size(); j++) {
                objects[i][j] = valuesList.get(j);
            }
        }

        double[] signedFeatureValue = new double[objects.length];
        for (int i = 0; i < objects.length; i++)
            signedFeatureValue[i] = objects[i][featureIdx]*sign;

        return Compute.indicesOfMax(signedFeatureValue);
    }

    double expectationNode(TetrisState state, TetrisAction action, int ply) {
        double expectation = 0;
        double prob = 1/7;
        for (TetrisState tetrisState : state.nextStates(action.col, action.rot)) {
            List<Pair<TetrisAction, TetrisFeatures>> actions = tetrisState.getActionsFeaturesList();
            actions = actions.stream().filter(p -> !p.getSecond().gameOver).collect(Collectors.toList());//filter out gameover actions.
            double value = decisionNode(tetrisState, actions, ply - 1).getSecond();
            expectation += value * prob;
        }
        return expectation;
    }
}
