package analysis;


import analysis.util.BctsActions;
import domains.FeatureSet;
import domains.tetris.*;
import org.apache.commons.math3.util.Pair;
import util.DistinctCounter;
import util.LinearDecisionRule;

import java.util.List;
import java.util.stream.Collectors;

public class CumDominanceAnalysis implements Analysis{

    GeneralReport report;
    List<Double> weightVector;
    double[] weightArray;

    FeatureSet featureSet;

    public CumDominanceAnalysis(FeatureSet featureSet){
        this.featureSet = featureSet;
    }


    @Override
    public void startReport(String reportPath) {
        this.report = new GeneralReport(reportPath);
        this.weightVector = TetrisWeightVector.make("bcts");
        this.weightArray = new double[weightVector.size()];
        for (int i = 0; i < weightArray.length; i++)
            weightArray[i] = weightVector.get(i);
        report.addLine("placements,distinct,pareto,pareto_distinct,intercept");
    }

    @Override
    public void executeAndWriteLineToReport(TetrisState stateBefore, TetrisAction action) {
        List<Pair<TetrisAction,TetrisFeatures>> actionFeatures = stateBefore.getActionsFeaturesList();
        actionFeatures = actionFeatures.stream().filter(p -> !p.getSecond().gameOver).collect(Collectors.toList()); //Filter out actions that lead to gameover.
        List<TetrisAction> bctsActions = BctsActions.get(stateBefore);

        int agentOptionsAreDominant = 0;

        double[][] objects = new double[actionFeatures.size()][weightVector.size()];

        //fill objects
        for (int i = 0; i < actionFeatures.size(); i++) {
            List<Double> valuesList = featureSet.make(actionFeatures.get(i).getSecond());
            for (int j = 0; j < valuesList.size(); j++) {
                objects[i][j] = valuesList.get(j);
            }
        }
        //count pareto
        boolean[] pareto = LinearDecisionRule.paretoCumDominanceSet(weightArray, objects);
        int numPareto = 0;
        for (int i = 0; i < pareto.length; i++)
            if (pareto[i])
                numPareto++;

        double[][] paretoObjects = new double[numPareto][weightArray.length];

        //fill pareto objects
        int paretoIdx = 0;
        for (int i = 0; i < objects.length; i++) {
            if (pareto[i]) {
                for (TetrisAction bctsAction : bctsActions) {
                    if(actionFeatures.get(i).getFirst().equals(bctsAction))
                        agentOptionsAreDominant = 1;
                }
                for (int j = 0; j < objects[0].length; j++) {
                    paretoObjects[paretoIdx][j] = objects[i][j];
                }
                paretoIdx++;
            }
        }
        assert agentOptionsAreDominant == 1;
        if(numPareto > 0) {
            report.addLine(objects.length + "," + DistinctCounter.howManyDistinct(objects) + "," + numPareto + "," + DistinctCounter.howManyDistinct(paretoObjects) + "," + agentOptionsAreDominant);
            System.out.println(objects.length + "," + DistinctCounter.howManyDistinct(objects) + "," + numPareto + "," + DistinctCounter.howManyDistinct(paretoObjects) + "," + agentOptionsAreDominant);
        }
    }

    @Override
    public void finishReport() {
        report.generateNew();
    }
}
