package domains.tetris;

import domains.FeatureSet;
import domains.Features;

import java.util.ArrayList;
import java.util.List;

public class TetrisFeatureSet implements FeatureSet{

    final String featureSet;

    public TetrisFeatureSet(String featureSet){
        this.featureSet = featureSet;
    }




    @Override
    public String name(){
        return featureSet;
    }

    @Override
    public List<Double> make(Features features){
        TetrisFeatures tetrisFeatures = (TetrisFeatures)features;
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
        }else if(featureSet.equals("bcts")){
            values.add((double)tetrisFeatures.nHoles);
            values.add((double)tetrisFeatures.colTransition);
            values.add((double)tetrisFeatures.rowTransition);
            values.add((double)tetrisFeatures.cumWells);
            values.add((double)tetrisFeatures.nErodedCells);
            values.add(tetrisFeatures.landingHeight);
            values.add((double)tetrisFeatures.nRowsWithHoles);
            values.add((double)tetrisFeatures.holesDepth);
        }else if(featureSet.equals("bertsekas")){
            values.add(1.);
            values.add((double)tetrisFeatures.nHoles);
            values.add((double)tetrisFeatures.pileHeight);
            for (int colHeight : tetrisFeatures.colHeights) {
                values.add((double)colHeight);
            }
            for (int colHeightDiff : tetrisFeatures.colHeightsDiff) {
                values.add((double)colHeightDiff);
            }
        }else if(featureSet.equals("lagoudakisthierry")){
            values.add((double)tetrisFeatures.nHoles);
            values.add((double)tetrisFeatures.pileHeight);
            values.add(tetrisFeatures.averageHeight);
            values.add((double)tetrisFeatures.sumHeightDiff);
            values.add((double)tetrisFeatures.nClearedLines);
            values.add((double)tetrisFeatures.nHolesDelta);
            values.add((double)tetrisFeatures.pileHeightDelta);
            values.add(tetrisFeatures.averageHeightDelta);
            values.add((double)tetrisFeatures.sumHeightDiffDelta);
            values.add((double)tetrisFeatures.colTransition);
            values.add((double)tetrisFeatures.rowTransition);
            values.add((double)tetrisFeatures.cumWells);
            values.add((double)tetrisFeatures.nErodedCells);
            values.add(tetrisFeatures.landingHeight);
            values.add((double)tetrisFeatures.nRowsWithHoles);
            values.add((double)tetrisFeatures.holesDepth);
            values.add((double)tetrisFeatures.nPatternDiversity);
        }
        return values;
    }

    @Override
    public List<String> featureNames(){
        List<String> names = new ArrayList<>();
        if(featureSet.equals("thierry")){
            names.add("landingHeight");
            names.add("nErodedCells");
            names.add("rowTransition");
            names.add("colTransition");
            names.add("nHoles");
            names.add("cumWells");
            names.add("holesDepth");
            names.add("nRowsWithHoles");
            names.add("nPatternDiversity");
        }else if(featureSet.equals("bcts")){
            names.add("nHoles");
            names.add("colTransition");
            names.add("rowTransition");
            names.add("cumWells");
            names.add("nErodedCells");
            names.add("landingHeight");
            names.add("nRowsWithHoles");
            names.add("holesDepth");
        }else if(featureSet.equals("bertsekas")){
            names.add("Constant");
            names.add("nHoles");
            names.add("pileHeight");
            for (int i = 0; i < 10; i++) {
                names.add("colHeight_"+i);
            }
            for (int i = 0; i < 9; i++) {
                names.add("colHeightDiff_"+i);
            }
        }else if(featureSet.equals("lagoudakisthierry")){
            names.add("nHoles");
            names.add("pileHeight");
            names.add("averageHeight");
            names.add("sumHeightDiff");
            names.add("nClearedLines");
            names.add("nHolesDelta");
            names.add("pileHeightDelta");
            names.add("averageHeightDelta");
            names.add("sumHeightDiffDelta");
            names.add("colTransition");
            names.add("rowTransition");
            names.add("cumWells");
            names.add("nErodedCells");
            names.add("landingHeight");
            names.add("nRowsWithHoles");
            names.add("holesDepth");
            names.add("nPatternDiversity");
        }
        return names;
    }

}
