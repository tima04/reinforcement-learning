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
        }else if(name.equals("DT10")){
            weights.add(-2.18);
            weights.add(2.42);
            weights.add(-2.17);
            weights.add(-3.31);
            weights.add( 0.95);
            weights.add(-2.22);
            weights.add(-0.81);
            weights.add(-9.65);
            weights.add(1.27);
        }
        return weights;
    }

}
