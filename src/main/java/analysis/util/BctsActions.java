package analysis.util;


import domains.tetris.*;
import org.apache.commons.math3.util.Pair;
import util.Compute;
import util.UtilAmpi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BctsActions {

    public static List<TetrisAction> get(TetrisState state){
        List<Pair<TetrisAction, TetrisFeatures>> actionsWithGameover = state.getActionsFeaturesList();
        List<Pair<TetrisAction, TetrisFeatures>> actions = actionsWithGameover.stream()
                .filter(p -> !p.getSecond().gameOver).collect(Collectors.toList());
        List<Double> weights = TetrisWeightVector.make("bcts");
        List<TetrisAction> bctsActions = new ArrayList<>();
        if(actions.isEmpty())
            return bctsActions;

        double[] values = new double[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            TetrisFeatures features = actions.get(i).getSecond();
            values[i] = UtilAmpi.dotproduct(weights, TetrisFeatureSet.make(features, "bcts"));
        }
        int[] maxIndices = Compute.indicesOfMax(values);
        for (int i = 0; i < maxIndices.length; i++)
            bctsActions.add(actions.get(maxIndices[i]).getFirst());
        return bctsActions;
    }
}
