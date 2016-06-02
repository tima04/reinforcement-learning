package util;


import domains.tetris.TetrisAction;
import domains.tetris.TetrisFeatureSet;
import domains.tetris.TetrisFeatures;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PickAction {

    public static int random(Random random, List<Pair<TetrisAction, TetrisFeatures>> actions) {
        return random.nextInt(actions.size());
    }

    public static int linear(List<Double> weights, String featureSet,  Random random, List<Pair<TetrisAction, TetrisFeatures>> actions){
        assert !actions.isEmpty();
        double[] values = new double[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            TetrisFeatures features = actions.get(i).getSecond();
            List<Double> featureValues = TetrisFeatureSet.make(features, featureSet);
            values[i] = UtilAmpi.dotproduct(weights, featureValues);
        }
        int[] maxIndices = Compute.indicesOfMax(values);
        int chosenAction = maxIndices[random.nextInt(maxIndices.length)];
        return chosenAction;
    }

    /**
     * Picks randomly from the intersect of two partial pareto sets. If the intersect is empty, it picks randomly from the full pareto set.
     * @param weights
     * @param random
     * @param actionFeatures
     * @param featureSet
     * @param cueGroups
     * @param type
     * @return
     */
    public static int multiPareto(double[] weights, Random random, List<Pair<TetrisAction, TetrisFeatures>> actionFeatures, String featureSet, List<List<Integer>> cueGroups, UtilAmpi.ActionType type){

        double[][] objects = new double[actionFeatures.size()][weights.length];

        //fill objects only with all cues
        for (int i = 0; i < actionFeatures.size(); i++) {
            List<Double> valuesList = TetrisFeatureSet.make(actionFeatures.get(i).getSecond(), featureSet);
            for (int j = 0; j < valuesList.size(); j++) {
                objects[i][j] = valuesList.get(j);
            }
        }


        int[] votes = new int[objects.length];

        for (List<Integer> cueGroup : cueGroups) {
            double[] weightsCueGroup = new double[cueGroup.size()];
            double[][] objectsCueGroup = new double[actionFeatures.size()][weightsCueGroup.length];
            for (int i = 0; i < cueGroup.size(); i++) {
                weightsCueGroup[i] = weights[cueGroup.get(i)];
            }

            //fill objects only with cues of cuegroup
            for (int i = 0; i < actionFeatures.size(); i++) {
                List<Double> valuesList = TetrisFeatureSet.make(actionFeatures.get(i).getSecond(), featureSet);
                int index = 0;
                for (int j = 0; j < valuesList.size(); j++) {
                    if(index < cueGroup.size() && cueGroup.get(index) == j) {
                        objectsCueGroup[i][index] = valuesList.get(j);
                        index++;
                    }
                }
            }

            boolean[] pareto_cuegroup;
            if(type.equals(UtilAmpi.ActionType.DOM)) {
                pareto_cuegroup = LinearDecisionRule.paretoDominanceSet(weightsCueGroup, objectsCueGroup);
            }else if(type.equals(UtilAmpi.ActionType.CUMDOM)){
                pareto_cuegroup = LinearDecisionRule.paretoCumDominanceSet(weightsCueGroup, objectsCueGroup);
            }else{
                pareto_cuegroup = LinearDecisionRule.paretoDominanceSet(weightsCueGroup, objectsCueGroup);
            }
            for (int i = 0; i < pareto_cuegroup.length; i++) {
                if(pareto_cuegroup[i])
                    votes[i]++;
            }

        }

        int[] indicesOfMax = Compute.indicesOfMax(votes);
        if(votes[indicesOfMax[0]] < 2)//If intersection is empty we choose from the complete pareto cum set.
            return pareto( weights, random , actionFeatures, featureSet, type);

        return indicesOfMax[random.nextInt(indicesOfMax.length)];
    }

    /**
     *
     * @param weights
     * @param random
     * @param actionFeatures
     * @param featureSet
     * @param type
     * @return
     */
    public static int pareto(double[] weights, Random random, List<Pair<TetrisAction, TetrisFeatures>> actionFeatures, String featureSet, UtilAmpi.ActionType type){
        double[][] objects = new double[actionFeatures.size()][weights.length];

        //fill objects only with all cues
        for (int i = 0; i < actionFeatures.size(); i++) {
            List<Double> valuesList = TetrisFeatureSet.make(actionFeatures.get(i).getSecond(), featureSet);
            for (int j = 0; j < valuesList.size(); j++) {
                objects[i][j] = valuesList.get(j);
            }
        }
        boolean[] pareto;
        if(type.equals(UtilAmpi.ActionType.DOM)) {
            pareto = LinearDecisionRule.paretoDominanceSet(weights, objects);
        }else if(type.equals(UtilAmpi.ActionType.CUMDOM)){
            pareto = LinearDecisionRule.paretoCumDominanceSet(weights, objects);
        }else{
            pareto = LinearDecisionRule.paretoDominanceSet(weights, objects);
        }
        List<Integer> paretoIndices = new ArrayList<>();
        for (int i = 0; i < pareto.length; i++) {
            if(pareto[i])
                paretoIndices.add(i);
        }
        if(paretoIndices.size() == 0)//All actions can lead to game over.
            return 0;
        return paretoIndices.get(random.nextInt(paretoIndices.size()));
    }

    /**
     *
     * @param weights
     * @param random
     * @param actionFeatures
     * @param featureSet
     * @param type
     * @return
     */
    public static int pareto_appr(double[] weights, Random random, List<Pair<TetrisAction, TetrisFeatures>> actionFeatures, String featureSet, UtilAmpi.ActionType type, int betterIn, int worseIn){
        double[][] objects = new double[actionFeatures.size()][weights.length];

        //fill objects only with all cues
        for (int i = 0; i < actionFeatures.size(); i++) {
            List<Double> valuesList = TetrisFeatureSet.make(actionFeatures.get(i).getSecond(), featureSet);
            for (int j = 0; j < valuesList.size(); j++) {
                objects[i][j] = valuesList.get(j);
            }
        }
        boolean[] pareto;
        if(type.equals(UtilAmpi.ActionType.DOM)) {
            pareto = LinearDecisionRule.paretoDominanceApprSet(weights, objects, betterIn, worseIn);
        }else if(type.equals(UtilAmpi.ActionType.CUMDOM)){
            pareto = LinearDecisionRule.paretoCumDominanceApprSet(weights, objects, betterIn, worseIn);
        }else{
            pareto = LinearDecisionRule.paretoDominanceApprSet(weights, objects, betterIn, worseIn);
        }
        List<Integer> paretoIndices = new ArrayList<>();
        for (int i = 0; i < pareto.length; i++) {
            if(pareto[i])
                paretoIndices.add(i);
        }
        if(paretoIndices.size() == 0)//All actions can lead to game over.
            return 0;
        return paretoIndices.get(random.nextInt(paretoIndices.size()));
    }

    /**
     * Single cue
     * @param weight
     * @param featureIdx
     * @param random
     * @param actions
     * @param featureSet
     * @return
     */
    static int pickActionSingleCue(double weight, int featureIdx, Random random, List<Pair<TetrisAction, TetrisFeatures>> actions, String featureSet){
        int[] indicesOfMax;
        if(actions.size() == 0)
            return 0;
        double[][] objects = new double[actions.size()][TetrisFeatureSet.featureNames(featureSet).size()];

        //fill objects only with all cues
        for (int i = 0; i < actions.size(); i++) {
            List<Double> valuesList = TetrisFeatureSet.make(actions.get(i).getSecond(), featureSet);
            for (int j = 0; j < valuesList.size(); j++) {
                objects[i][j] = valuesList.get(j);
            }
        }
        List<Integer> lastIndicesOfMax = new ArrayList<>();
        for (int i = 0; i < objects.length; i++)
            lastIndicesOfMax.add(new Integer(i));

        int sign = (int) (weight/Math.abs(weight));
        indicesOfMax = findIndicesOfBestValue(objects, sign, featureIdx, lastIndicesOfMax);
        lastIndicesOfMax = new ArrayList<>();
        for (int j = 0; j < indicesOfMax.length; j++)
            lastIndicesOfMax.add(indicesOfMax[j]);

        return lastIndicesOfMax.get(random.nextInt(lastIndicesOfMax.size()));
    }

    private static int[] findIndicesOfBestValue(double[][] objects, int sign, int j, List<Integer> includingOnly) {
        double[] signedFeatureValue = new double[objects.length];
        for (int i = 0; i < objects.length; i++) {
            if(includingOnly.contains(new Integer(i)))
                signedFeatureValue[i] = objects[i][j]*sign;
            else
                signedFeatureValue[i] = -Double.MAX_VALUE;
        }
        return Compute.indicesOfMax(signedFeatureValue);
    }

}
