import domains.tetris.*;
import org.apache.commons.math3.util.Pair;
import util.Compute;
import util.UtilAmpi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class createState {

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

         double[] paretoWeights = new double[]{-5, -6, -2, -3, 1, -4, -7, -1}; //It should have the same effect since the direction and order are the same.

        weights = Arrays.asList(0.06664687963909306, -0.06589950329665056, 0.8618569745289846, -0.006332430996340783, 8.067700976741143, -0.714334195643843, 0.07031484808105806, 6.979718295556643, 0.0019732789005759872, 0.051826846863774235, -0.04834502018518938, -0.03779567389047788, -0.0020529849983748063, -0.11995050219683696, -0.5897459987199273, -0.043555479641247744, 0.028936396535413744);

        EvaluateLinearAgent.gamesTetris(10, new Random(), "lagoudakisthierry", weights, UtilAmpi.ActionType.CUMDOM, "bcts", paretoWeights, true);


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
