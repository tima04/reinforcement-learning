package algs.irl;



import algs.Game;
import algs.rl.Dpi;
import algs.rl.LambdaPI;
import algs.rl.ValueIteration;
import domains.FeatureSet;
import domains.Features;
import domains.State;
import domains.Task;
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
import util.RolloutUtil;
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
    static int numTrajectories = 10000;
    static int trajectoryLength = 10;
    static TetrisFeatureSet rewardFunctionFeatureSet = new TetrisFeatureSet("lagoudakisthierry");
    static TetrisFeatureSet policyFeatureSet = new TetrisFeatureSet("lagoudakisthierrybertsekas");
    static double gamma = 1;

    public static void main(String[] arg){
        setOutput("bcts");
        TetrisParameters.getInstance().setSize(20, 10);
        List<Double> weights = TetrisWeightVector.make("bcts");
        Policy optimalPolicy = new CustomPolicy(new LinearPick(weights, new TetrisFeatureSet("bcts"), random), new TetrisTaskLines(0.9), random);
        System.out.println("getting demonstrated trajectories . . .");
        List<State> initialStates = RolloutUtil.getRolloutSetTetris(new Game(random, new TetrisTaskLines(0.9)), numTrajectories, weights, new TetrisFeatureSet("bcts"), UtilAmpi.ActionType.ANY, new TetrisFeatureSet("bcts"), new double[]{}, random).stream().map(p -> (TetrisState)p).collect(Collectors.toList());
        List<List<Pair<State, Features>>> demonstratedTrajectories = IrlUtil.getTrajectories(numTrajectories, initialStates, optimalPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);

        maxEnt(demonstratedTrajectories, 10, numTrajectories, trajectoryLength, policyFeatureSet, rewardFunctionFeatureSet);
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

    public static List<Policy> maxEnt(List<List<Pair<State, Features>>> demonstratedTrajectories, int iterations, int numTrajectories, int trajectoryLength, FeatureSet policyFeatureSet, FeatureSet rewardFunctionFeatureSet) {
        List<Policy> policies = new ArrayList<>();

        List<Double> randWeights = new ArrayList<>();
        for (int i = 0; i < policyFeatureSet.featureNames().size(); i++)
            randWeights.add(random.nextGaussian());

        Policy closerPolicy = new CustomPolicy(new LinearPick(randWeights, policyFeatureSet, random), new TetrisTaskLines(0.9), random);

        System.out.println("calculating demonstrated feature expectations . . .");
        List<Double> ofe = IrlUtil.calculateFeatureExpectations(demonstratedTrajectories, gamma, rewardFunctionFeatureSet);


        List<State> initialStates = demonstratedTrajectories.stream().filter(p->p.size() > 0).map(p -> ((TetrisState)p.get(0).getFirst()).copy()).collect(Collectors.toList());
//        List<List<Pair<State, Features>>> trajectories = IrlUtil.getTrajectories(numTrajectories, initialStates, closerPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);
//        List<Double> fe = IrlUtil.calculateFeatureExpectationsRecursively(initialStates, new LinearPick(randWeights, policyFeatureSet, random), .9, rewardFunctionFeatureSet, 4);

        List<Double> fe;
        List<Double> rewardFunction = new ArrayList<>();
        for (int i = 0; i < ofe.size(); i++)
            rewardFunction.add(0.00001);

        //We calculate steady state distribution of random policy * features. See Ziebart 2008 Learning from Demonstrated Behavior
        List<Double> gradient;

        for (int i = 1; i <= iterations; i++) {
            System.out.println("********** iteration: "+ i);
            System.out.println("calculating feature expectations of policy i . . .");
            List<List<Pair<State, Features>>> trajectories = IrlUtil.getTrajectories(numTrajectories, initialStates, closerPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);
            fe = IrlUtil.calculateFeatureExpectations(trajectories, gamma, rewardFunctionFeatureSet);
//            fe = IrlUtil.calculateFeatureExpectationsRecursively(initialStates, closerPolicy.getPick(), gamma, rewardFunctionFeatureSet, trajectoryLength-1);


            //We calculate steady state distribution of random policy * features. See Ziebart 2008 Learning from Demonstrated Behavior
            gradient = new ArrayList<>();
            for (int j = 0; j < ofe.size(); j++) {
                gradient.add(ofe.get(j) - fe.get(j));
            }

            for (int j = 0; j < rewardFunction.size(); j++) {
//                rewardFunction.set(j, rewardFunction.get(j) * Math.exp(learningRate * gradient.get(j)));//online exponentiated gradient descent.
                rewardFunction.set(j, rewardFunction.get(j) + learningRate * gradient.get(j));

            }


            //print reward:
            for (int j = 0; j < rewardFunctionFeatureSet.featureNames().size(); j++)
                System.out.println("weight,"+i+","+rewardFunctionFeatureSet.featureNames().get(j) + "," + rewardFunction.get(j));
            System.out.println("****");
            closerPolicy = getNearOptimalPolicy(trajectoryLength - 1, rewardFunction, rewardFunctionFeatureSet, policyFeatureSet);
            policies.add(closerPolicy);
        }
        return policies;
    }



    private static Policy getNearOptimalPolicy(int rolloutLenght, List<Double> rewardFunction, FeatureSet rewardFunctionFeatureSet, FeatureSet policyFeatureSet) {

        List<Double> initWeightsDpi = new ArrayList<>();
        for (String s : policyFeatureSet.featureNames())
            initWeightsDpi.add(0.);


        System.out.println("Running DPI");
        Task task = new TetrisCustomTask(0.9, rewardFunction, rewardFunctionFeatureSet, random);
        Dpi dpi = new Dpi(
                new Game(random, task),
                policyFeatureSet,
                2,
                1000,
                rolloutLenght,
                0.9,
                1,
                initWeightsDpi,
                UtilAmpi.ActionType.ANY,
                random);
        dpi.setRounds(0);
        dpi.setTask(task);
        List<Double> weights = dpi.iterate();
        System.out.println("DPI done");
        return new CustomPolicy(new LinearPick(weights, policyFeatureSet, random), new TetrisTaskLines(0.9), random);
    }

    private static Policy getNearOptimalPolicyLPI(List<Double> rewardFunction, FeatureSet rewardFunctionFeatureSet, FeatureSet policyFeatureSet) {

        List<Double> initWeightsDpi = new ArrayList<>();
        for (String s : policyFeatureSet.featureNames())
            initWeightsDpi.add(0.);


        System.out.println("Running  Lambda PI");
        Task task = new TetrisCustomTask(0.9, rewardFunction, rewardFunctionFeatureSet, random);
        LambdaPI lpi = new LambdaPI(
                new Game(random, task),
                policyFeatureSet,
                15,
                0.5,
                0.5,
                initWeightsDpi,
                50000,
                UtilAmpi.ActionType.ANY,
                random);
        List<Double> weights = lpi.iterate();
        System.out.println("Lambda PI done");
        return new CustomPolicy(new LinearPick(weights, policyFeatureSet, random), new TetrisTaskLines(0.9), random);
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
