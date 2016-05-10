package domains.tetris;

import java.util.ArrayList;
import java.util.List;

public class ValueArrayMaker {

    public static List<Double> make(TetrisFeatures tetrisFeatures, String featureSet){
        List<Double> values = new ArrayList<>();
        if(featureSet.equals("thierry")){
//                TetrisFeatureExtractor.TetrisFeature.LANDING_HEIGHT,
//                TetrisFeatureExtractor.TetrisFeature.ERODED_CELLS,
//                TetrisFeatureExtractor.TetrisFeature.ROW_TRANSITIONS,
//                TetrisFeatureExtractor.TetrisFeature.COLUMN_TRANSITIONS,
//                TetrisFeatureExtractor.TetrisFeature.HOLES,
//                TetrisFeatureExtractor.TetrisFeature.CUMULATIVE_WELLS,
//                TetrisFeatureExtractor.TetrisFeature.HOLES_DEPTH,
//                TetrisFeatureExtractor.TetrisFeature.ROWS_WITH_HOLES,
//                TetrisFeatureExtractor.TetrisFeature.PATTERN_DIVERSITY
            values.add((double)tetrisFeatures.landingHeight);
            values.add((double)tetrisFeatures.erodedCells);
            values.add((double)tetrisFeatures.rowTransition);
            values.add((double)tetrisFeatures.colTransition);
//            values.add(tetrisFeatures.);
        }

        return values;
    }

}
