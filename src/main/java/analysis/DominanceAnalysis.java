package analysis;


import analysis.util.BctsActions;
import domains.tetris.*;
import org.apache.commons.math3.util.Pair;
import util.DistinctCounter;
import util.LinearDecisionRule;

import java.util.List;
import java.util.stream.Collectors;

public class DominanceAnalysis implements Analysis{

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
        report.addLine("placements,distinct,pareto,pareto_distinct,action_within_pareto,action_ideal");
    }

    @Override
    public void executeAndWriteLineToReport(TetrisState stateBefore, TetrisAction action) {
        List<Pair<TetrisAction,TetrisFeatures>> actionFeatures = stateBefore.getActionsFeaturesList();
        actionFeatures = actionFeatures.stream().filter(p -> !p.getSecond().gameOver).collect(Collectors.toList()); //Filter out actions that lead to gameover.
        List<TetrisAction> bctsActions = BctsActions.get(stateBefore);

        boolean agentOptionsAreDominant = false;
        int actionBetweenPareto = 0;
        int actionIdeal = 0;

        double[][] objects = new double[actionFeatures.size()][weightVector.size()];

        //fill objects
        for (int i = 0; i < actionFeatures.size(); i++) {
            List<Double> valuesList = TetrisFeatureSet.make(actionFeatures.get(i).getSecond(), "bcts");
            for (int j = 0; j < valuesList.size(); j++) {
                objects[i][j] = valuesList.get(j);
            }
        }
        //count pareto
        boolean[] pareto = LinearDecisionRule.paretoDominanceSet(weightArray, objects);
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
                        agentOptionsAreDominant = true;
                }
                if(actionFeatures.get(i).getFirst().equals(action)){
                    actionBetweenPareto = 1;
                }
                for (int j = 0; j < objects[0].length; j++) {
                    paretoObjects[paretoIdx][j] = objects[i][j];
                }
                paretoIdx++;
            }
        }

        for (TetrisAction bctsAction : bctsActions) {
            if(action.equals(bctsAction))
                actionIdeal = 1;
        }

        assert agentOptionsAreDominant;
        if(numPareto > 0) { //When numpareto is 0 the state is already gameover.
            report.addLine(objects.length + "," + DistinctCounter.howManyDistinct(objects) + "," + numPareto + "," + DistinctCounter.howManyDistinct(paretoObjects)+","+actionBetweenPareto+","+actionIdeal);
            System.out.println(objects.length + "," + DistinctCounter.howManyDistinct(objects) + "," + numPareto + "," + DistinctCounter.howManyDistinct(paretoObjects)+","+actionBetweenPareto+","+actionIdeal);
        }
    }

    @Override
    public void finishReport() {
        report.generateNew();
    }
}
