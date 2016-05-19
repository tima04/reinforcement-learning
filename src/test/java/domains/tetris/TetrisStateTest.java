package domains.tetris;


import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.util.Pair;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


public class TetrisStateTest {

    String stateValuesPath = "src/test/resources/tetris/featureValuesRandom16x10.txt";
//    String stateValuesPath = "src/test/resources/tetris/featureValuesBcts16x10.txt";


    List<Pair<TetrisState,TetrisAction>> states;
    List<List<Double>> featureValues;
    List<String> featureNames;

    List<Pair<TetrisState,TetrisAction>> statesLT;
    List<List<Double>> featureValuesLT;
    List<String> featureNamesLT;

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
            List<String> lines = Files.readAllLines(Paths.get(stateValuesPath));
            String header = lines.get(0);
            String[] featureNamesFile = header.split(",");
            for (int i = 2; i < featureNamesFile.length; i++)
                featureNames.add(featureNamesFile[i]);

            for (int i = 1; i < lines.size(); i++) {
                String[] items = lines.get(i).split(",");
                String[] action = items[1].split("_");
                states.add(new Pair(TetrisState.parseState(items[0]), new TetrisAction(Integer.parseInt(action[0]), Integer.parseInt(action[1]))));
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

    @Test
    public void testLandingHeightAfterAction(){
        testStateActionFeature(0, "thierry");
    }


    @Test
    public void testErodedCellsAfterAction(){
        testStateActionFeature(1, "thierry");
    }

    @Test
    public void testRowTransitionsAfterAction(){
        testStateActionFeature(2, "thierry");
    }

    @Test
    public void testColTransitionsAfterAction(){
        testStateActionFeature(3, "thierry");
    }

    @Test
    public void testHolesAfterAction(){
        testStateActionFeature(4, "thierry");
    }

    @Test
    public void testCumWellsAfterAction(){
        testStateActionFeature(5, "thierry");
    }

    @Test
    public void testHolesDepthAfterAction(){
        testStateActionFeature(6, "thierry");
    }

    @Test
    public void testRowsWithHolesAfterAction(){
        testStateActionFeature(7, "thierry");
    }


    @Test
    public void testNextState() {
        for (int i = 0; i < states.size() - 1; i++) {
            Pair<TetrisState, TetrisAction> stateActionPair = states.get(i);
            TetrisState state = stateActionPair.getFirst();
            TetrisAction action = stateActionPair.getSecond();
            state.nextState(action);
            state.piece = states.get(i+1).getFirst().piece;
            if (!state.features.gameOver) {
                System.out.println(state.getStringKey());
                assertEquals("Expected: " + states.get(i + 1).getFirst().getStringKey() + " got:" + state.getStringKey(), states.get(i + 1).getFirst().getStringKey(), state.getStringKey());
            }
        }
    }



    private void testFeature(int featureNumber) {
        for (int i = 0; i < states.size(); i++) {
            Pair<TetrisState, TetrisAction> stateActionPair = states.get(i);
            TetrisState state = stateActionPair.getFirst();
            List<Double> valuesState = TetrisFeatureSet.make(state.features, "thierry");
            List<Double> valuesStateFile = featureValues.get(i);
            System.out.println(state.getStringKey());
            assertTrue("Error in feature " + featureNames.get(featureNumber)+": expected: "+valuesStateFile.get(featureNumber)+", found: "+valuesState.get(featureNumber) +"\n state: " + state.getStringKey(), Math.abs(valuesState.get(featureNumber) - valuesStateFile.get(featureNumber)) < epsilon);
        }
    }

    private void testStateActionFeature(int featureNumber, String featureSet) {
        for (int i = 0; i < states.size() - 1; i++) {
            Pair<TetrisState, TetrisAction> stateActionPair = states.get(i);
            TetrisState state = stateActionPair.getFirst();
            TetrisAction action = stateActionPair.getSecond();
            state.nextState(action);
            if(!state.features.gameOver) {
                List<Double> valuesState = TetrisFeatureSet.make(state.features, featureSet);
                List<Double> valuesStateFile = featureValues.get(i + 1);
                System.out.println(state.getStringKey());
                assertTrue("Error in feature " + featureNames.get(featureNumber) + ": expected: " + valuesStateFile.get(featureNumber) + ", found: " + valuesState.get(featureNumber) + "\n state: " + state.getStringKey(), Math.abs(valuesState.get(featureNumber) - valuesStateFile.get(featureNumber)) < epsilon);
            }
        }
    }


}
