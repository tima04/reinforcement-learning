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
import util.LinearDecisionRule;
import util.UtilAmpi;

import java.util.List;
import java.util.Random;

/**
 * Created by simonalgorta on 02/06/16.
 */
public class MultiPareto implements PickAction{

    double[] weights;
    FeatureSet featureSet;
    UtilAmpi.ActionType type;
    List<List<Integer>> cueGroups;
    Pareto pareto;
    Random random;

    public MultiPareto(double[] weights, FeatureSet featureSet, List<List<Integer>> cueGroups, UtilAmpi.ActionType type, Random random){
        this.weights = weights;
        this.featureSet = featureSet;
        this.type = type;
        this.cueGroups = cueGroups;
        pareto = new Pareto(weights, featureSet, type, random);
        this.random = random;
    }

    /**
     * Picks randomly from the intersect of two partial pareto sets. If the intersect is empty, it picks randomly from the full pareto set.
     * @param actionFeatures
     * @return
     */
    public int pick(State state, List<Pair<Action, Features>> actionFeatures){

        double[][] objects = new double[actionFeatures.size()][weights.length];

        //fill objects only with all cues
        for (int i = 0; i < actionFeatures.size(); i++) {
            List<Double> valuesList = featureSet.make(actionFeatures.get(i).getSecond());
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
                List<Double> valuesList = featureSet.make(actionFeatures.get(i).getSecond());
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
            return pareto.pick(state, actionFeatures);

        return indicesOfMax[random.nextInt(indicesOfMax.length)];
    }
}
