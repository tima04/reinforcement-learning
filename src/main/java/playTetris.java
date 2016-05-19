import domains.tetris.*;
import org.apache.commons.math3.util.Pair;
import util.Compute;
import util.UtilAmpi;

import java.util.*;
import java.util.stream.Collectors;

public class playTetris {

    public static void main(String[] args) {

    List<Double> weights = new ArrayList<>();
    weights.add(-13.08);
    weights.add(-19.77);
    weights.add(-9.22);
    weights.add(-10.49);
    weights.add( 6.60);
    weights.add(-12.63);
    weights.add(-24.04);
    weights.add(-1.61);

        Random random = new Random(1);

        boolean limitGames = true; //If true the agent plays the number of games.
        int numSteps = 100000;
        int numGames = 10;
        int totalScore = 0;
        int steps;
        int totalSteps = 0;
    //        GeneralReport scoreReport = new GeneralReport(path + "tetris/icml2016replication/scores/doublecumdom.txt");
    //        scoreReport.addLine("game,score,steps");
    //        scoreReport.addLine("score");
        games:
        for (int game = 0; game < numGames || !limitGames; game++) {
            int score = 0;
            steps = 0;

            TetrisState state = new TetrisState(random);
            long t0 = System.currentTimeMillis();
    //            GeneralReport detailedReport = new GeneralReport(path + "tetris/icml2016replication/rawGames/dom/detail_dom.txt");

            while(!state.features.gameOver) {
                steps++;

                List<Pair<TetrisAction, TetrisFeatures>> actions = state.getActionsFeaturesList();
                actions = actions.stream().filter(p -> !p.getSecond().gameOver).collect(Collectors.toList()); //Filter out actions that lead to gameover.

                if(actions.size() == 0) { // no actions available
                    state.nextState(0, 0, random);
                    break;
                }

                int actionIndex = pickActionRandom(random, actions);
//                int actionIndex = pickActionGreedy(weights, random, actions);
                TetrisAction action  = actions.get(actionIndex).getFirst();
    //                detailedReport.addLine(state.getString()+","+action.col+"_"+action.rot);
    //                detailedReport.generate();

                state.nextState(action.col, action.rot, random);

                totalSteps++;

//                state.print();
//                state.printFeatures(state.features);
//                System.out.println(state.getString());
                score += state.features.nClearedLines;
                if(totalSteps > numSteps && !limitGames)
                    break games;
            }
    //            scoreReport.addLine(game+","+score+","+steps);
    //            scoreReport.addLine(score+"");
    //            System.out.println("_____________");
            System.out.println("lines cleared: "+score);
            totalScore = totalScore + score;
            System.out.println("Time spent: " + (System.currentTimeMillis() - t0)/60000. + " minutes");

        }
    //        scoreReport.generateNew();
    //        System.out.println("Total steps: " + totalSteps);
        System.out.println("mean: " +totalScore/numGames);

    }

    private static int pickActionRandom(Random random, List<Pair<TetrisAction, TetrisFeatures>> actions) {
        return random.nextInt(actions.size());
    }

    static int pickActionGreedy(List<Double> weights, Random random, List<Pair<TetrisAction, TetrisFeatures>> actions){

        double[] values = new double[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            TetrisFeatures features = actions.get(i).getSecond();
            List<Double> featureValues = TetrisFeatureSet.make(features, "bcts");
            values[i] = UtilAmpi.dotproduct(weights, featureValues);
        }
        if(values.length == 0)
            return 0;
        int[] maxIndices = Compute.indicesOfMax(values);
        int chosenAction = maxIndices[random.nextInt(maxIndices.length)];
        return chosenAction;
    }



}
