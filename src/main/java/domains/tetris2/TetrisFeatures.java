package algorta.domains.tetris2;

import algorta.scratch.util.Compute;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TetrisFeatures {

    public final int[] colHeights;
    public final int[] colDif;
    public final int[] wellDepth;//unused!
    public final int numHoles;
    public final int pileHeight;
    public final int numColTransitions;
    public final int numRowTransitions;
    public final int holesDepth;
    public final List<List<Integer>> wellDetail;
    public final List<Integer> rowsWithHoles;
    public final List<Pair<Integer,Integer>> coordinateHoles;
    public final List<Integer> linesCleared;
    public final int clearedBrickPieces;
    public final double landingHeight;
    public final int erodedCells;
    public final int cumulativeWells;
    public final int patternDiversity;
    public final double[] rbf;
    public final boolean gameOver;
    public final double averageHeightColumn;
    public final int numRows = 10;

    public final int lastPileHeight;
    public final int lastHeightDifference;
    public final double lastAverageHeightColumn;

    public final int holesChange;
    public final int pileHeightChange;
    public final double averageHeightColumnChange;
    public final int heightDifference;
    public final int heightDifferenceChange;

    public TetrisFeatures(int[] colHeights, int[] wellDepth, int numHoles, int numHolesChange, int lastPileHeight, double lastAverageHeightColumn,
                          int lastHeightDifference, int holesDepth, List<List<Integer>> wellDetail,
                          List<Integer> linesCleared, int clearedBrickPieces, int numColTransitions, int numRowTransitions,
                          List<Integer> rowsWithHoles, List<Pair<Integer,Integer>> coordinateHoles, double landingHeight, boolean gameOver) {
        this.colHeights = colHeights;
        this.wellDepth = wellDepth;
        this.numHoles = numHoles;
        this.holesChange = numHolesChange;
        this.holesDepth = holesDepth;
        this.linesCleared = linesCleared;
        this.clearedBrickPieces = clearedBrickPieces;
        this.numColTransitions = numColTransitions;
        this.numRowTransitions = numRowTransitions;
        this.rowsWithHoles = rowsWithHoles;
        this.coordinateHoles = coordinateHoles;
        this.landingHeight = landingHeight;
        this.erodedCells = linesCleared.size() * clearedBrickPieces;
        this.gameOver = gameOver;
        this.colDif = new int[colHeights.length-1];
        this.wellDetail = wellDetail;
        this.pileHeight = Compute.max(colHeights);
        this.pileHeightChange = pileHeight - lastPileHeight;
        this.averageHeightColumn = Compute.sum(colHeights)/colHeights.length;
        this.averageHeightColumnChange = averageHeightColumn - lastAverageHeightColumn;
        this.lastAverageHeightColumn = lastAverageHeightColumn;
        this.lastHeightDifference = lastHeightDifference;
        this.lastPileHeight = lastPileHeight;

        this.rbf = new double[5];
        for (int i = 0; i < 5; i++)
            rbf[i] = Math.exp(-1*Math.pow(averageHeightColumn-i*numRows/4,2)/Math.pow(2*(numRows/5),2));

        int tempCumulativeWells = 0;

        for (int col = 0; col < wellDetail.size(); col++) {//Measuring cumulative wells.
            Collections.sort(wellDetail.get(col), Collections.reverseOrder()); // We order the rows in which there is a well.
            int cum = 1;
            if(wellDetail.get(col).size() > 0) {//are there wells in this column?
                for (int row = 0; row <= wellDetail.get(col).get(0); row++) {//We iterate from row 0 to the last well.
                    if (wellDetail.get(col).contains(row)) {//if cell is a well
                        tempCumulativeWells += cum;//We add
                        cum++;//We accumulate.
                    } else if (isHole(row, col) || colHeights[col] < row + 1) {
                        cum++;//We accumulate.
                    } else {
                        cum = 1;//We restart.
                    }
                }
            }
        }

        this.cumulativeWells = tempCumulativeWells;

        List<Integer> surfacePatterns = new ArrayList<Integer>();

        int heightDifferenceTemp = 0;

        for (int col = 0; col < colHeights.length -1; col++) {
            int heightCol = colHeights[col];
            int heightNextCol = colHeights[col + 1];
            colDif[col] = Math.abs(heightCol - heightNextCol);
            heightDifferenceTemp += Math.abs(heightCol - heightNextCol);
            int thisPattern = (heightCol - heightNextCol);
            if(!surfacePatterns.contains(thisPattern) &&
                    thisPattern >= -2 &&
                    thisPattern <= 2){
                surfacePatterns.add(thisPattern);
            }
        }
        heightDifference = heightDifferenceTemp;
        heightDifferenceChange = heightDifference - lastHeightDifference;
        patternDiversity = surfacePatterns.size();
    }

    private boolean isHole(int row, int col) {
        for (Pair<Integer, Integer> hole : coordinateHoles)
            if(hole.getFirst() == row && hole.getSecond() == col)
                return true;

        return false;
    }

    public double[] getFeatureValuesArray(String featureSet){
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
           return new double[]{landingHeight, erodedCells, numRowTransitions,
                    numColTransitions, numHoles, cumulativeWells, holesDepth,
                    rowsWithHoles.size(), patternDiversity};
        }else if(featureSet.equals("thierryrbf")){
            //                TetrisFeatureExtractor.TetrisFeature.LANDING_HEIGHT,
            //                TetrisFeatureExtractor.TetrisFeature.ERODED_CELLS,
            //                TetrisFeatureExtractor.TetrisFeature.ROW_TRANSITIONS,
            //                TetrisFeatureExtractor.TetrisFeature.COLUMN_TRANSITIONS,
            //                TetrisFeatureExtractor.TetrisFeature.HOLES,
            //                TetrisFeatureExtractor.TetrisFeature.CUMULATIVE_WELLS,
            //                TetrisFeatureExtractor.TetrisFeature.HOLES_DEPTH,
            //                TetrisFeatureExtractor.TetrisFeature.ROWS_WITH_HOLES,
            //                TetrisFeatureExtractor.TetrisFeature.PATTERN_DIVERSITY
            //                TetrisFeatureExtractor.TetrisFeature.rbf0
            //                TetrisFeatureExtractor.TetrisFeature.rbf1
            //                TetrisFeatureExtractor.TetrisFeature.rbf2
            //                TetrisFeatureExtractor.TetrisFeature.rbf4

            return new double[]{landingHeight, erodedCells, numRowTransitions,
                    numColTransitions, numHoles, cumulativeWells, holesDepth,
                    rowsWithHoles.size(), patternDiversity, rbf[0], rbf[1], rbf[2], rbf[3], rbf[4], 1};
        }else if(featureSet.equals("bcts")){
//                    TetrisFeatureExtractor.TetrisFeature.HOLES,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_TRANSITIONS,
//                    TetrisFeatureExtractor.TetrisFeature.ROW_TRANSITIONS,
//                    TetrisFeatureExtractor.TetrisFeature.CUMULATIVE_WELLS,
//                    TetrisFeatureExtractor.TetrisFeature.ERODED_CELLS,
//                    TetrisFeatureExtractor.TetrisFeature.LANDING_HEIGHT,
//                    TetrisFeatureExtractor.TetrisFeature.ROWS_WITH_HOLES,
//                    TetrisFeatureExtractor.TetrisFeature.HOLES_DEPTH

            return new double[]{numHoles, numColTransitions, numRowTransitions,
                    cumulativeWells, erodedCells, landingHeight,
                    rowsWithHoles.size(), holesDepth};
        }else if((featureSet.equals("lagoudakisthierry"))){
//                    TetrisFeatureExtractor.TetrisFeature.HOLES,
//                    TetrisFeatureExtractor.TetrisFeature.PILE_HEIGHT,
//                    TetrisFeatureExtractor.TetrisFeature.AVERAGE_HEIGHT_COLUMN,
//                    TetrisFeatureExtractor.TetrisFeature.HEIGHT_DIFFERENCE,
//                    TetrisFeatureExtractor.TetrisFeature.REMOVED_LINES,
//                    TetrisFeatureExtractor.TetrisFeature.HOLES_CHANGE,
//                    TetrisFeatureExtractor.TetrisFeature.PILE_HEIGHT_CHANGE,
//                    TetrisFeatureExtractor.TetrisFeature.AVERAGE_HEIGHT_COLUMN_CHANGE,
//                    TetrisFeatureExtractor.TetrisFeature.HEIGHT_DIFFERENCE_CHANGE,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_TRANSITIONS,
//                    TetrisFeatureExtractor.TetrisFeature.ROW_TRANSITIONS,
//                    TetrisFeatureExtractor.TetrisFeature.CUMULATIVE_WELLS,
//                    TetrisFeatureExtractor.TetrisFeature.ERODED_CELLS,
//                    TetrisFeatureExtractor.TetrisFeature.LANDING_HEIGHT,
//                    TetrisFeatureExtractor.TetrisFeature.ROWS_WITH_HOLES,
//                    TetrisFeatureExtractor.TetrisFeature.HOLES_DEPTH,
//                    TetrisFeatureExtractor.TetrisFeature.PATTERN_DIVERSITY
            return new double[]{numHoles, pileHeight, averageHeightColumn, heightDifference, linesCleared.size(), holesChange, pileHeightChange, averageHeightColumnChange, heightDifferenceChange,
                                numColTransitions, numRowTransitions, cumulativeWells, erodedCells, landingHeight, rowsWithHoles.size(), holesDepth, patternDiversity};
        }else if((featureSet.equals("bertsekas"))) {
//                    TetrisFeatureExtractor.TetrisFeature.CONSTANT,
//                    TetrisFeatureExtractor.TetrisFeature.HOLES,
//                    TetrisFeatureExtractor.TetrisFeature.PILE_HEIGHT,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_ONE,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_TWO,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_THREE,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_FOUR,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_FIVE,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_SIX,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_SEVEN,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_EIGHT,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_NINE,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_ZERO,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_DIF_ZERO_ONE,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_DIF_ONE_TWO,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_DIF_TWO_THREE,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_DIF_THREE_FOUR,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_DIF_FOUR_FIVE,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_DIF_FIVE_SIX,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_DIF_SIX_SEVEN,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_DIF_SEVEN_EIGHT,
//                    TetrisFeatureExtractor.TetrisFeature.COLUMN_HEIGHT_DIF_EIGHT_NINE
            double[] fv = new double[22];
            fv[0] = 1; fv[1] = numHoles; fv[2] = pileHeight;
            for (int i = 0; i < colHeights.length; i++)
                fv[i+3] = colHeights[i];

            for (int i = 0; i < colDif.length; i++)
                fv[i+3+colHeights.length] = colDif[i];

            return fv;
        }
        return new double[]{};
    }

    public List<Double> getFeatureValues(String featureSet){
        List<Double> featureValues = new ArrayList<>();
        double[] valuesArray = getFeatureValuesArray(featureSet);
        for (int i = 0; i < valuesArray.length; i++)
            featureValues.add(valuesArray[i]);

        return featureValues;
    }

    public static List<String> getFeatureSetsNames(){
        List<String> featureSets = new ArrayList<>();
        featureSets.add("thierry");
        return featureSets;
    }

    public static List<String> getFeatureSetNames(String featureSetName) {
        List<String> featureNames = new ArrayList<>();
        if(featureSetName.equals("thierry")) {
            featureNames.add("LANDING_HEIGHT");
            featureNames.add("ERODED_CELLS");
            featureNames.add("ROW_TRANSITIONS");
            featureNames.add("COLUMN_TRANSITIONS");
            featureNames.add("HOLES");
            featureNames.add("CUMULATIVE_WELLS");
            featureNames.add("HOLES_DEPTH");
            featureNames.add("ROWS_WITH_HOLES");
            featureNames.add("PATTERN_DIVERSITY");
        }else if(featureSetName.equals("thierryrbf")){
            featureNames.add("LANDING_HEIGHT");
            featureNames.add("ERODED_CELLS");
            featureNames.add("ROW_TRANSITIONS");
            featureNames.add("COLUMN_TRANSITIONS");
            featureNames.add("HOLES");
            featureNames.add("CUMULATIVE_WELLS");
            featureNames.add("HOLES_DEPTH");
            featureNames.add("ROWS_WITH_HOLES");
            featureNames.add("PATTERN_DIVERSITY");
            featureNames.add("RBF1");
            featureNames.add("RBF2");
            featureNames.add("RBF3");
            featureNames.add("RBF4");
            featureNames.add("RBF5");
            featureNames.add("CONSTANT");
        }else if(featureSetName.equals("lagoudakisthierry")){
            featureNames.add("HOLES");
            featureNames.add("PILE_HEIGHT");
            featureNames.add("AVERAGE_HEIGHT_COLUMN");
            featureNames.add("HEIGHT_DIFFERENCE");
            featureNames.add("REMOVED_LINES");
            featureNames.add("HOLES_CHANGE");
            featureNames.add("PILE_HEIGHT_CHANGE");
            featureNames.add("AVERAGE_HEIGHT_COLUMN_CHANGE");
            featureNames.add("HEIGHT_DIFFERENCE_CHANGE");
            featureNames.add("COLUMN_TRANSITIONS");
            featureNames.add("ROW_TRANSITIONS");
            featureNames.add("CUMULATIVE_WELLS");
            featureNames.add("ERODED_CELLS");
            featureNames.add("LANDING_HEIGHT");
            featureNames.add("ROWS_WITH_HOLES");
            featureNames.add("HOLES_DEPTH");
            featureNames.add("PATTERN_DIVERSITY");
        }else if(featureSetName.equals("bcts")) {
            featureNames.add("HOLES");
            featureNames.add("COLUMN_TRANSITIONS");
            featureNames.add("ROW_TRANSITIONS");
            featureNames.add("CUMULATIVE_WELLS");
            featureNames.add("ERODED_CELLS");
            featureNames.add("LANDING_HEIGHT");
            featureNames.add("ROWS_WITH_HOLES");
            featureNames.add("HOLES_DEPTH");
        }else if(featureSetName.equals("bertsekas")){
            featureNames.add("CONSTANT");
            featureNames.add("HOLES");
            featureNames.add("PILE_HEIGHT");
            featureNames.add("COLUMN_HEIGHT_ZERO");
            featureNames.add("COLUMN_HEIGHT_ONE");
            featureNames.add("COLUMN_HEIGHT_TWO");
            featureNames.add("COLUMN_HEIGHT_THREE");
            featureNames.add("COLUMN_HEIGHT_FOUR");
            featureNames.add("COLUMN_HEIGHT_FIVE");
            featureNames.add("COLUMN_HEIGHT_SIX");
            featureNames.add("COLUMN_HEIGHT_SEVEN");
            featureNames.add("COLUMN_HEIGHT_EIGHT");
            featureNames.add("COLUMN_HEIGHT_NINE");
            featureNames.add("COLUMN_HEIGHT_DIF_ZERO_ONE");
            featureNames.add("COLUMN_HEIGHT_DIF_ONE_TWO");
            featureNames.add("COLUMN_HEIGHT_DIF_TWO_THREE");
            featureNames.add("COLUMN_HEIGHT_DIF_THREE_FOUR");
            featureNames.add("COLUMN_HEIGHT_DIF_FOUR_FIVE");
            featureNames.add("COLUMN_HEIGHT_DIF_FIVE_SIX");
            featureNames.add("COLUMN_HEIGHT_DIF_SIX_SEVEN");
            featureNames.add("COLUMN_HEIGHT_DIF_SEVEN_EIGHT");
            featureNames.add("COLUMN_HEIGHT_DIF_EIGHT_NINE");
        }
        return featureNames;
    }

    public TetrisFeatures copy() {
        List<Integer> linesClearedCopy = new ArrayList<>();
        for (int i = 0; i < linesCleared.size(); i++)
            linesClearedCopy.add(linesCleared.get(i));

        List<List<Integer>> wellDetailCopy = new ArrayList<List<Integer>>();
        for (int i = 0; i < wellDetail.size(); i++) {
            wellDetailCopy.add(new ArrayList<>());
            for (int j = 0; j < wellDetail.get(i).size(); j++) {
                wellDetailCopy.get(i).add(wellDetail.get(i).get(j));
            }
        }

        List<Integer> rowsWithHolesCopy = new ArrayList<>();
        for (int i = 0; i < rowsWithHoles.size(); i++)
            rowsWithHolesCopy.add(rowsWithHoles.get(i));

        List<Pair<Integer,Integer>> coordinateHolesCopy = new ArrayList<>();
        for (int i = 0; i < coordinateHoles.size(); i++)
            coordinateHolesCopy.add(coordinateHoles.get(i));

        return new TetrisFeatures(colHeights.clone(), wellDepth.clone(), numHoles, holesChange, lastPileHeight, lastAverageHeightColumn, lastHeightDifference,
                holesDepth, wellDetailCopy, linesClearedCopy, clearedBrickPieces, numColTransitions, numRowTransitions,
                rowsWithHolesCopy, coordinateHolesCopy, landingHeight, gameOver);
    }
}
