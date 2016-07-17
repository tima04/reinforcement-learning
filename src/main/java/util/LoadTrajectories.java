package util;

import domains.Action;
import domains.tetris.TetrisAction;
import domains.tetris.TetrisState;
import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static analysis.analyzeTetris.parseLine;

/**
 * Created by simonalgorta on 13/07/16.
 */
public class LoadTrajectories {


    public static List<List<String>> loadData(String dirPath) throws IOException {
        List<List<String>> lines = new ArrayList<>();
        File dir = new File(dirPath);
        int id = 0;
        for (File file : dir.listFiles()) {
            if (!file.getName().equals(".DS_Store")) {
                lines.add(Files.lines(Paths.get(file.getPath()), StandardCharsets.ISO_8859_1).collect(Collectors.toList()));
                id++;
            }
        }
        return lines;
    }

    public static List<List<Pair<TetrisState, TetrisAction>>> parse(List<List<String>> lines) {
        int gameId = 0;
        int clearedLines = 0;
        int actions = 0;
        List<List<Pair<TetrisState, TetrisAction>>> trajectories = new ArrayList<>();
//        System.out.println("actions,score");
        for(List<String> game: lines) {
            trajectories.add(new ArrayList<>());
            Pair<TetrisState, TetrisAction> lastState = null;
            for (String line : game) {
                if(!line.contains("#")) {
                    Pair<TetrisState, TetrisAction> stateActionPair = parseLine(line);
                    if (lastState != null && stateActionPair != null) {
                        Action action = getAppropiateAction(lastState.getFirst(), stateActionPair.getFirst());
                        assert action != null;
                        Pair<TetrisState, TetrisAction> validStateActionPair = new Pair(lastState.getFirst(), action);
                        clearedLines += validStateActionPair.getFirst().features.nClearedLines;
                        actions++;
                        trajectories.get(gameId).add(validStateActionPair);
                        TetrisState lastStateFull = lastState.getFirst().copy();
                        lastStateFull.nextState(action, stateActionPair.getFirst().piece());
                        lastState = new Pair(lastStateFull, null);
//                    lastState.getFirst().print();
                    } else {
                        lastState = stateActionPair;//first stateaction pair
                    }
                }
            }
//            System.out.println(actions +","+ clearedLines);
            clearedLines = 0;
            actions = 0;
            gameId++;
        }
        return trajectories;
    }


    static Action getAppropiateAction(TetrisState initState, TetrisState endState){
        for (Action action : initState.getActions()) {
            TetrisState nextState = initState.copy();
            nextState.nextState(action, new Random());
            if(nextState.boardEquals(endState.board()))
                return action;
        }
        return null;
    }

}
