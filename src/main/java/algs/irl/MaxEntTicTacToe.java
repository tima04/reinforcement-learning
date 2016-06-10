package algs.irl;



import algs.rl.ValueIteration;
import domains.FeatureSet;
import domains.Features;
import domains.State;
import domains.tictactoe.TicTacToe;
import domains.tictactoe.TicTacToeCustomTask;
import domains.tictactoe.TicTacToeState;
import domains.tictactoe.TicTacToeTask;
import domains.tictactoe.helpers.TicTacToeFeatureSet;
import domains.tictactoe.helpers.TicTacToeFeatures;
import models.Policy;
import org.apache.commons.math3.util.Pair;
import util.IrlUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MaxEntTicTacToe {

    static Random random = new Random();
    
    static double learningRate = 0.01;
    static int numTrajectories = 1000;

    static FeatureSet featureSet = new TicTacToeFeatureSet("all");

    public static void main(String[] arg){
        setOutput("MaxEnt");
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
            System.setOut(new PrintStream(new File("src/main/resources/irl/tictactoe/maxent/"+fileName)));
//			System.setOut(new PrintStream(new File("scores/ampiq/"+fileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void maxEnt() {
        TicTacToe ticTacToe = new TicTacToe();
        List<State> states = ticTacToe.states().stream().collect(Collectors.<State>toList());
        System.out.println(states.size());
        ValueIteration valueIteration = new ValueIteration(states, ticTacToe, new TicTacToeTask(0.9), random);
        valueIteration.computeOptimalV();
        valueIteration.computeQFactors();
        Policy optimalPolicy = valueIteration.computeOptimalPolicy();
        Policy randomPolicy = valueIteration.createRandomPolicy();

        List<List<Pair<State, Features>>> optimalTrajectories = IrlUtil.getTrajectories(numTrajectories, new TicTacToeState(), optimalPolicy, new TicTacToeTask(0.9), random);
        List<List<Pair<State, Features>>> trajectories = IrlUtil.getTrajectories(numTrajectories, new TicTacToeState(), randomPolicy, new TicTacToeTask(0.9), random);

        List<Double> fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, featureSet);
        List<Double> ofe = IrlUtil.calculateFeatureExpectations(optimalTrajectories, 0.9, featureSet);
        double performance = calculatePerformance(optimalTrajectories);
        System.out.println("Optimal_Performance," + performance);
        performance = calculatePerformance(trajectories);
        System.out.println("0,Performance," + performance);

        List<Double> rewardFunction = new ArrayList<>();
        for (int i = 0; i < fe.size(); i++)
            rewardFunction.add(0.);

        //We calculate steady state distribution of random policy * features. See Ziebart 2008 Learning from Demonstrated Behavior
        List<Double> df = new ArrayList<>();
        for (int i = 0; i < fe.size(); i++)
            df.add(0.);

        List<Double> steadyState = empiricalSteadyStateVector(trajectories, states);
        for (int i = 0; i < states.size(); i++) {
            TicTacToeFeatures tf = ((TicTacToeState) states.get(i)).ticTacToeFeatures;
            List<Double> features = featureSet.make( tf);
            for (int j = 0; j < features.size(); j++) {
                df.set(j, df.get(j) + steadyState.get(i) * features.get(j));
            }
        }

        List<Double> gradient = new ArrayList<>();
        for (int i = 0; i < ofe.size(); i++) {
            gradient.add(ofe.get(i) - df.get(i));
        }

        for (int i = 0; i < rewardFunction.size(); i++)
            rewardFunction.set(i, rewardFunction.get(i) + learningRate * gradient.get(i));

        double prevValue = Double.NEGATIVE_INFINITY;
        for (int i = 1; i <= 15; i++) {
            System.out.println("********** iteration: "+ i);

            valueIteration = new ValueIteration(states, ticTacToe, new TicTacToeCustomTask(0.9, rewardFunction, featureSet), random);
            valueIteration.computeOptimalV();
            valueIteration.computeQFactors();
            randomPolicy = valueIteration.computeOptimalPolicy();
            trajectories = IrlUtil.getTrajectories(numTrajectories, new TicTacToeState(), randomPolicy, new TicTacToeTask(0.9), random);
            performance = calculatePerformance(trajectories);
            System.out.println(i+",Performance," + performance);
            System.out.println("Distance from optimal policy: " +policyDifferences(randomPolicy, optimalPolicy, states));
            fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, featureSet);

            //We calculate steady state distribution of random policy * features. See Ziebart 2008 Learning from Demonstrated Behavior
            df = new ArrayList<>();
            for (int j = 0; j < fe.size(); j++)
                df.add(0.);

            steadyState = empiricalSteadyStateVector(trajectories, states);
            for (int j = 0; j < states.size(); j++) {
                TicTacToeFeatures tf = ((TicTacToeState) states.get(j)).ticTacToeFeatures;
                List<Double> features = featureSet.make(tf);
                for (int k = 0; k < features.size(); k++) {
                    df.set(k, df.get(k) + steadyState.get(j) * features.get(k));
                }
            }

            gradient = new ArrayList<>();
            for (int j = 0; j < ofe.size(); j++) {
                gradient.add(ofe.get(j) - fe.get(j));
            }

            for (int j = 0; j < rewardFunction.size(); j++)
                rewardFunction.set(j, rewardFunction.get(j) + learningRate * gradient.get(j));

            //print reward:
            System.out.println(rewardFunction);
        }

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
