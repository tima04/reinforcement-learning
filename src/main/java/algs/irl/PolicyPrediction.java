package algs.irl;


import analysis.Analysis;
import analysis.analyzeTetris;
import domains.Action;
import domains.Features;
import domains.State;
import domains.tetris.TetrisAction;
import domains.tetris.TetrisParameters;
import domains.tetris.TetrisState;
import models.Policy;
import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static analysis.analyzeTetris.*;

public class PolicyPrediction {

    String dirPath;
    int trajectorySize;
    List<List<Pair<TetrisState, TetrisAction>>> trajectories;

    List<List<Pair<TetrisState, TetrisAction>>> trainingSet;
    List<List<Pair<TetrisState, TetrisAction>>> testSet;
    Random random = new Random(1);

    public final static void main(String[] arg){
        TetrisParameters.getInstance().setSize(20, 10);
        String dirPath = "src/main/resources/tetris/rawGames/people new/Juana/";
        int trajectorySize = 50;
        PolicyPrediction p = new PolicyPrediction(dirPath, trajectorySize);

    }


    PolicyPrediction(String dirpath, int trajectorySize){
        try {
            this.dirPath = dirpath;
            this.trajectorySize = trajectorySize;
            List<String> lines = loadData();
            trajectories = parseAndChop(lines);
            assignTrainingAndTestSet(15);
            Policy policy = MaxEntTetris.maxEnt(formatTrainingSet(trainingSet), 3, trainingSet.size(), trajectorySize);
            System.out.println("Accuracy individual predictions: "+ predictIndividualDecisions(policy));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    List<String> loadData() throws IOException {
        List<String> lines = new ArrayList<>();
        File dir = new File(dirPath);
        for (File file : dir.listFiles()) {
            if (!file.getName().equals(".DS_Store")) {
                lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
                System.out.println(lines.size() +" lines loaded");
            }
        }
        return lines;
    }

    private List<List<Pair<TetrisState, TetrisAction>>> parseAndChop(List<String> lines) {
        int chunkIdx = 0;
        int lineIdx = 1;
        List<List<Pair<TetrisState, TetrisAction>>> trajectories = new ArrayList<>();
        trajectories.add(new ArrayList<>());
        Pair<TetrisState, TetrisAction> lastState = null;
        for (String line : lines) {
            Pair<TetrisState, TetrisAction> stateActionPair = parseLine(line);
            if(lastState != null) {
                Action action = getAppropiateAction(lastState.getFirst(), stateActionPair.getFirst());
                assert action != null;
                Pair<TetrisState, TetrisAction> validStateActionPair = new Pair(lastState.getFirst(), action);
                trajectories.get(chunkIdx).add(validStateActionPair);
                if(lineIdx % trajectorySize == 0){
                    trajectories.add(new ArrayList<>());
                    chunkIdx++;
                }
                lineIdx++;

                TetrisState lastStateFull = lastState.getFirst().copy();
                lastStateFull.nextState(action, stateActionPair.getFirst().piece());
                lastState = new Pair(lastStateFull, null);
            }else {
                lastState = stateActionPair;//first stateaction pair
            }
        }
        return trajectories;
    }


    Action getAppropiateAction(TetrisState initState, TetrisState endState){
        for (Action action : initState.getActions()) {
            TetrisState nextState = initState.copy();
            nextState.nextState(action, random);
            if(nextState.boardEquals(endState.board()))
                return action;
        }
        return null;
    }

    void assignTrainingAndTestSet(int numTraining){
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

    double predictIndividualDecisions(Policy policy){
        double total = 0;
        double predicted = 0;
        for (List<Pair<TetrisState, TetrisAction>> pairs : testSet) {
            for (Pair<TetrisState, TetrisAction> pair : pairs) {
                total++;
                if(policy.pickAction(pair.getFirst()).equals(pair.getSecond()))
                    predicted++;
            }

        }

        return predicted/total;
    }
}
