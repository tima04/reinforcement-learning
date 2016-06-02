package analysis;

import domains.tetris.TetrisAction;
import domains.tetris.TetrisState;
import org.apache.commons.math3.util.Pair;
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
//  Make sure board is 16 by 10

//    static String path = "src/main/resources/tetris/rawGames/random/";
//    static String suffix = "Random";
//    static int numSamples = 200000;
////  Make sure board is 16 by 10

//    static String path = "src/main/resources/tetris/rawGames/random_board/";
//    static String suffix = "RandomBoard";
//    static int numSamples = 200000;
////  Make sure board is 16 by 10

//    static String path = "src/main/resources/tetris/rawGames/dom/";
//    static String suffix = "Dom";
//    static int numSamples = 200000;
//////  Make sure board is 16 by 10

//    static String path = "src/main/resources/tetris/rawGames/cum/";
//    static String suffix = "Cumdom";
//    static int numSamples = 200000;
////  Make sure board is 16 by 10

//    static String path = "src/main/resources/tetris/rawGames/people/";
//    static String suffix = "AllPeople";
//    static int numSamples = 200000;
////Make sure board is 20 by 10

    static String outpath = "src/main/resources/tetris/data/";

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
        dominance.startReport(outpath +"dom" + suffix + ".txt");
        analysisList.add(dominance);

        //CUMULATIVE DOMINANCE
        Analysis cumdominance = new CumDominanceAnalysis();
        cumdominance.startReport(outpath +"cumdom" + suffix + ".txt");
        analysisList.add(cumdominance);
//
//        //NONCOMPENSATORINESS
//        Analysis noncompensatoriness = new NoncompensatorinessAnalysis();
//        noncompensatoriness.startReport(outpath +"noncom" + suffix + ".txt");
//        analysisList.add(noncompensatoriness);
//
//        //MULTIPLE CUMULATIVE DOMINANCE
//        Analysis multicumulativeDominance = new MultiDominanceAnalysis(MultiDominanceAnalysis.CUMDOM);
//        multicumulativeDominance.startReport(outpath +"multicumdom" + suffix + ".txt");
//        analysisList.add(multicumulativeDominance);

//        //MULTIPLE DOMINANCE
//        Analysis multiDominance = new MultiDominanceAnalysis(MultiDominanceAnalysis.DOM);
//        multiDominance.startReport(outpath +"multidom" + suffix + ".txt");
//        analysisList.add(multiDominance);

//        //Approximate Dominance
        Analysis appdominance = new ApproximateDominanceAnalysis(4, 1);
        appdominance.startReport(outpath +"appdom" + suffix + ".txt");
        analysisList.add(appdominance);

        //        //Approximate Dominance
        Analysis appcumdominance = new ApproximateCumDominanceAnalysis(4, 1);
        appcumdominance.startReport(outpath +"appcumdom" + suffix + ".txt");
        analysisList.add(appcumdominance);

        return analysisList;
    }

    static List<String> readLinesAndSample(File file) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(file.getPath()));
        if(lines.size() <= numSamples)
            return lines;

        return ReservoirSample.sample(lines, numSamples, random);
    }


    static Pair<TetrisState, TetrisAction> parseLine(String line){
        String[] splittedLine = line.split(",");
        TetrisState state = TetrisState.parseState(splittedLine[0]);
        TetrisAction action = new TetrisAction(0, 0);
        if(splittedLine[1].split("_").length == 2) { //SECOND PART OF LINE IS ACTION
            String[] splittedAction = splittedLine[1].split("_");
            action = new TetrisAction(Integer.parseInt(splittedAction[0]), Integer.parseInt(splittedAction[1]));
        }
        return new Pair(state, action);
    }

    private static void closeAnalysis(List<Analysis> analysisList) {
        for (Analysis analysis : analysisList)
            analysis.finishReport();
    }
}
