package analysis;


import domains.tetris.TetrisAction;
import domains.tetris.TetrisState;
import javafx.util.Pair;
import util.ReservoirSample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class analyzeTetris {

    static String path = "src/main/resources/tetris/rawGames/bcts/";
    static String suffix = "Bcts";
    static int numSamples = 10000;

//    static String path = "src/main/resources/tetris/rawGames/random/";
//    static String suffix = "Random";
//    static int numSamples = 200000;

    static String outpath = "src/main/resources/tetris/dominance/";

    static Random random = new Random(1);

    public static void main(String[] arg){
        File dir = new File(path);
        List<Analysis> analysisList = initAnalysis(suffix);
        for (File file : dir.listFiles()) {
            if(!file.getName().equals(".DS_Store")) {
                try {
                    List<String> sampledLines = readLinesAndSample(file);
                    for (String sampledLine : sampledLines) {
                        Pair<TetrisState, TetrisAction> stateActionPair = parseLine(sampledLine);
                        for (Analysis analysis : analysisList)
                            analysis.executeAndWriteLineToReport(stateActionPair.getKey(), stateActionPair.getValue());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        closeAnalysis(analysisList);
    }



    private static List<Analysis> initAnalysis(String suffix) {
        List<Analysis> analysisList = new ArrayList<>();

        //DOMINANCE
        Analysis dominance = new DominanceAnalysis();
        dominance.startReport(outpath +"dominance" + suffix + ".txt");
        analysisList.add(dominance);

        //CUMULATIVE DOMINANCE
        Analysis cumdominance = new CumDominanceAnalysis();
        cumdominance.startReport(outpath +"cumdominance" + suffix + ".txt");
        analysisList.add(cumdominance);

        //NONCOMPENSATORINESS
        Analysis noncompensatoriness = new NoncompensatorinessAnalysis();
        noncompensatoriness.startReport(outpath +"noncompensation" + suffix + ".txt");
        analysisList.add(noncompensatoriness);

        return analysisList;
    }

    static List<String> readLinesAndSample(File file) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(file.getPath()));
        return ReservoirSample.sample(lines, numSamples, random);
    }


    static Pair<TetrisState, TetrisAction> parseLine(String line){
        String[] splittedLine = line.split(",");
        TetrisState state = TetrisState.parseState(splittedLine[0]);
        String[] splittedAction = splittedLine[1].split("_");
        TetrisAction action = new TetrisAction(Integer.parseInt(splittedAction[0]), Integer.parseInt(splittedAction[1]));
        return new Pair(state, action);
    }

    private static void closeAnalysis(List<Analysis> analysisList) {
        for (Analysis analysis : analysisList)
            analysis.finishReport();
    }
}
