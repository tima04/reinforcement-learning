package policy;

import domains.tetris.TetrisAction;
import domains.tetris.TetrisFeatureSet;
import domains.tetris.TetrisFeatures;
import domains.tetris.TetrisState;
import org.apache.commons.math3.util.Pair;
import util.LinearDecisionRule;
import util.UtilAmpi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by simonalgorta on 02/06/16.
 */
public class Pareto implements PickAction {

    double[] weights;
    String featureSet;
    UtilAmpi.ActionType type;
    Random random;

    public Pareto(double[] weights, String featureSet, UtilAmpi.ActionType type, Random random){
        this.weights = weights;
        this.featureSet = featureSet;
        this.type = type;
        this.random = random;
    }

    /**
     *
     * @param actionFeatures
     * @return
     */
    public int pick(TetrisState state, List<Pair<TetrisAction, TetrisFeatures>> actionFeatures){
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
}
