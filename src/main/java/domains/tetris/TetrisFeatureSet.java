package domains.tetris;

import java.util.ArrayList;
import java.util.List;

public class TetrisFeatureSet {

    public static List<Double> make(TetrisFeatures tetrisFeatures, String featureSet){
        List<Double> values = new ArrayList<>();
        if(featureSet.equals("thierry")){
            values.add(tetrisFeatures.landingHeight);
            values.add((double)tetrisFeatures.nErodedCells);
            values.add((double)tetrisFeatures.rowTransition);
            values.add((double)tetrisFeatures.colTransition);
            values.add((double)tetrisFeatures.nHoles);
            values.add((double)tetrisFeatures.cumWells);
            values.add((double)tetrisFeatures.holesDepth);
            values.add((double)tetrisFeatures.nRowsWithHoles);
            values.add((double)tetrisFeatures.nPatternDiversity);
        }
        return values;
    }

}
