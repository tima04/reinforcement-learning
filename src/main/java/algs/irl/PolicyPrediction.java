package algs.irl;



import domains.FeatureSet;
import domains.Features;
import domains.State;
import domains.tetris.*;
import models.CustomPolicy;
import models.Policy;
import org.apache.commons.math3.util.Pair;
import policy.*;
import util.LoadTrajectories;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class PolicyPrediction {

    String dirPath;
    int trajectorySize;
    List<List<Pair<TetrisState, TetrisAction>>> trajectoriesConv;

    List<List<Pair<TetrisState, TetrisAction>>> trainingSet;
    List<List<Pair<TetrisState, TetrisAction>>> testSet;

    HashMap<Tetromino, List< List<Pair<TetrisState, TetrisAction>>>> trajectoriesTetromino;
    HashMap<Tetromino, List< List<Pair<TetrisState, TetrisAction>>>> trainingSetTetromino;
    HashMap<Tetromino, List< List<Pair<TetrisState, TetrisAction>>>> testSetTetromino;

    Random random;

    public final static void main(String[] arg) throws IOException {
        TetrisParameters.getInstance().setSize(20, 10);
        String dirPath = "src/main/resources/tetris/rawGames/people_new/Malte";
        System.out.println(dirPath);
//        String dirPath = "src/main/resources/tetris/rawGames/people/";
        int trajectorySize = 3;
        int convOffset = trajectorySize;
        String policyFeatures = "lagoudakisthierrybertsekas";
        String rewardFeatures = "lagoudakisthierrybertsekas";
        String suffix = "";
        String model = "her";
        String[] pieces = "SZLJOIT".split("");
//        String[] pieces = "I".split("");

        if(arg.length > 0){
            dirPath = arg[0];
            trajectorySize = Integer.parseInt(arg[1]);
            policyFeatures = arg[2];
            rewardFeatures = arg[3];
            suffix = arg[4];
            model = arg[5];
            pieces = arg[6].split("");
            System.out.println(dirPath);
            System.out.println(suffix+"_"+model+"_"+policyFeatures+"_"+rewardFeatures+"_"+trajectorySize+"_"+pieces);
            setOutput(suffix+"_"+model+"_"+policyFeatures+"_"+rewardFeatures+"_"+trajectorySize);
            System.out.println(dirPath);
        }
        List<String> piecesList = Arrays.stream(pieces).collect(Collectors.toList());
//        setOutput("lui");
        List<List<String>> lines = LoadTrajectories.loadData(dirPath);
        List<List<Pair<TetrisState, TetrisAction>>> fullGames = LoadTrajectories.parse(lines);
        PolicyPrediction p = new PolicyPrediction(fullGames, trajectorySize, convOffset, policyFeatures, rewardFeatures, piecesList, true);
        suffix+=",";
        for (String s : piecesList)
            suffix += s;

        if(model.equals("lex")){
            p.lex(suffix, policyFeatures, 5, true);
        }else if(model.equals("singlecue")){
            p.lex(suffix, policyFeatures, 1, true);
        }else if(model.equals("irl")){
            p.maxEnt(suffix, policyFeatures, rewardFeatures, true);
        }else if(model.equals("dps")){
            p.dps(suffix, policyFeatures, true);
        }else if(model.equals("singlecue_tally")){
            p.singleCueTallyRest(suffix, true);
        }else if(model.equals("bcts")){
            p.bcts(suffix, true);
        }else if(model.equals("her")){
            p.her(suffix,policyFeatures, true);
        }

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
//			System.setOut(new PrintStream(new File("src/main/resources/irl/tetris/"+fileName)));
            System.setOut(new PrintStream(new File("irl/tetris/"+fileName)));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    PolicyPrediction(List<List<Pair<TetrisState, TetrisAction>>> games, int trajectorySize, int convOffset, String policyFeatureSet, String rewardFeatureSet, List<String> piecesStr, boolean display){
        random = new Random();
        long seed = random.nextInt();
        if(display) {
            System.out.println("Random seed: " + seed);
            System.out.println("Trajectory size: " + trajectorySize);
            System.out.println("Conv Offset: " + convOffset);
            System.out.println("policy feature set: " + policyFeatureSet);
            System.out.println("reward feature set: " + rewardFeatureSet);
            System.out.print("Pieces: ");
            for (String s : piecesStr)
                System.out.print(s);

            System.out.println();
        }
        this.random = new Random(seed);
        this.trajectorySize = trajectorySize;

        trajectoriesConv = chopConv(games, convOffset, piecesStr);
        assignTrainingAndTestSet(trajectoriesConv, trajectoriesConv.size()/2);
    }

    public void lex(String suffix, String policyFeatureSet, int numCues, boolean display){
        TetrisFeatureSet fs = new TetrisFeatureSet(policyFeatureSet);
        Lex lex = new Lex();
        Policy policyLex = lex.fitPolicy(trainingSet, fs, random, numCues);
        System.out.println("***** features");
        System.out.println("Lex: Accuracy individual decisions (fitting): ");
        predictIndividualDecisions(policyLex, trainingSet, ","+suffix+",fitting,0,lex_"+numCues, display);
        System.out.println("Lex: Accuracy individual decisions (prediction): ");
        predictIndividualDecisions(policyLex, testSet, ","+suffix+",prediction,0,lex_"+numCues, display);
        System.out.println("*****");
        System.out.println("Score:");
        EvaluateTetrisAgent.gamesTetris(100, random, policyLex, true);
    }


    public void maxEnt(String suffix, String policyFeatureSet, String rewardFeatureSet, boolean display){
        List<Policy> policies = MaxEntTetris.maxEnt(formatTrainingSet(trainingSet), 15, trainingSet.size(), trajectorySize, new TetrisFeatureSet(policyFeatureSet), new TetrisFeatureSet(rewardFeatureSet));
//            List<Policy> policies = AbbeelTetris.abbeel2004QP(formatTrainingSet(trainingSet), 20, trainingSet.size(), trajectorySize, new TetrisFeatureSet(policyFeatureSet), new TetrisFeatureSet(rewardFeatureSet));
        int irliteration = 0;
        for (Policy policy : policies) {
            System.out.println("IRL (MaxEnt): Accuracy individual decisions (fitting): ");
            predictIndividualDecisions(policy, trainingSet, ","+suffix+",fitting,"+irliteration+",irl_maxent", display);
            System.out.println("IRL (MaxEnt): Accuracy individual decisions (prediction): ");
            predictIndividualDecisions(policy, testSet, ","+suffix+",prediction,"+irliteration+",irl_maxent", display);
            System.out.println("*****");
            irliteration++;
        }
    }

    public void dps(String suffix, String policyFeatureSet, boolean display){
        DirectPolicySearch dps = new DirectPolicySearch(random, new TetrisFeatureSet(policyFeatureSet), new RandomPick(random));
        Policy policyDps = dps.fitPolicy(trainingSet);
        System.out.println("***** features: "+policyFeatureSet);
        System.out.println("Dps: Accuracy individual decisions (fitting): ");
        predictIndividualDecisions(policyDps, trainingSet, ","+suffix+",fitting,0,dps_"+policyFeatureSet, display);
        System.out.println("Dps: Accuracy individual decisions (prediction): ");
        predictIndividualDecisions(policyDps, testSet, ","+suffix+",prediction,0,dps_"+policyFeatureSet, display);
        System.out.println("*****");
        System.out.println("Score:");
        EvaluateTetrisAgent.gamesTetris(100, random, policyDps, true);
    }

    public void her(String suffix, String policyFeatureSet, boolean display){
        TetrisFeatureSet fs = new TetrisFeatureSet(policyFeatureSet);
        SimpleHeuristicSearch dps = new SimpleHeuristicSearch(random, fs);
        Policy policyDps = dps.fitPolicy(trainingSet);
        StackedPick pick = (StackedPick) policyDps.getPick();
//        pick.print(fs.featureNames());
        System.out.println("***** features: "+policyFeatureSet);
        System.out.println("Heuristic: Accuracy individual decisions (fitting): ");
        predictIndividualDecisions(policyDps, trainingSet, ","+suffix+",fitting,0,her_"+policyFeatureSet, display);
        System.out.println("Heuristic: Accuracy individual decisions (prediction): ");
        predictIndividualDecisions(policyDps, testSet, ","+suffix+",prediction,0,her_"+policyFeatureSet, display);
        System.out.println("*****");
        System.out.println("Score:");
        EvaluateTetrisAgent.gamesTetris(100, random, policyDps, true);
    }

    public void bcts(String suffix, boolean display){
        Policy bctsPolicy = new CustomPolicy(new LinearPick(TetrisWeightVector.make("bcts"), new TetrisFeatureSet("bcts"), random), new TetrisTaskLines(1.), random);
        System.out.println("Bcts: Accuracy individual decisions: ");
        predictIndividualDecisions(bctsPolicy, trajectoriesConv, ","+suffix+",prediction,0,bcts", display);
        System.out.println("*****");
    }

    public void singleCue(String suffix, int featureIdx, int sign, boolean display){
        FeatureSet fs = new TetrisFeatureSet("bcts");
        String featureName = fs.featureNames().get(featureIdx);
        Policy singleCuePolicy = new CustomPolicy(new SingleCue(sign, featureIdx, fs, 0, random, new TetrisTaskLines(1.)), new TetrisTaskLines(1.), random);
        System.out.println("Single Cue ("+featureName+"): Accuracy individual decisions (prediction): ");
        predictIndividualDecisions(singleCuePolicy, trajectoriesConv, ","+suffix+",prediction,0,singlecue_"+featureName, display);
        System.out.println("*****");
    }

    public double singleCueTallyRest(String suffix, boolean display){
        int[] weights = new int[]{-1,-1,1};
        Policy singlecueTallyPolicy = new CustomPolicy(new SingleCueTallyRest(weights, 0, new TetrisFeatureSet("people"), random), new TetrisTaskLines(1.), random);
        if(display)System.out.println("Single Cue (Holes) Tally Rest: Accuracy individual decisions (prediction): ");
        double predictiveAccuracy = predictIndividualDecisions(singlecueTallyPolicy, trajectoriesConv, ","+suffix+",prediction,0,singlecuetally", display);
        if(display)System.out.println("***");
        if(display)System.out.println("Score:");
        EvaluateTetrisAgent.gamesTetris(100, random, singlecueTallyPolicy, display);
        return predictiveAccuracy;
    }

    private List<List<Pair<TetrisState, TetrisAction>>> chopConv(List<List<Pair<TetrisState, TetrisAction>>> fullGames, int step, List<String> pieceFilter) {
        int chunkId = 0;
        int stateCount = 0;
        boolean newTrajectoryFlag = true;
        List<List<Pair<TetrisState, TetrisAction>>> trajectories = new ArrayList<>();
        trajectories.add(new ArrayList<>());
        for (List<Pair<TetrisState, TetrisAction>> fullGame : fullGames) {
            for(int lineCount = 0; lineCount < fullGame.size(); lineCount++){
                newTrajectoryFlag = true;
                Pair<TetrisState, TetrisAction> tetrisStateTetrisActionPair = fullGame.get(lineCount);
                if(pieceFilter.contains(tetrisStateTetrisActionPair.getFirst().piece().name())) {
                    trajectories.get(chunkId).add(tetrisStateTetrisActionPair);
                    stateCount++;
                    if (stateCount % trajectorySize == 0) {
                        lineCount -= trajectorySize;
                        lineCount += step;
                        stateCount = 0;
                        chunkId++;
                        trajectories.add(new ArrayList<>());
                        newTrajectoryFlag = false;
                    }
                }
            }
            stateCount = 0;
            if(newTrajectoryFlag) {
                chunkId++;
                trajectories.add(new ArrayList<>());
            }
        }
        trajectories = trajectories.stream().filter(p -> p.size() > 0).collect(Collectors.toList());
        return trajectories;
    }

    private boolean canClearLines(TetrisState first) {
        for (Pair<TetrisAction, TetrisFeatures> tetrisActionTetrisFeaturesPair : first.getActionsFeaturesList()) {
            if(tetrisActionTetrisFeaturesPair.getSecond().nClearedLines > 0)
                return true;
        }
        return false;
    }


    void assignTrainingAndTestSet(List<List<Pair<TetrisState, TetrisAction>>> trajectories, int numTraining){
        Collections.shuffle(trajectories);
        trainingSet = new ArrayList<>();
        testSet = new ArrayList<>();
        for (int i = 0; i < numTraining; i++) {
            trainingSet.add(trajectories.get(i));
        }
        for (int i = numTraining; i < trajectories.size(); i++) {
            testSet.add(trajectories.get(i));
        }
    }

    List<List<Pair<State, Features>>> formatTrainingSet(List<List<Pair<TetrisState, TetrisAction>>> list){
        List<List<Pair<State, Features>>> formattedList = new ArrayList<>();
        int idx = 0;
        for (List<Pair<TetrisState, TetrisAction>> pairs : list) {
            formattedList.add(new ArrayList<>());
            for (Pair<TetrisState, TetrisAction> pair : pairs) {
                formattedList.get(idx).add(new Pair((State)pair.getFirst(), pair.getFirst().features()));
            }
            idx++;
        }
        return formattedList;
    }

    double predictIndividualDecisions(Policy policy, List<List<Pair<TetrisState, TetrisAction>>> dataSet, String prefix, boolean display){
        double total = 0;
        double predictedPunished = 0;
        double predictedTotal = 0;
        for (List<Pair<TetrisState, TetrisAction>> pairs : dataSet) {
            for (Pair<TetrisState, TetrisAction> pair : pairs) {
                    total++;
                    double predictedPoints = policy.isGreedyAction(pair.getFirst(), pair.getSecond());
                    predictedPunished += predictedPoints;
                    predictedTotal += predictedPoints > 0 ? 1 : 0;
//                if(predictedPoints == 0){
//                    System.out.println("Unpredicted Action:");
//                    pair.getFirst().print();
//                    TetrisState picked = pair.getFirst().copy();
//                    picked.nextState(pair.getSecond(), random);
//                    picked.print();
//                    TetrisState predicted = pair.getFirst().copy();
//                    predicted.nextState(policy.pickAction(predicted), random);
//                    predicted.print();
//                }
            }

        }
        predictedTotal = predictedTotal/total;
        predictedPunished = predictedPunished/total;
        if(display)System.out.println(prefix+",predicted_brutto," + predictedTotal);
        if(display)System.out.println(prefix+",predicted_netto," + predictedPunished);
        return predictedPunished;
    }
}

