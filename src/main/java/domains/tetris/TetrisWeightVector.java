package domains.tetris;

import java.util.ArrayList;
import java.util.List;

public class TetrisWeightVector {

    public static List<Double> make(String name){
        List<Double> weights = new ArrayList<>();
        if(name.equals("bcts")){
            weights.add(-13.08);
            weights.add(-19.77);
            weights.add(-9.22);
            weights.add(-10.49);
            weights.add( 6.60);
            weights.add(-12.63);
            weights.add(-24.04);
            weights.add(-1.61);
        }
        return weights;
    }

}
