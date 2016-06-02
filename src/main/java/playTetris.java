import domains.tetris.*;
import org.apache.commons.math3.util.Pair;
import policy.LinearPick;
import policy.MultiPareto;
import policy.PickAction;
import policy.SingleCue;
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

        double[] paretoWeights = new double[]{-5, -6, -2, -3, 1, -4, -7, -1}; //It should have the same effect as bcts since the direction and order are the same.

        Random random = new Random(1);
                List<List<Integer>> cueGroups = new ArrayList<>();
                cueGroups.add(Arrays.asList(1,3,5,6));
                cueGroups.add(Arrays.asList(0,2,4,7));

//        playGames(100, new LinearPick(weights, "bcts", random), random);
//        playGames(10, new MultiPareto(paretoWeights, "bcts", cueGroups, UtilAmpi.ActionType.CUMDOM, random), random);
        playGames(10, new SingleCue(-1, 6, "bcts", 1, random), random);

    }

    private static void playGames(int numGames, PickAction pickAction, Random random) {




        boolean limitGames = true; //If true the agent plays the number of games.
        int numSteps = 100000;
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


                int actionIndex = pickAction.pick(state, actions);
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


}
