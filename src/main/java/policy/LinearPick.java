package policy;

import domains.Action;
import domains.FeatureSet;
import domains.Features;
import domains.State;
import domains.tetris.TetrisAction;
import domains.tetris.TetrisFeatureSet;
import domains.tetris.TetrisFeatures;
import domains.tetris.TetrisState;
import org.apache.commons.math3.util.Pair;
import util.Compute;
import util.UtilAmpi;

import java.util.List;
import java.util.Random;



public class LinearPick implements PickAction{
    List<Double> weights;
    FeatureSet featureSet;
    Random random;

    public LinearPick(List<Double> weights, FeatureSet featureSet, Random random){
        this.weights = weights;
        this.featureSet = featureSet;
        this.random = random;
    }

    public int[] pick(State state, List<Pair<Action, Features>> actions) {
        assert !actions.isEmpty();
        double[] values = new double[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            Features features = actions.get(i).getSecond();
            List<Double> featureValues = featureSet.make(features);
            values[i] = UtilAmpi.dotproduct(weights, featureValues);
        }
        if(actions.isEmpty())
            return new int[]{};

        int[] maxIndices = Compute.indicesOfMax(values);
//        int chosenAction = maxIndices[random.nextInt(maxIndices.length)];
        return maxIndices;
    }
}
