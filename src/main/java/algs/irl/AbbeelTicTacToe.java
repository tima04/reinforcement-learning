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
import libsvm.LibSVM;
import libsvm.svm_parameter;
import lpsolve.LpSolve;
import models.Policy;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import org.apache.commons.math3.util.Pair;
import util.IrlUtil;
import util.LpSolveUtil;
import util.UtilAmpi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AbbeelTicTacToe {

    static Random random = new Random();
    static int numTrajectories = 1000;
    static FeatureSet featureSetClass = new TicTacToeFeatureSet("all");

    public static void main(String[] arg){

//        ng2000();
//        setOutput("abbeel");
//        constraintPerPolicy();
//        abbeel2004();
        abbeel2004svm();
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
            System.setOut(new PrintStream(new File("src/main/resources/irl/tictactoe/abbeel/"+fileName)));
//			System.setOut(new PrintStream(new File("scores/ampiq/"+fileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void constraintPerPolicy() {
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

        List<Double> fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, featureSetClass);
        List<Double> ofe = IrlUtil.calculateFeatureExpectations(optimalTrajectories, 0.9, featureSetClass);
        double performance = calculatePerformance(optimalTrajectories);
        System.out.println("Optimal_Performance," + performance);
        performance = calculatePerformance(trajectories);
        System.out.println("0,Performance," + performance);

        List<Double> difference = new ArrayList<>();
        for (int i = 0; i < fe.size(); i++) {
            difference.add(ofe.get(i) - fe.get(i));
        }
        List sign = new ArrayList<>();
        List<Double> rightHand = new ArrayList<>();
        List<Double> objectiveFunction = new ArrayList<>();
        for (int i = 0; i < difference.size(); i++)
            objectiveFunction.add(-1.);

        List<List<Double>> constraints = new ArrayList<>();
        for (int i = 0; i < objectiveFunction.size(); i++) {
            List<Double> constraint = new ArrayList<>();
            for (int j = 0; j < objectiveFunction.size(); j++) {
                if(j != i)
                    constraint.add(0.);
                else
                    constraint.add(1.);
            }
            sign.add(LpSolve.LE);
            rightHand.add(1.);
            constraints.add(constraint);
        }

        List<Double> constraint = new ArrayList<>();
        for (int i = 0; i < difference.size(); i++)
            constraint.add(difference.get(i));

        constraints.add(constraint);
        sign.add(LpSolve.GE);
        rightHand.add(policyDifferences(randomPolicy, optimalPolicy, states)/states.size());

        List<Double> rewardFunction = LpSolveUtil.solve(objectiveFunction, constraints, sign, rightHand);
        for (int i = 1; i <= 15; i++) {
            System.out.println("********** iteration: "+ i);

            valueIteration = new ValueIteration(states, ticTacToe, new TicTacToeCustomTask(0.9, rewardFunction, featureSetClass), random);
            valueIteration.computeOptimalV();
            valueIteration.computeQFactors();
            randomPolicy = valueIteration.computeOptimalPolicy();
            trajectories = IrlUtil.getTrajectories(numTrajectories, new TicTacToeState(), randomPolicy, new TicTacToeTask(0.9), random);
            performance = calculatePerformance(trajectories);
            System.out.println(i+",Performance," + performance);
            System.out.println("Distance from optimal policy: " +policyDifferences(randomPolicy, optimalPolicy, states));
            fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, featureSetClass);
            constraint = new ArrayList<>();

            for (int j = 0; j < fe.size(); j++)
                constraint.add(ofe.get(j) - fe.get(j));

            constraints.add(constraint);
            sign.add(LpSolve.GE);
            rightHand.add(policyDifferences(randomPolicy, optimalPolicy, states)/states.size());

            rewardFunction = LpSolveUtil.solve(objectiveFunction, constraints, sign, rightHand);
            List<String> names = featureSetClass.featureNames();
            System.out.println("**************");
            System.out.println("Inferred reward function:");
            for (int j = 0; j < rewardFunction.size(); j++)
                System.out.println(names.get(j)+":"+rewardFunction.get(j));
        }
    }

    private static void abbeel2004() {
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

        List<Double> fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, featureSetClass);
        List<Double> ofe = IrlUtil.calculateFeatureExpectations(optimalTrajectories, 0.9, featureSetClass);
        double performance = calculatePerformance(optimalTrajectories);
        System.out.println("Optimal_Performance," + performance);
        performance = calculatePerformance(trajectories);
        System.out.println("0,Performance," + performance);

        List<Double> difference = new ArrayList<>();
        for (int i = 0; i < fe.size(); i++) {
            difference.add(ofe.get(i) - fe.get(i));
        }
        List sign = new ArrayList<>();
        List<Double> rightHand = new ArrayList<>();
        List<Double> objectiveFunction = new ArrayList<>();
        for (int i = 0; i < difference.size(); i++)
            objectiveFunction.add(0.);

        objectiveFunction.add(-1.);

        List<List<Double>> constraints = new ArrayList<>();
        for (int i = 0; i < objectiveFunction.size(); i++) {
            List<Double> constraint = new ArrayList<>();
            for (int j = 0; j < objectiveFunction.size(); j++) {
                if(j != i)
                    constraint.add(0.);
                else
                    constraint.add(1.);
            }
            sign.add(LpSolve.LE);
            rightHand.add(1.);
            constraints.add(constraint);
        }

        List<Double> constraint = new ArrayList<>();
        for (int i = 0; i < difference.size(); i++)
            constraint.add(difference.get(i));

        constraint.add(-1.);

        constraints.add(constraint);
        sign.add(LpSolve.GE);
        rightHand.add(0.);

        List<Double> rewardFunction = LpSolveUtil.solve(objectiveFunction, constraints, sign, rightHand);
        for (int i = 1; i <= 15; i++) {
            System.out.println("********** iteration: "+ i);

            valueIteration = new ValueIteration(states, ticTacToe, new TicTacToeCustomTask(0.9, rewardFunction, featureSetClass), random);
            valueIteration.computeOptimalV();
            valueIteration.computeQFactors();
            randomPolicy = valueIteration.computeOptimalPolicy();
            trajectories = IrlUtil.getTrajectories(numTrajectories, new TicTacToeState(), randomPolicy, new TicTacToeTask(0.9), random);
            performance = calculatePerformance(trajectories);
            System.out.println(i+",Performance," + performance);
            System.out.println("Distance from optimal policy: " +policyDifferences(randomPolicy, optimalPolicy, states));
            fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, featureSetClass);
            constraint = new ArrayList<>();

            for (int j = 0; j < fe.size(); j++)
                constraint.add(ofe.get(j) - fe.get(j));

            constraint.add(-1.);

            constraints.add(constraint);
            sign.add(LpSolve.GE);
            rightHand.add(0.);

            rewardFunction = LpSolveUtil.solve(objectiveFunction, constraints, sign, rightHand);
        }
    }

    private static void abbeel2004svm() {
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

        List<Double> ofe = IrlUtil.calculateFeatureExpectations(optimalTrajectories, 0.9, featureSetClass);
        double performance = calculatePerformance(optimalTrajectories);
        System.out.println("Optimal_Performance," + performance);
        performance = calculatePerformance(trajectories);
        System.out.println("0,Performance," + performance);


        Dataset instances = new DefaultDataset();

        double[] valuesOfe = new double[ofe.size()];
        for (int i = 0; i < ofe.size(); i++)
            valuesOfe[i] = ofe.get(i);

        Instance instance = new DenseInstance(valuesOfe);
        instance.setClassValue(1);
        instances.add(instance);

        List<Double> rewardFunction = new ArrayList<>();
        for (int i = 0; i < ofe.size(); i++)
            rewardFunction.add(0.);

        for (int i = 0; i < 30; i++) {
            performance = calculatePerformance(trajectories);
            System.out.println("0,Performance," + performance);
            List<Double> fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, featureSetClass);
            double[] valuesFe = new double[fe.size()];
            for (int j = 0; j < fe.size(); j++)
                valuesFe[j] = fe.get(j);

            instance.setClassValue(0);
            instance = new DenseInstance(valuesFe);
            instances.add(instance);


            LibSVM svm = new LibSVM();
            try {

                svm_parameter par = svm.getParameters();
                par.kernel_type = svm_parameter.LINEAR;//set kernel to linear
                svm.setParameters(par);
                svm.buildClassifier(instances);
                System.out.println("******* weights *******");
                rewardFunction = new ArrayList<>();
                double[] weights = svm.getWeights();
                for (int j = 0; j < weights.length; j++)
                    rewardFunction.add(weights[j]);

                rewardFunction = UtilAmpi.normalize(rewardFunction);

                for (int j = 0; j < rewardFunction.size(); j++)
                    System.out.println(featureSetClass.featureNames().get(j)+":"+ rewardFunction.get(j));

                System.out.println("*************");

            } catch (Exception e) {
                e.printStackTrace();
            }

            valueIteration = new ValueIteration(states, ticTacToe, new TicTacToeCustomTask(0.9, rewardFunction, featureSetClass), random);
            valueIteration.computeOptimalV();
            valueIteration.computeQFactors();
            randomPolicy = valueIteration.computeOptimalPolicy();
            trajectories = IrlUtil.getTrajectories(numTrajectories, new TicTacToeState(), randomPolicy, new TicTacToeCustomTask(0.9, rewardFunction, featureSetClass), random);
        }
    }


    private static void ng2000() {
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

        List<Double> fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, featureSetClass);
        List<Double> ofe = IrlUtil.calculateFeatureExpectations(optimalTrajectories, 0.9, featureSetClass);

        double performance = calculatePerformance(optimalTrajectories);
        System.out.println("Optimal Performance: " + performance);
        performance = calculatePerformance(trajectories);
        System.out.println("Performance: " + performance);

        List<Double> difference = new ArrayList<>();
        for (int i = 0; i < fe.size(); i++) {
            difference.add(ofe.get(i) - fe.get(i));
        }

        List sign = new ArrayList<>();
        List<Double> rightHand = new ArrayList<>();
        List<Double> objectiveFunction = new ArrayList<>();
        for (int i = 0; i < difference.size(); i++)
            objectiveFunction.add(difference.get(i));

        List<List<Double>> constraints = new ArrayList<>();
        for (int i = 0; i < objectiveFunction.size(); i++) {
            List<Double> constraint = new ArrayList<>();
            for (int j = 0; j < objectiveFunction.size(); j++) {
                if(j != i)
                    constraint.add(0.);
                else
                    constraint.add(1.);
            }
            sign.add(LpSolve.LE);
            rightHand.add(1.);
            constraints.add(constraint);
        }

        List<Double> rewardFunction = LpSolveUtil.solve(objectiveFunction, constraints, sign, rightHand);
        for (int i = 0; i < 30; i++) {
            valueIteration = new ValueIteration(states, ticTacToe, new TicTacToeCustomTask(0.9, rewardFunction, featureSetClass), random);
            valueIteration.computeOptimalV();
            valueIteration.computeQFactors();
            randomPolicy = valueIteration.computeOptimalPolicy();

            trajectories = IrlUtil.getTrajectories(100, new TicTacToeState(), randomPolicy, new TicTacToeTask(0.9), random);

            performance = calculatePerformance(trajectories);
            System.out.println("Performance: " + performance);

            fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, featureSetClass);
            objectiveFunction = new ArrayList<>();
            for (int j = 0; j < difference.size(); j++) {
                double dif = ofe.get(j) - fe.get(j);
                if(dif < 1) dif = 2*dif;
                difference.set(j, difference.get(j) + dif);
//                difference.set(j, dif);
                objectiveFunction.add(difference.get(j));
            }
            rewardFunction = LpSolveUtil.solve(objectiveFunction, constraints, sign, rightHand);
        }
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
