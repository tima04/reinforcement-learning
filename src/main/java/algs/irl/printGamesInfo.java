package algs.irl;


import domains.Action;
import domains.Features;
import domains.State;
import domains.tetris.*;
import org.apache.commons.math3.util.Pair;
import policy.*;
import util.LoadTrajectories;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static analysis.analyzeTetris.parseLine;

public class printGamesInfo {

    List<List<Pair<TetrisState, TetrisAction>>> trajectories;

    static Random random = new Random();

    static String pathPrefix = "src/main/resources/tetris/rawGames/people_new/";
    static String[] dataDirs = new String[]{
            "Juana",
            "Kalypso",
            "Lui",
            "Malte",
            "Marcus",
            "Simon",
            "Santiago",
            "Fergal"
    };

//    static String[] dataDirs = new String[]{
//            "kalypso1",
//            "kalypso2",
//            "kalypso3"
//    };

    public final static void main(String[] arg){
        TetrisParameters.getInstance().setSize(20, 10);
        setOutput("games");
        System.out.println("person,actions,score");

        for (String dataDir : dataDirs) {
            printGamesInfo(pathPrefix+dataDir, dataDir);
        }
    }

    private static void setOutput(String fileName) {
        Date yourDate = new Date();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-hh:mm");
        String date = DATE_FORMAT.format(yourDate);
        //redirecting output stream
        fileName += ".csv";
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

    private static void printGamesInfo(String dirpath, String person){
        List<List<String>> lines = null;

        String[] pieces = "SZLJOIT".split("");
        List<String> piecesList = Arrays.stream(pieces).collect(Collectors.toList());

        try {
            lines = LoadTrajectories.loadData(dirpath);
            List<List<Pair<TetrisState, TetrisAction>>> trajectories = LoadTrajectories.parse(lines);
            for (List<Pair<TetrisState, TetrisAction>> trajectory : trajectories) {
                int score = trajectory.stream().map(p -> p.getFirst().features.nClearedLines).reduce(0, Integer::sum);
                List<List<Pair<TetrisState, TetrisAction>>> game = new ArrayList<>();
                game.add(trajectory);
//                PolicyPrediction pp = new PolicyPrediction(game, 3, 3, "bcts", "bcts", piecesList, false);
                System.out.println(person+","+trajectory.size()+","+score);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

