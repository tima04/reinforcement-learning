package policy;

import domains.*;
import org.apache.commons.math3.util.Pair;
import util.Compute;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SingleCueTallyRest implements PickAction{

    int[] signs;
    int featureIdx;
    FeatureSet featureSet;
    Random random;

    public SingleCueTallyRest(int[] signs, int featureIdx, FeatureSet featureSet, Random random){
        this.signs = signs;
        this.featureIdx = featureIdx;
        this.featureSet = featureSet;
        this.random = random;
    }
    /**
     * Single cue
     * @param actions
     * @return
     */
    public int[] pick(State state, List<Pair<Action, Features>> actions){
        return singleCueTallyRest(actions);
    }

    /**
     * Single cue

     * @param actions
     * @return
     */
    int[] singleCueTallyRest(List<Pair<Action, Features>> actions){
        int[] indicesOfMax;
        if(actions.size() == 0)
            return new int[]{};

        List<Integer> includingOnly = new ArrayList<>();
        for (int i = 0; i < actions.size(); i++)
            includingOnly.add(i);

        indicesOfMax = findIndicesOfBestValue(actions, signs[featureIdx], featureSet, featureIdx, includingOnly);

        includingOnly = new ArrayList<>();
        for (int i = 0; i < indicesOfMax.length; i++)
            includingOnly.add(indicesOfMax[i]);

        int[] votes = new int[actions.size()];
        for (int i = 0; i < featureSet.featureNames().size(); i++) { // We iterate over the other features
            if(i != featureIdx) {
                indicesOfMax = findIndicesOfBestValue(actions, signs[i], featureSet, i, includingOnly);
                for (int j = 0; j < indicesOfMax.length; j++) {
                    votes[indicesOfMax[j]] += 1;
                }
            }
        }

        indicesOfMax = Compute.indicesOfMax(votes);

        return indicesOfMax;
    }



    private static int[] findIndicesOfBestValue(List<Pair<Action, Features>> actions, int sign, FeatureSet featureSet, int featureIdx, List<Integer> includingOnly) {
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
            if(includingOnly.contains(i))
                signedFeatureValue[i] = objects[i][featureIdx]*sign;
            else
                signedFeatureValue[i] = Double.NEGATIVE_INFINITY;

        return Compute.indicesOfMax(signedFeatureValue);
    }


}
