package algs.irl;



import algs.Game;
import algs.rl.Dpi;
import algs.rl.LambdaPI;
import algs.rl.ValueIteration;
import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.functions.PDQuadraticMultivariateRealFunction;
import domains.FeatureSet;
import domains.Features;
import domains.State;
import domains.Task;
import domains.tetris.*;
import domains.tictactoe.TicTacToe;
import domains.tictactoe.TicTacToeCustomTask;
import domains.tictactoe.TicTacToeState;
import domains.tictactoe.TicTacToeTask;
import lpsolve.LpSolve;
import models.CustomPolicy;
import models.Policy;
import org.apache.commons.math3.util.Pair;
import policy.LinearPick;
import util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AbbeelTetris {

    static Random random = new Random(2);
    static int numTrajectories = 1000;
    static int trajectoryLength = 10;
    static TetrisFeatureSet rewardFunctionFeatureSet = new TetrisFeatureSet("thierry");
    static TetrisFeatureSet policyFeatureSet = new TetrisFeatureSet("thierry");


    public static void main(String[] arg){

        setOutput("bcts");
//        Ng2000();
//        constraintPerPolicy();

        List<Double> weights = TetrisWeightVector.make("bcts");
        TetrisParameters.getInstance().setSize(10, 10);
        Policy optimalPolicy = new CustomPolicy(new LinearPick(weights, new TetrisFeatureSet("bcts"), random), new TetrisTaskLines(1.), random);
        System.out.println("getting demonstrated trajectories . . .");
        List<State> initialStates = RolloutUtil.getRolloutSetTetris(new Game(random, new TetrisTaskLines(1.)), numTrajectories, weights, new TetrisFeatureSet("bcts"), UtilAmpi.ActionType.ANY, new TetrisFeatureSet("bcts"), new double[]{}, random).stream().map(p -> (TetrisState)p).collect(Collectors.toList());
        List<List<Pair<State, Features>>> demonstratedTrajectories = IrlUtil.getTrajectories(numTrajectories, initialStates, optimalPolicy, new TetrisTruncatedTaskLines(1., trajectoryLength, random), random);
        abbeel2004(demonstratedTrajectories, 30, numTrajectories, trajectoryLength, policyFeatureSet, rewardFunctionFeatureSet);
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
            System.setOut(new PrintStream(new File("src/main/resources/irl/tetris/abbeel/"+fileName)));
//			System.setOut(new PrintStream(new File("scores/dpi/"+fileName)));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    //Maximize R
    //s.t: ri <= 1 for all i
    // Demonstrated Policy Feature Expectations >= Policy i for i in {0 .. k}
    public static Policy constraintPerPolicy(List<List<Pair<State, Features>>> demonstratedTrajectories, int iterations, int numTrajectories, int trajectoryLength) {

        List<Double> randWeights = new ArrayList<>();
        for (int i = 0; i < 8; i++)
           randWeights.add(random.nextGaussian());

        Policy randomPolicy = new CustomPolicy(new LinearPick(randWeights, policyFeatureSet, random), new TetrisTaskLines(0.9), random);


        System.out.println("calculating feature expectations . . .");
        List<Double> ofe = IrlUtil.calculateFeatureExpectations(demonstratedTrajectories, 0.9, rewardFunctionFeatureSet);

        for (int i = 0; i < iterations; i++) {
            List<List<Pair<State, Features>>> trajectories = IrlUtil.getTrajectories(numTrajectories, new TetrisState(random), randomPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);
            List<Double> fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, rewardFunctionFeatureSet);

            double performance = calculatePerformance(demonstratedTrajectories);
            System.out.println("Optimal Performance: " + performance);
            performance = calculatePerformance(trajectories);
            System.out.println("Performance: " + performance);

            List<Double> difference = new ArrayList<>();
            for (int j = 0; j < fe.size(); j++) {
                difference.add(ofe.get(j) - fe.get(j));
            }
            for (int j = 0; j < fe.size(); j++) {
                difference.add(fe.get(j) - ofe.get(j));
            }
            List sign = new ArrayList<>();
            List<Double> rightHand = new ArrayList<>();
            List<Double> objectiveFunction = new ArrayList<>();
            for (int j = 0; j < difference.size(); j++)
                objectiveFunction.add(-1.);

            List<List<Double>> constraints = new ArrayList<>();
            for (int j = 0; j < objectiveFunction.size(); j++) {
                List<Double> constraint = new ArrayList<>();
                for (int k = 0; k < objectiveFunction.size(); k++) {
                    if (j != i)
                        constraint.add(0.);
                    else
                        constraint.add(1.);
                }
                sign.add(LpSolve.LE);
                rightHand.add(1.);
                constraints.add(constraint);
            }

            List<Double> constraint = new ArrayList<>();
            for (int j = 0; j < difference.size(); j++)
                constraint.add(difference.get(j));

            constraints.add(constraint);
            sign.add(LpSolve.GE);
            rightHand.add(0.); //todo change for proper loss function

            List<Double> rewardFunction = LpSolveUtil.solve(objectiveFunction, constraints, sign, rightHand);
            List<String> names = rewardFunctionFeatureSet.featureNames();
            System.out.println("**************");
            System.out.println("Inferred reward function:");
            for (int j = 0; j < rewardFunction.size(); j++)
                System.out.println("weight,"+i+","+names.get(j)+","+rewardFunction.get(j));

            System.out.println("");
            randomPolicy = getNearOptimalPolicy(trajectoryLength, rewardFunction, rewardFunctionFeatureSet, policyFeatureSet);
        }
        return randomPolicy;
    }


    public static List<Policy> abbeel2004(List<List<Pair<State, Features>>> demonstratedTrajectories, int iterations, int numTrajectories, int trajectoryLength, FeatureSet policyFeatureSet, FeatureSet rewardFunctionFeatureSet) {

        List<Policy> policies = new ArrayList<>();
        List<Double> randWeights = new ArrayList<>();
        for (int i = 0; i < policyFeatureSet.featureNames().size(); i++)
            randWeights.add(random.nextGaussian());


        Policy randomPolicy = new CustomPolicy(new LinearPick(randWeights, policyFeatureSet, random), new TetrisTaskLines(0.9), random);


        System.out.println("calculating feature expectations . . .");
        List<Double> ofe = IrlUtil.calculateFeatureExpectations(demonstratedTrajectories, 1, rewardFunctionFeatureSet);

        List<State> initialStates = demonstratedTrajectories.stream().map(p -> p.get(0).getFirst()).collect(Collectors.toList());
        List<List<Pair<State, Features>>> trajectories = IrlUtil.getTrajectories(numTrajectories, initialStates, randomPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);
        List<Double> fe = IrlUtil.calculateFeatureExpectations(trajectories, 1, rewardFunctionFeatureSet);

        List<Double> difference = new ArrayList<>();
        for (int i = 0; i < fe.size(); i++) {
            difference.add(ofe.get(i) - fe.get(i));
        }
        for (int j = 0; j < fe.size(); j++) {
            difference.add(fe.get(j) - ofe.get(j));
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

        List<Double> rewardFunction;
        for (int i = 1; i <= iterations; i++) {
            System.out.println("********** iteration: "+ i);


            trajectories = IrlUtil.getTrajectories(numTrajectories, initialStates, randomPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);
            fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, rewardFunctionFeatureSet);
            constraint = new ArrayList<>();

            for (int j = 0; j < fe.size(); j++)
                constraint.add(ofe.get(j) - fe.get(j));

            for (int j = 0; j < fe.size(); j++)
                constraint.add(fe.get(j) - ofe.get(j));

            constraint.add(-1.);

            constraints.add(constraint);
            sign.add(LpSolve.GE);
            rightHand.add(0.);

            List<Double> rewardFunctionComplete = LpSolveUtil.solve(objectiveFunction, constraints, sign, rightHand);
            rewardFunction = new ArrayList<>();
            List<String> names = rewardFunctionFeatureSet.featureNames();
            System.out.println("Inferred reward function:");
            for (int j = 0; j < names.size(); j++) {
                System.out.println("weight," + i + "," + names.get(j) + "," + rewardFunctionComplete.get(j) + " - " + rewardFunctionComplete.get(j + names.size()));
                rewardFunction.add(rewardFunctionComplete.get(j) - rewardFunctionComplete.get(j + names.size()));
            }
            System.out.println("");

            randomPolicy = getNearOptimalPolicy(trajectoryLength, rewardFunction, rewardFunctionFeatureSet, policyFeatureSet);
            policies.add(randomPolicy);

        }
        return policies;
    }

    public static List<Policy> abbeel2004QP(List<List<Pair<State, Features>>> demonstratedTrajectories, int iterations, int numTrajectories, int trajectoryLength, FeatureSet policyFeatureSet, FeatureSet rewardFunctionFeatureSet) {


        List<Policy> policies = new ArrayList<>();
        List<Double> randWeights = new ArrayList<>();
        for (int i = 0; i < 8; i++)
            randWeights.add(random.nextGaussian());


        Policy closerPolicy = new CustomPolicy(new LinearPick(randWeights, policyFeatureSet, random), new TetrisTaskLines(0.9), random);


        System.out.println("calculating feature expectations . . .");
        List<Double> ofe = IrlUtil.calculateFeatureExpectations(demonstratedTrajectories, 1, rewardFunctionFeatureSet);

        List<State> initialStates = demonstratedTrajectories.stream().map(p -> p.get(0).getFirst()).collect(Collectors.toList());
        List<List<Pair<State, Features>>> trajectories = IrlUtil.getTrajectories(numTrajectories, initialStates, closerPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);
        List<Double> fe = IrlUtil.calculateFeatureExpectations(trajectories, 1, rewardFunctionFeatureSet);


        //OBJ FUNCTION = max: t
        //CONSTRAINTS:
        // L2 norm of weights <= 1
        // fe - ofe + t <= 0
        double[] objectiveFunctionVector = new double[fe.size() + 1];
        objectiveFunctionVector[fe.size()] = -1.;
        LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(objectiveFunctionVector, 0);

        double[][] pMatrix = new double[fe.size()+1][fe.size()+1];
        double[] pVector = new double[fe.size()+1];
        for (int i = 0; i < fe.size(); i++) {
            for (int j = 0; j < fe.size(); j++) {
                pVector[i] = 1;
                if(i == j)
                    pMatrix[i][j] = 1;
                else
                    pMatrix[i][j] = 0;
            }
        }
        pVector[fe.size()] = 0;

        List<ConvexMultivariateRealFunction> inequalities = new ArrayList<>();
        inequalities.add(new PDQuadraticMultivariateRealFunction(pMatrix, pVector, 1));


        List<Double> rewardFunction;
        for (int i = 1; i <= 15; i++) {
            System.out.println("********** iteration: "+ i);

            trajectories = IrlUtil.getTrajectories(numTrajectories, initialStates, closerPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);
            fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, rewardFunctionFeatureSet);

            double[] constraint = new double[fe.size() + 1];
            for (int j = 0; j < fe.size(); j++)
                constraint[j] =  fe.get(j) - ofe.get(j);

            constraint[fe.size()] = 1.;
            inequalities.add(new LinearMultivariateRealFunction(constraint, 0));

            rewardFunction = QpSolveUtil.solve(objectiveFunction, inequalities);

            System.out.println("******* weights *******");
            for (int j = 0; j < rewardFunction.size() - 1; j++)
                System.out.println("weight,"+i+","+rewardFunctionFeatureSet.featureNames().get(j)+","+rewardFunction.get(j));

            policies.add(closerPolicy);
            closerPolicy = getNearOptimalPolicy(trajectoryLength, rewardFunction, rewardFunctionFeatureSet, policyFeatureSet);
        }

        return policies;
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
                10,
                0.9,
                0.9,
                initWeightsDpi,
                50000,
                UtilAmpi.ActionType.ANY,
                random);
        List<Double> weights = lpi.iterate();
        System.out.println("Lambda PI done");
        return new CustomPolicy(new LinearPick(weights, policyFeatureSet, random), new TetrisTaskLines(0.9), random);
    }


    private static Policy getNearOptimalPolicy(int rolloutLenght, List<Double> rewardFunction, FeatureSet rewardFunctionFeatureSet, FeatureSet policyFeatureSet) {

        List<Double> initWeightsDpi = new ArrayList<>();
        for (String s : policyFeatureSet.featureNames())
            initWeightsDpi.add(0.);

        Task task = new TetrisCustomTask(0.9, rewardFunction, rewardFunctionFeatureSet, random);
        System.out.println("Running DPI");
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
        dpi.setRounds(10);
        dpi.setTask(task);
        List<Double> weights = dpi.iterate();
        System.out.println("DPI done");
        return new CustomPolicy(new LinearPick(weights, policyFeatureSet, random), new TetrisTaskLines(0.9), random);
    }

    private static double calculatePerformance(List<List<Pair<State, Features>>> trajectories) {
        int linesCleared = 0;
        for (List<Pair<State, Features>> trajectory : trajectories) {
            for (Pair<State, Features> stateFeaturesPair : trajectory) {
                linesCleared += ((TetrisFeatures)stateFeaturesPair.getSecond()).nClearedLines;
            }
        }
        return linesCleared/trajectories.size();
    }
}
