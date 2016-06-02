package policy;

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
    String featureSet;
    Random random;

    public LinearPick(List<Double> weights, String featureSet, Random random){
        this.weights = weights;
        this.featureSet = featureSet;
        this.random = random;
    }

    public int pick(TetrisState state,  List<Pair<TetrisAction, TetrisFeatures>> actions) {
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
}
