package domains.tetris;


import org.apache.commons.math3.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


public class TetrisLagoudakisThierryFeaturesTest {

//    String stateValuesPath = "src/test/resources/tetris/featureValuesRandom16x10.txt";

    String stateValuesPathLagoudakisThierry = "src/test/resources/tetris/featureValuesRandomLagoudakisThierry16x10.txt";



    List<Pair<TetrisState,TetrisAction>> statesLT;
    List<List<Double>> featureValuesLT;
    List<String> featureNamesLT;

    double epsilon = .0000001; //Error tolerance in feature value

    @Before
    public void setUp(){
        loadListsLagoudakisThierry();
    }




    private void loadListsLagoudakisThierry() {
        try {
            statesLT = new ArrayList<>();
            featureValuesLT = new ArrayList<>();
            featureNamesLT = new ArrayList<>();
            List<String> lines = Files.readAllLines(Paths.get(stateValuesPathLagoudakisThierry));
            String header = lines.get(0);
            String[] featureNamesFile = header.split(",");
            for (int i = 2; i < featureNamesFile.length; i++)
                featureNamesLT.add(featureNamesFile[i]);

            for (int i = 1; i < lines.size(); i++) {
                String[] items = lines.get(i).split(",");
                String[] action = items[1].split("_");
                statesLT.add(new Pair(TetrisState.parseState(items[0]), new TetrisAction(Integer.parseInt(action[0]), Integer.parseInt(action[1]))));
                List<Double> featureValuesState = new ArrayList<>();
                for (int j = 2; j < items.length; j++) {
                    featureValuesState.add(Double.parseDouble(items[j]));
                }
                featureValuesLT.add(featureValuesState);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testStateActionFeaturesLT() {
        for (int i = 0; i < statesLT.size() - 1; i++) {
            Pair<TetrisState, TetrisAction> stateActionPair = statesLT.get(i);
            TetrisState state = stateActionPair.getFirst();
            TetrisAction action = stateActionPair.getSecond();
            System.out.println(state.getStringKey());
            state.nextState(action.col, action.rot, null);
            System.out.println(state.getStringKey());
            System.out.println(action.name());
            if(!state.features.gameOver) {
                List<Double> valuesState = TetrisFeatureSet.make(state.features, "lagoudakisthierry");
                List<Double> valuesStateFile = featureValuesLT.get(i + 1);
                assertEquals(valuesStateFile.size(), valuesState.size());
                for (int featureNumber = 0; featureNumber < valuesState.size(); featureNumber++) {
                    if(featureNumber != 10) //ROW TRANSITIONS
                        assertTrue("Error in feature "+featureNumber+" :" + featureNamesLT.get(featureNumber) + ": expected: " + valuesStateFile.get(featureNumber) + ", found: " + valuesState.get(featureNumber) + "\n state: " + state.getStringKey(), Math.abs(valuesState.get(featureNumber) - valuesStateFile.get(featureNumber)) < epsilon);
                }
            }
        }
    }

}
