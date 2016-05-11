package domains.tetris;


import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.util.Pair;

import static junit.framework.TestCase.assertTrue;


public class TetrisTest {

    String path = "src/test/resources/tetris/featuresValuesBcts16x10.txt";
    List<Pair<Tetris,TetrisAction>> states;
    List<List<Double>> featureValues;
    List<String> featureNames;

    double epsilon = .0000001; //Error tolerance in feature value

    @Before
    public void setUp(){
         loadLists();
    }

    private void loadLists() {
        try {
            states = new ArrayList<>();
            featureValues = new ArrayList<>();
            featureNames = new ArrayList<>();
            List<String> lines = Files.readAllLines(Paths.get(path));
            String header = lines.get(0);
            String[] featureNamesFile = header.split(",");
            for (int i = 2; i < featureNamesFile.length; i++)
                featureNames.add(featureNamesFile[i]);

            for (int i = 1; i < lines.size(); i++) {
                String[] items = lines.get(i).split(",");
                String[] action = items[1].split("_");
                states.add(new Pair(Tetris.parseState(items[0]), new TetrisAction(Integer.parseInt(action[0]), Integer.parseInt(action[1]))));
                List<Double> featureValuesState = new ArrayList<>();
                for (int j = 2; j < items.length; j++) {
                    featureValuesState.add(Double.parseDouble(items[j]));
                }
                featureValues.add(featureValuesState);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRowTransitions(){
        testFeature(2);
    }

    @Test
    public void testColTransitions(){
        testFeature(3);
    }

    @Test
    public void testHoles(){
        testFeature(4);
    }

    @Test
    public void testCumWells(){
        testFeature(5);
    }

    @Test
    public void testHolesDepth(){
        testFeature(6);
    }

    @Test
    public void testRowsWithHoles(){
        testFeature(7);
    }

    @Test
    public void testPatternDiversity(){
        testFeature(8);
    }

    private void testFeature(int featureNumber) {
        for (int i = 0; i < states.size(); i++) {
            Pair<Tetris, TetrisAction> stateActionPair = states.get(i);
            Tetris state = stateActionPair.getFirst();
//            TetrisAction action = stateActionPair.getSecond();
//            state.nextState(action);
            List<Double> valuesState = FeatureSet.make(state.features, "thierry");
            List<Double> valuesStateFile = featureValues.get(i);
            System.out.println(state.getStringKey());
            assertTrue("Error in feature " + featureNames.get(featureNumber)+": expected: "+valuesStateFile.get(featureNumber)+", found: "+valuesState.get(featureNumber) +"\n state: " + state.getStringKey(), Math.abs(valuesState.get(featureNumber) - valuesStateFile.get(featureNumber)) < epsilon);
        }
    }
}
