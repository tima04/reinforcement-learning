package algs.irl.tetris_styles;

import algs.Game;
import algs.rl.Dpi;
import analysis.GeneralReport;
import domains.Action;
import domains.FeatureSet;
import domains.tetris.*;
import models.CustomPolicy;
import models.Policy;
import policy.LinearPick;
import util.UtilAmpi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by simonalgorta on 30/06/16.
 */
public class generateStyle {

    public static void main(String[] arg){
        Random random = new Random();
        long seed = random.nextInt();
//		long seed = 1;

        random = new Random(seed);
        FeatureSet featureSet = new TetrisFeatureSet("thierry");
        Game game = new Game(random, new TetrisTaskLines(1));
        List<Double> initialWeights = new ArrayList<>();
        for (String name: game.getFeatureNames(featureSet))
            initialWeights.add(random.nextGaussian());

        int numIt = 4;
        double gamma = 1;
        int sampleSize = 5000;
        int nrollout = 5;
        TetrisParameters.getInstance().setSize(20,10);

        setOutput("maxwells_"+featureSet.name()+"_"+sampleSize);
        System.out.println("seed:" + seed);

        UtilAmpi.ActionType  actionType = UtilAmpi.ActionType.ANY;
        Dpi dpi = new Dpi(game, featureSet, numIt, sampleSize, nrollout, gamma, 1, initialWeights, actionType, random);

        List<Double> rewardFunction = new ArrayList<>();
        //Maximize Cum Wells (bcts):
        rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(1.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);
        //Maximize Holes (bcts):
//        rewardFunction.add(1.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);

//        Minimize Holes (bcts):
//        rewardFunction.add(-1.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);

        //Maximize Sum Height Diff (lagoudakis):
//        rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(1.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);rewardFunction.add(0.);

        dpi.setTask(new TetrisCustomTask(1., rewardFunction, new TetrisFeatureSet("bcts"), random));
        List<Double> weights = dpi.iterate();
        Policy customPolicy = new CustomPolicy(new LinearPick(weights, featureSet, random), new TetrisTaskLines(1.), random);
        createGameFiles(customPolicy, 1000, "src/main/resources/tetris/rawGames/people_new/fake/min_holes/", random);
    }

    private static void createGameFiles(Policy policy, int numGames, String dirPath, Random random) {
        for (int i = 0; i < numGames; i++) {
            GeneralReport generalReport = new GeneralReport(dirPath+"_"+i);
            TetrisState state = new TetrisState(random);
            while(!state.features.gameOver){
                Action a = policy.pickAction(state);
                generalReport.addLine(state.getStringKey()+","+a.name());
                state.nextState(a, random);
            }
            generalReport.generateNew();
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
			System.setOut(new PrintStream(new File("src/main/resources/irl/tetris/styles/"+fileName)));
//            System.setOut(new PrintStream(new File("scores/dpi/"+fileName)));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
