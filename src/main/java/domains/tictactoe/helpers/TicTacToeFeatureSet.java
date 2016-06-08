package domains.tictactoe.helpers;



import domains.FeatureSet;
import domains.Features;

import java.util.ArrayList;
import java.util.List;

public class TicTacToeFeatureSet implements FeatureSet {

    final String featureSet;

    public TicTacToeFeatureSet(String featureSet){
        this.featureSet = featureSet;
    }

    @Override
    public List<Double> make( Features features){
        TicTacToeFeatures ticTacToeFeatures = (TicTacToeFeatures) features;
        List<Double> featureValues = new ArrayList<>();
        if(featureSet.equals("all")){
            featureValues.add((double)ticTacToeFeatures.win);
            featureValues.add((double)ticTacToeFeatures.emptyThreeRow);
            featureValues.add((double)ticTacToeFeatures.oBlocks);
            featureValues.add((double)ticTacToeFeatures.oBlocksDelta);
            featureValues.add((double)ticTacToeFeatures.xBlocks);
            featureValues.add((double)ticTacToeFeatures.xBlocksDelta);
            featureValues.add((double)ticTacToeFeatures.oCenter);
            featureValues.add((double)ticTacToeFeatures.xCenter);
            featureValues.add((double)ticTacToeFeatures.oWinChance);
            featureValues.add((double)ticTacToeFeatures.xWinChance);
            featureValues.add((double)ticTacToeFeatures.oCorners);
            featureValues.add((double)ticTacToeFeatures.xCorners);
            featureValues.add((double)ticTacToeFeatures.xCenterDelta);
            featureValues.add((double)ticTacToeFeatures.xCornersDelta);
            featureValues.add((double)ticTacToeFeatures.xSide);
            featureValues.add((double)ticTacToeFeatures.xSideDelta);
        }
        return featureValues;
    }

    @Override
    public List<String> featureNames(){
        List<String> names = new ArrayList<>();
        if(featureSet.equals("all")){
            names.add("win");
            names.add("emptyThreeRow");
            names.add("oBlocks");
            names.add("oBlocksDelta");
            names.add("xBlocks");
            names.add("xBlocksDelta");
            names.add("oCenter");
            names.add("xCenter");
            names.add("oWinChance");
            names.add("xWinChance");
            names.add("oCorners");
            names.add("xCorners");
            names.add("xCenterDelta");
            names.add("xCornersDelta");
            names.add("xSide");
            names.add("xSideDelta");
        }
        return names;
    }

}
