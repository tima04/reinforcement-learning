package algs.irl;



import algs.Game;
import algs.rl.Dpi;
import algs.rl.ValueIteration;
import domains.FeatureSet;
import domains.Features;
import domains.State;
import domains.tetris.*;
import domains.tictactoe.TicTacToe;
import domains.tictactoe.TicTacToeCustomTask;
import domains.tictactoe.TicTacToeState;
import domains.tictactoe.TicTacToeTask;
import domains.tictactoe.helpers.TicTacToeFeatureSet;
import domains.tictactoe.helpers.TicTacToeFeatures;
import models.CustomPolicy;
import models.Policy;
import org.apache.commons.math3.util.Pair;
import policy.LinearPick;
import util.IrlUtil;
import util.UtilAmpi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MaxEntTetris {

    static double learningRate = 0.01;
    static Random random = new Random(2);
    static int numTrajectories = 100;
    static int trajectoryLength = 1000;
    static TetrisFeatureSet rewardFunctionFeatureSet = new TetrisFeatureSet("lagoudakisthierry");
    static TetrisFeatureSet policyFeatureSet = new TetrisFeatureSet("bcts");

    static FeatureSet featureSet = new TicTacToeFeatureSet("all");

    public static void main(String[] arg){
//        setOutput("MaxEnt");
        maxEnt();
    }

    private static void setOutput(String fileName) {
        Date yourDate = new Date();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-hh:mm");
        String date = DATE_FORMAT.format(yourDate);
        //redirecting output stream
        fileName += "_" + date + "_"+System.currentTimeMillis()+ ".log";
        //file = file + System.currentTimeMillis();
        try {
            System.out.println(fileName);
            System.setOut(new PrintStream(new File("src/main/resources/irl/tetris/maxent/"+fileName)));
//			System.setOut(new PrintStream(new File("scores/ampiq/"+fileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void maxEnt() {
        List<Double> weights = new ArrayList<>();
        weights.add(-13.08);
        weights.add(-19.77);
        weights.add(-9.22);
        weights.add(-10.49);
        weights.add( 6.60);
        weights.add(-12.63);
        weights.add(-24.04);
        weights.add(-1.61);

        Policy optimalPolicy = new CustomPolicy(new LinearPick(weights, policyFeatureSet, random), new TetrisTaskLines(0.9));

        List<Double> randWeights = new ArrayList<>();
        for (int i = 0; i < 8; i++)
            randWeights.add(random.nextGaussian());

        Policy randomPolicy = new CustomPolicy(new LinearPick(randWeights, policyFeatureSet, random), new TetrisTaskLines(0.9));

        System.out.println("getting demonstrated trajectories . . .");
        List<List<Pair<State, Features>>> demonstratedTrajectories = IrlUtil.getTrajectories(numTrajectories, new TetrisState(random), optimalPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);

        System.out.println("calculating feature expectations . . .");
        List<Double> ofe = IrlUtil.calculateFeatureExpectations(demonstratedTrajectories, 0.9, rewardFunctionFeatureSet);


        List<List<Pair<State, Features>>> trajectories = IrlUtil.getTrajectories(numTrajectories, new TetrisState(random), randomPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);
        List<Double> fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, rewardFunctionFeatureSet);

        List<Double> rewardFunction = new ArrayList<>();
        for (int i = 0; i < ofe.size(); i++)
            rewardFunction.add(0.);

        //We calculate steady state distribution of random policy * features. See Ziebart 2008 Learning from Demonstrated Behavior
        List<Double> gradient = new ArrayList<>();
        for (int i = 0; i < ofe.size(); i++) {
            gradient.add(ofe.get(i) - fe.get(i));
        }

        for (int i = 0; i < rewardFunction.size(); i++)
            rewardFunction.set(i, rewardFunction.get(i) + learningRate * gradient.get(i));

        double prevValue = Double.NEGATIVE_INFINITY;
        for (int i = 1; i <= 15; i++) {
            System.out.println("********** iteration: "+ i);

            trajectories = IrlUtil.getTrajectories(numTrajectories, new TetrisState(random), randomPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);
            fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, rewardFunctionFeatureSet);


            //We calculate steady state distribution of random policy * features. See Ziebart 2008 Learning from Demonstrated Behavior
            gradient = new ArrayList<>();
            for (int j = 0; j < ofe.size(); j++) {
                gradient.add(ofe.get(j) - fe.get(j));
            }

            for (int j = 0; j < rewardFunction.size(); j++)
                rewardFunction.set(j, rewardFunction.get(j) + learningRate * gradient.get(j));

            //print reward:
            for (int j = 0; j < rewardFunctionFeatureSet.featureNames().size(); j++)
                System.out.println(rewardFunctionFeatureSet.featureNames().get(j) + ": " + rewardFunction.get(j));

            randomPolicy = getNearOptimalPolicy(rewardFunction);
        }

    }

    private static Policy getNearOptimalPolicy(List<Double> rewardFunction) {

        List<Double> initWeightsDpi = new ArrayList<>();
        for (String s : policyFeatureSet.featureNames())
            initWeightsDpi.add(0.);


        System.out.println("Running DPI");
        Dpi dpi = new Dpi(new Game(random, new TetrisCustomTask(0.9, rewardFunction, rewardFunctionFeatureSet, random)),
                policyFeatureSet,
                2,
                5000,
                10,
                0.9,
                initWeightsDpi,
                UtilAmpi.ActionType.ANY,
                random);

        List<Double> weights = dpi.iterate();
        System.out.println("DPI done");
        return new CustomPolicy(new LinearPick(weights, policyFeatureSet, random), new TetrisTaskLines(0.9));
    }


    /**
     * According to Ziebart, the gradient of the log likelihood is:
     * optimal feature expectations - steady state distribution * feature expectations
     *
     */

    private static List<Double> steadyStateVector(Policy randomPolicy){
        return null;//// TODO: 03/06/16
    }

    private static List<Double> empiricalSteadyStateVector(List<List<Pair<State, Features>>> trajectories, List<State> states){
        HashMap<State, Double> stateFrequency = new HashMap<>();
        List<Double> steadyStateVector = new ArrayList<>();
        double maxFrequency = 0;
        for (List<Pair<State, Features>> trajectory : trajectories) {
            for (Pair<State, Features> stateFeaturesPair : trajectory) {
                if(!stateFrequency.containsKey(stateFeaturesPair.getKey())) {
                    stateFrequency.put(stateFeaturesPair.getKey(), 1.);
                    if(1 > maxFrequency)
                        maxFrequency = 1.;
                }else {
                    stateFrequency.put(stateFeaturesPair.getKey(), stateFrequency.get(stateFeaturesPair.getKey()) + 1);
                    if(stateFrequency.get(stateFeaturesPair.getKey()) > maxFrequency)
                        maxFrequency = stateFrequency.get(stateFeaturesPair.getKey());
                }
            }
        }

        for (State state : states) {
            if(stateFrequency.containsKey(state))
                steadyStateVector.add(stateFrequency.get(state)/maxFrequency);//Max frequency == 1;
            else
                steadyStateVector.add(0.);
        }

        return steadyStateVector;
    }

    private static double policyDifferences(Policy randomPolicy, Policy optimalPolicy, List<State> states) {
        int difference = 0;
        for (State state : states) {
            if(!randomPolicy.pickAction(state).equals(optimalPolicy.pickAction(state)))
                difference++;
        }
        return difference;
    }


    private static double calculatePerformance(List<List<Pair<State, Features>>> trajectories) {
        int won = 0;
        for (List<Pair<State, Features>> trajectory : trajectories) {
            won+=((TicTacToeFeatures)trajectory.get(trajectory.size()-1).getSecond()).win;
        }
        return won;
    }
}
