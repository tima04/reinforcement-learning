package algs.irl;



import algs.Game;
import algs.rl.Dpi;
import algs.rl.ValueIteration;
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

public class AbbeelTetris {

    static Random random = new Random(2);
    static int numTrajectories = 100;
    static int trajectoryLength = 1000;
    static TetrisFeatureSet rewardFunctionFeatureSet = new TetrisFeatureSet("lagoudakisthierry");
    static TetrisFeatureSet policyFeatureSet = new TetrisFeatureSet("bcts");


    public static void main(String[] arg){

        setOutput("bcts");
        TetrisParameters.getInstance().setSize(16, 10);
//        Ng2000();
//        constraintPerPolicy();
        abbeel2004();

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
    private static void constraintPerPolicy() {

        List<Double> weights = TetrisWeightVector.make("bcts");

        Policy optimalPolicy = new CustomPolicy(new LinearPick(weights, policyFeatureSet, random), new TetrisTaskLines(0.9));

        List<Double> randWeights = new ArrayList<>();
        for (int i = 0; i < 8; i++)
           randWeights.add(random.nextGaussian());

        Policy randomPolicy = new CustomPolicy(new LinearPick(randWeights, policyFeatureSet, random), new TetrisTaskLines(0.9));

        System.out.println("getting demonstrated trajectories . . .");
        List<List<Pair<State, Features>>> demonstratedTrajectories = IrlUtil.getTrajectories(numTrajectories, new TetrisState(random), optimalPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);

        System.out.println("calculating feature expectations . . .");
        List<Double> ofe = IrlUtil.calculateFeatureExpectations(demonstratedTrajectories, 0.9, rewardFunctionFeatureSet);

        for (int i = 0; i < 15; i++) {
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
            randomPolicy = getNearOptimalPolicy(rewardFunction);
        }
    }


    private static void abbeel2004() {
        List<Double> weights = TetrisWeightVector.make("bcts");

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


            trajectories = IrlUtil.getTrajectories(numTrajectories, new TetrisState(random), randomPolicy, new TetrisTruncatedTaskLines(0.9, trajectoryLength, random), random);
            fe = IrlUtil.calculateFeatureExpectations(trajectories, 0.9, rewardFunctionFeatureSet);
            constraint = new ArrayList<>();

            for (int j = 0; j < fe.size(); j++)
                constraint.add(ofe.get(j) - fe.get(j));

            constraint.add(-1.);

            constraints.add(constraint);
            sign.add(LpSolve.GE);
            rightHand.add(0.);

            rewardFunction = LpSolveUtil.solve(objectiveFunction, constraints, sign, rightHand);

            List<String> names = rewardFunctionFeatureSet.featureNames();
            System.out.println("Inferred reward function:");
            for (int j = 0; j < rewardFunction.size()-1; j++)
                System.out.println("weight,"+i+","+names.get(j)+","+rewardFunction.get(j));

            System.out.println("");
            randomPolicy = getNearOptimalPolicy(rewardFunction);

        }
    }


    private static Policy getNearOptimalPolicy(List<Double> rewardFunction) {

        List<Double> initWeightsDpi = new ArrayList<>();
        for (String s : policyFeatureSet.featureNames())
            initWeightsDpi.add(0.);

        Task task = new TetrisCustomTask(0.9, rewardFunction, rewardFunctionFeatureSet, random);
        System.out.println("Running DPI");
        Dpi dpi = new Dpi(new Game(random, task),
                        policyFeatureSet,
                        2,
                        5000,
                        5,
                        0.9,
                        initWeightsDpi,
                        UtilAmpi.ActionType.ANY,
                        random);
        dpi.setRounds(10);
        dpi.setTask(task);
        List<Double> weights = dpi.iterate();
        System.out.println("DPI done");
        return new CustomPolicy(new LinearPick(weights, policyFeatureSet, random), new TetrisTaskLines(0.9));
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
