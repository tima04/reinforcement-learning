package analysis;

import analysis.util.BctsActions;
import domains.FeatureSet;
import domains.tetris.*;
import org.apache.commons.math3.util.Pair;
import util.Compute;
import util.DistinctCounter;
import util.LinearDecisionRule;
import util.UtilAmpi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MultiDominanceAnalysis implements Analysis {

    private GeneralReport report;

    List<List<List<Integer>>> cueGroupsCombinations = new ArrayList<>();

    public int type = 1;

    public final static int CUMDOM = 1;
    public final static int DOM = 2;

    FeatureSet featureSet;


    public MultiDominanceAnalysis(int type, FeatureSet featureSet){
        this.type = type;
        this.featureSet = featureSet;
    }


    @Override
    public void startReport(String reportPath) {
        this.report = new GeneralReport(reportPath);
        report.addLine("combination,placements,distinct,pareto,pareto_distinct,ideal_in_intersect");
        cueGroupsCombinations = new ArrayList<>();
        for (int[] ints : UtilAmpi.combinations(8, 4)) {
            List<List<Integer>> cueGroups = new ArrayList<>();
            List<Integer> firstGroup = new ArrayList<>();
            List<Integer> secondGroup = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                secondGroup.add(i);
            }
            for (int anInt : ints) {
                firstGroup.add(anInt);
                secondGroup.remove(new Integer(anInt));
            }
            cueGroups.add(firstGroup);
            cueGroups.add(secondGroup);
            cueGroupsCombinations.add(cueGroups);
        }
        for (int[] ints : UtilAmpi.combinations(8, 3)) {
            List<List<Integer>> cueGroups = new ArrayList<>();
            List<Integer> firstGroup = new ArrayList<>();
            List<Integer> secondGroup = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                secondGroup.add(i);
            }
            for (int anInt : ints) {
                firstGroup.add(anInt);
                secondGroup.remove(new Integer(anInt));
            }
            cueGroups.add(firstGroup);
            cueGroups.add(secondGroup);
            cueGroupsCombinations.add(cueGroups);
        }
        for (int[] ints : UtilAmpi.combinations(8, 2)) {
            List<List<Integer>> cueGroups = new ArrayList<>();
            List<Integer> firstGroup = new ArrayList<>();
            List<Integer> secondGroup = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                secondGroup.add(i);
            }
            for (int anInt : ints) {
                firstGroup.add(anInt);
                secondGroup.remove(new Integer(anInt));
            }
            cueGroups.add(firstGroup);
            cueGroups.add(secondGroup);
            cueGroupsCombinations.add(cueGroups);
        }
        report.generateNew();

    }



    @Override
    public void executeAndWriteLineToReport(TetrisState stateBefore, TetrisAction action) {
        List<Pair<TetrisAction, TetrisFeatures>> actionFeatures = stateBefore.getActionsFeaturesList();
        List<Double> weights = TetrisWeightVector.make("bcts");
        double[][] objects = new double[actionFeatures.size()][weights.size()];
        List<TetrisAction> bctsActions = BctsActions.get(stateBefore);


        //fill objects only with all cues
        for (int i = 0; i < actionFeatures.size(); i++) {
            List<Double> valuesList = featureSet.make(actionFeatures.get(i).getSecond());
            for (int j = 0; j < valuesList.size(); j++) {
                    objects[i][j] = valuesList.get(j);
            }
        }

        for (List<List<Integer>> cueGroups : cueGroupsCombinations) {//For each combination
            int[] votes = new int[objects.length];
            for (List<Integer> cueGroup : cueGroups) {
                double[] weightsCueGroup = new double[cueGroup.size()];
                double[][] objectsCueGroup = new double[actionFeatures.size()][weightsCueGroup.length];
                for (int i = 0; i < cueGroup.size(); i++) {
                    weightsCueGroup[i] = weights.get(cueGroup.get(i));
                }

                //fill objects only with cues of cuegroup
                for (int i = 0; i < actionFeatures.size(); i++) {
                    List<Double> valuesList = featureSet.make(actionFeatures.get(i).getSecond());
                    int index = 0;
                    for (int j = 0; j < valuesList.size(); j++) {
                        if (index < cueGroup.size() && cueGroup.get(index) == j) {
                            objectsCueGroup[i][index] = valuesList.get(j);
                            index++;
                        }
                    }
                }

                boolean[] pareto_cuegroup = getPareto(weightsCueGroup, objectsCueGroup);
                for (int i = 0; i < pareto_cuegroup.length; i++) {
                    if (pareto_cuegroup[i])
                        votes[i]++;
                }

            }

            int intersect = 0;
            int[] indicesOfMax = Compute.indicesOfMax(votes);
            List<double[]> chosenObjects = new ArrayList<>();
            if(votes[indicesOfMax[0]] == 2) {//By checking only if the highest object has at least 2 votes we check only the intersection of pareto sets.
                for (int i = 0; i < indicesOfMax.length; i++) {
                    chosenObjects.add(objects[indicesOfMax[i]]);
                    for (TetrisAction bctsAction : bctsActions) {
                        if (actionFeatures.get(indicesOfMax[i]).getFirst().equals(bctsAction))
                            intersect = 1;
                    }
                }
            }
            String combination = "c";
            for (Integer integer : cueGroups.get(0)) {
                combination = combination +  integer;
            }

            report.addLine(combination+","+objects.length+","+ DistinctCounter.howManyDistinct(objects)+","+chosenObjects.size()+","+DistinctCounter.howManyDistinct(chosenObjects)+","+intersect);
//            System.out.println(combination+","+objects.length+","+ DistinctCounter.howManyDistinct(objects)+","+chosenObjects.size()+","+DistinctCounter.howManyDistinct(chosenObjects)+","+intersect);
            report.generate();
        }
    }


    private boolean[] getPareto(double[] weights, double[][] objects) {
        boolean intersect = false;
        //count pareto
        boolean[] pareto;
        if(type == CUMDOM) {
             pareto = LinearDecisionRule.paretoCumDominanceSet(weights, objects);
        }else{
            pareto = LinearDecisionRule.paretoDominanceSet(weights, objects);
        }
        return pareto;
    }



    @Override
    public void finishReport() {
        report.generate();
        System.out.println(report.filePath());
    }

}
