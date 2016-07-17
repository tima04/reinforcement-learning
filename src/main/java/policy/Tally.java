package policy;

import domains.Action;
import domains.FeatureSet;
import domains.Features;
import domains.State;
import org.apache.commons.math3.util.Pair;
import util.Compute;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tally implements PickAction{

    int[] signs;
    int[] featureidx;
    FeatureSet featureSet;
    Random random;

    public Tally(int[] signs, int[] featureidx, FeatureSet featureSet, Random random){
        this.signs = signs;
        this.featureidx = featureidx;
        this.featureSet = featureSet;
        this.random = random;
    }
    /**
     * Single cue
     * @param actions
     * @return
     */
    public int[] pick(State state, List<Pair<Action, Features>> actions){
        return tally(actions);
    }

    /**
     * Single cue

     * @param actions
     * @return
     */
    int[] tally(List<Pair<Action, Features>> actions){
        int[] indicesOfMax;
        if(actions.size() == 0)
            return new int[]{};


        int[] votes = new int[actions.size()];
        for (int i = 0; i < featureidx.length; i++) { // We iterate over the other features
            indicesOfMax = findIndicesOfBestValue(actions, signs[i], featureSet, featureidx[i]);
            for (int j = 0; j < indicesOfMax.length; j++) {
                votes[indicesOfMax[j]] += 1;
            }
        }

        indicesOfMax = Compute.indicesOfMax(votes);

        return indicesOfMax;
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


    public int[] getFeaturesIdx() {
        return featureidx;
    }

    public int[] getDirections() {
        return signs;
    }
}
