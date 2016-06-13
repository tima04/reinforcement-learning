package domains.tetris.helpers;




import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ApproximateDominanceSpec {


    public static Pair<List<Integer>, List<Integer>> get(double c){
        List<Integer> worseIn = new ArrayList<>();
        List<Integer> betterIn = new ArrayList<>();
            if(c == 0.95){
                betterIn.add(3); worseIn.add(1);
                betterIn.add(4); worseIn.add(1);
                betterIn.add(5); worseIn.add(1);
                betterIn.add(5); worseIn.add(2);
                betterIn.add(6); worseIn.add(1);
                betterIn.add(6); worseIn.add(2);
                betterIn.add(7); worseIn.add(1);
            }else if(c== 0.98){
                betterIn.add(4); worseIn.add(1);
                betterIn.add(5); worseIn.add(1);
                betterIn.add(6); worseIn.add(1);
                betterIn.add(6); worseIn.add(2);
                betterIn.add(7); worseIn.add(1);
            }else if(c== 0.99){
                betterIn.add(5); worseIn.add(1);
                betterIn.add(6); worseIn.add(1);
                betterIn.add(6); worseIn.add(2);
                betterIn.add(7); worseIn.add(1);
            }

        assert !betterIn.isEmpty();
        return new Pair(betterIn, worseIn);

    }
}
