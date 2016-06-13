import analysis.GeneralReport;
import domains.Action;
import domains.Features;
import domains.Task;
import domains.tetris.*;
import domains.tetris.helpers.ApproximateDominanceSpec;
import org.apache.commons.math3.util.Pair;
import policy.*;
import util.UtilAmpi;

import java.util.*;
import java.util.stream.Collectors;

public class playTetris {

    public static String path = "/Users/simonalgorta/IdeaProjects/rl5/src/main/resources/";

    public static void main(String[] args) {
        List<Double> weights = new ArrayList<>();
//        weights.add(-13.08);
//        weights.add(-19.77);
//        weights.add(-9.22);
//        weights.add(-10.49);
//        weights.add( 6.60);
//        weights.add(-12.63);
//        weights.add(-24.04);
//        weights.add(-1.61);


        double[] paretoWeights = new double[]{-5, -6, -2, -3, 1, -4, -7, -1}; //It should have the same effect as bcts since the direction and order are the same.
        int[] paretoSigns = new int[]{-1, -1, -1, -1, 1, -1, -1, -1};

        Random random = new Random();
                List<List<Integer>> cueGroups = new ArrayList<>();
                cueGroups.add(Arrays.asList(1,3,5,6));
                cueGroups.add(Arrays.asList(0,2,4,7));

        TetrisParameters.getInstance().setSize(10,10);


//        Pair<List<Integer>, List<Integer>> apprDominanceSpec = ApproximateDominanceSpec.get(0.95);
//        playGames(100, new ParetoAppr(paretoWeights, new TetrisFeatureSet("bcts"), UtilAmpi.ActionType.CUMDOM, apprDominanceSpec.getFirst(), apprDominanceSpec.getSecond(), random), random,
//                "apprcumdom0.95.txt");
//        apprDominanceSpec = ApproximateDominanceSpec.get(0.98);
//        playGames(100, new ParetoAppr(paretoWeights, new TetrisFeatureSet("bcts"), UtilAmpi.ActionType.CUMDOM, apprDominanceSpec.getFirst(), apprDominanceSpec.getSecond(), random), random,
//                "apprcumdom0.98.txt");
//        apprDominanceSpec = ApproximateDominanceSpec.get(0.99);
//        playGames(100, new ParetoAppr(paretoWeights, new TetrisFeatureSet("bcts"), UtilAmpi.ActionType.CUMDOM, apprDominanceSpec.getFirst(), apprDominanceSpec.getSecond(), random), random,
//                "apprcumdom0.99.txt");
//
//        apprDominanceSpec = ApproximateDominanceSpec.get(0.95);
//        playGames(100, new ParetoAppr(paretoWeights, new TetrisFeatureSet("bcts"), UtilAmpi.ActionType.DOM, apprDominanceSpec.getFirst(), apprDominanceSpec.getSecond(), random), random,
//                "apprdom0.95.txt");
//        apprDominanceSpec = ApproximateDominanceSpec.get(0.98);
//        playGames(100, new ParetoAppr(paretoWeights, new TetrisFeatureSet("bcts"), UtilAmpi.ActionType.DOM, apprDominanceSpec.getFirst(), apprDominanceSpec.getSecond(), random), random,
//                "apprdom0.98.txt");
//        apprDominanceSpec = ApproximateDominanceSpec.get(0.99);
//        playGames(100, new ParetoAppr(paretoWeights, new TetrisFeatureSet("bcts"), UtilAmpi.ActionType.DOM, apprDominanceSpec.getFirst(), apprDominanceSpec.getSecond(), random), random,
//                "apprdom0.99.txt");


//        playGames(10000, new SingleCueTallyRest(paretoSigns, 6, new TetrisFeatureSet("bcts"), random), random);
        playGames(100, new LinearPick(weights, new TetrisFeatureSet("thierry"), random), random, "");
//        playSteps(10000, new LinearPick(weights, new TetrisFeatureSet("bcts"), random), random);
//        playGames(10, new MultiPareto(paretoWeights, new TetrisFeatureSet("bcts"), random), cueGroups, UtilAmpi.ActionType.CUMDOM, random), random);
//        playGames(1000, new SingleCue(-1, 6, new TetrisFeatureSet("bcts"), random), 3, random, new TetrisTaskLines(0.9)), random);

    }

    private static void playGames(int numGames, PickAction pickAction, Random random, String scoreReportName) {

        Task task = new TetrisTaskLines(0.9);
        int totalScore = 0;
        int steps = 0;
        GeneralReport scoreReport = new GeneralReport(path + "tetris/scores/policy/" + scoreReportName);
        scoreReport.addLine("game,score,steps");
        for (int game = 0; game < numGames; game++) {
            int score = 0;
            steps = 0;

            TetrisState state = new TetrisState(random);
            long t0 = System.currentTimeMillis();
//            GeneralReport detailedReport = new GeneralReport(path + "tetris/icml2016replication/rawGames/dom/detail_dom.txt");
            while(!state.features.gameOver) {
                steps++;
                List<Pair<Action, Features>> actions = state.getActionFeaturesList();
                actions = actions.stream().filter(p -> !task.taskEnds(p.getSecond())).collect(Collectors.toList()); //Filter out actions that lead to gameover.

                if(actions.size() == 0) { // no actions available
                    state.nextState(0, 0, random);
                    break;
                }

                int actionIndex = pickAction.pick(state, actions);
                TetrisAction action  = (TetrisAction) actions.get(actionIndex).getFirst();
//                detailedReport.addLine(state.getString()+","+action.col+"_"+action.rot);
//                detailedReport.generate();
                state.nextState(action.col, action.rot, random);
//                state.print();
//                state.printFeatures(state.features);
//                System.out.println(state.getString());
                score += state.features.nClearedLines;
            }
            scoreReport.addLine(game+","+score+","+steps);
//            System.out.println("_____________");
            System.out.println("lines cleared: "+score);
            totalScore = totalScore + score;
            System.out.println("Time spent: " + (System.currentTimeMillis() - t0)/60000. + " minutes");

        }
        scoreReport.generateNew();
        //        System.out.println("Total steps: " + totalSteps);
        System.out.println("mean: " +totalScore/numGames);
    }


    private static void playSteps(int numSteps, PickAction pickAction, Random random) {

        Task task = new TetrisTaskLines(0.9);
        int totalScore = 0;
//        GeneralReport scoreReport = new GeneralReport(path + "tetris/scores/policy/singlecue_tallyrest.txt");
//        scoreReport.addLine("game,score,steps");
        int score = 0;
        long t0 = System.currentTimeMillis();

        for (int steps = 0; steps < numSteps; ) {
            TetrisState state = new TetrisState(random);
//            GeneralReport detailedReport = new GeneralReport(path + "tetris/icml2016replication/rawGames/dom/detail_dom.txt");
            while(!state.features.gameOver && steps < numSteps) {
                steps++;
                List<Pair<Action, Features>> actions = state.getActionFeaturesList();
                actions = actions.stream().filter(p -> !task.taskEnds(p.getSecond())).collect(Collectors.toList()); //Filter out actions that lead to gameover.

                if(actions.size() == 0) { // no actions available
                    state.nextState(0, 0, random);
                    break;
                }

                int actionIndex = pickAction.pick(state, actions);
                TetrisAction action  = (TetrisAction) actions.get(actionIndex).getFirst();
//                detailedReport.addLine(state.getString()+","+action.col+"_"+action.rot);
//                detailedReport.generate();
                state.nextState(action.col, action.rot, random);
//                state.print();
//                state.printFeatures(state.features);
//                System.out.println(state.getString());
                score += state.features.nClearedLines;
            }
//            scoreReport.addLine(score+"");
//            System.out.println("_____________");


        }
        System.out.println("lines cleared: "+score);
        totalScore = totalScore + score;
        System.out.println("Time spent: " + (System.currentTimeMillis() - t0)/60000. + " minutes");
//        scoreReport.generateNew();
        //        System.out.println("Total steps: " + totalSteps);
        System.out.println("mean: " +totalScore);
    }
}
