import domains.tetris.*;
import org.apache.commons.math3.util.Pair;
import util.Compute;
import util.UtilAmpi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class createState {

    public static void main(String[] args) {

        TetrisFeatureSet featureSet = new TetrisFeatureSet("bcts");

        String stateStr = "I|1:2:3:4:5:6:7:8:9|1:2:3:4:5:6:7:8:9|1:2:4:5:6:7:8:9|1:2:3:4:5:6:7:8|1:2:3:4:6:7:8|0:1:2:3:4:7:8|2:3:4:5:6:7:8:9|2:3:4:5:6:8|2:4:5|4|4|4";
        TetrisState state = TetrisState.parseState(stateStr);
        state.print();
        List<Double> featureValues = featureSet.make(state.features);
        List<String> featureNames = featureSet.featureNames();
        for (int i = 0; i < featureNames.size(); i++) {
            System.out.println(featureNames.get(i) + " :" + featureValues.get(i));
        }


    }


}
