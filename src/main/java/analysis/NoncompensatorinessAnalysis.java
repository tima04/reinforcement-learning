package analysis;


import domains.tetris.*;
import org.apache.commons.math3.util.Pair;
import util.Compute;
import util.DistinctCounter;
import util.UtilAmpi;

import java.util.ArrayList;
import java.util.List;

public class NoncompensatorinessAnalysis implements Analysis{

    GeneralReport report;
    List<Double> weightVector;
    double[] weightArray;

    @Override
    public void startReport(String reportPath) {
        this.report = new GeneralReport(reportPath);
        this.weightVector = TetrisWeightVector.make("bcts");
        this.weightArray = new double[weightVector.size()];
        for (int i = 0; i < weightArray.length; i++)
            weightArray[i] = weightVector.get(i);
        String line = "placements,distinct,pareto,pareto_distinct,intersection";
        for (int i = 0; i < weightArray.length; i++) {
            line+=","+i+"fplacements";
        }
        for (int i = 0; i < weightArray.length; i++) {
            line+=","+i+"fpdistinct";
        }
        for (int i = 0; i < weightArray.length; i++) {
            line+=","+i+"fpintersect";
        }
        report.addLine(line);
    }

    @Override
    public void executeAndWriteLineToReport(TetrisState stateBefore, TetrisAction action) {
        List<Pair<TetrisAction, TetrisFeatures>> actionFeatures = stateBefore.getActionsFeaturesList();
        double[][] objects = new double[actionFeatures.size()][weightArray.length];
        List<TetrisAction> bctsActions = bctsAction(stateBefore, weightArray);
        int[] featureOrder = Compute.orderedIndices(weightArray);
        //fill objects
        for (int i = 0; i < actionFeatures.size(); i++) {
            List<Double> valuesList = TetrisFeatureSet.make(actionFeatures.get(i).getSecond(), "bcts");
            for (int j = 0; j < valuesList.size(); j++) {
                objects[i][j] = valuesList.get(j);
            }
        }


        int[] fplacements = new int[weightArray.length];
        int[] fpdistinct = new int[weightArray.length];
        int[] fpintersect = new int[weightArray.length];
        List<Integer> lastIndicesOfMax = new ArrayList<>();
        for (int i = 0; i < objects.length; i++)
            lastIndicesOfMax.add(new Integer(i));

        int[] indicesOfMax = new int[0];
        for (int i = 0; i < featureOrder.length; i++) {
            int sign = (int) (weightArray[featureOrder[i]]/Math.abs(weightArray[featureOrder[i]]));
            indicesOfMax = findIndicesOfBestValue(objects, sign, featureOrder[i], lastIndicesOfMax);
            fplacements[i] = indicesOfMax.length;
            fpdistinct[i] = DistinctCounter.howManyDistinct(objects, indicesOfMax);
            fpintersect[i] = 0;
            outerloop:
            for (int j = 0; j < indicesOfMax.length; j++) {
                for (int k = 0; k < bctsActions.size(); k++) {
                    if (actionFeatures.get(indicesOfMax[j]).getFirst().col == bctsActions.get(k).col &&
                            actionFeatures.get(indicesOfMax[j]).getFirst().rot == bctsActions.get(k).rot) {
                        fpintersect[i] = 1;
                        break outerloop;
                    }
                }
            }
            lastIndicesOfMax = new ArrayList<>();
            for (int j = 0; j < indicesOfMax.length; j++)
                lastIndicesOfMax.add(indicesOfMax[j]);

        }

        String line = objects.length+","+DistinctCounter.howManyDistinct(objects)+","+ lastIndicesOfMax.size()+","+DistinctCounter.howManyDistinct(objects, indicesOfMax)+","+fpintersect[featureOrder.length-1];
        for (int i = 0; i < fplacements.length; i++) {
            line+=","+fplacements[i];
        }
        for (int i = 0; i < fpdistinct.length; i++) {
            line+=","+fpdistinct[i];
        }
        for (int i = 0; i < fpintersect.length; i++) {
            line+=","+fpintersect[i];
        }
        report.addLine(line);
        System.out.println(line);
    }

    private List<TetrisAction> bctsAction(TetrisState stateBefore, double[] weights) {
        List<Pair<TetrisAction, TetrisFeatures>> actionFeatures = stateBefore.getActionsFeaturesList();
        double[] values = new double[actionFeatures.size()];
        for (int i = 0; i < actionFeatures.size(); i++) {
            List<Double> featureValues = TetrisFeatureSet.make(actionFeatures.get(i).getSecond(), "bcts");
            values[i] = UtilAmpi.dotproduct(featureValues, weights);
        }
        int[] max = Compute.indicesOfMax(values);
        List<TetrisAction> bctsActions = new ArrayList<>();
        for (int i = 0; i < max.length; i++) {
            bctsActions.add(actionFeatures.get(max[i]).getFirst());
        }
        return bctsActions;
    }

    private int[] findIndicesOfBestValue(double[][] objects, int sign, int j, List<Integer> includingOnly) {
        double[] signedFeatureValue = new double[objects.length];
        for (int i = 0; i < objects.length; i++) {
            if(includingOnly.contains(new Integer(i)))
                signedFeatureValue[i] = objects[i][j]*sign;
            else
                signedFeatureValue[i] = -Double.MAX_VALUE;
        }
        return Compute.indicesOfMax(signedFeatureValue);
    }

    @Override
    public void finishReport() {
        report.generateNew();
    }
}
