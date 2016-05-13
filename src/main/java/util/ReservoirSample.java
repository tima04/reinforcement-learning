package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReservoirSample {


    public static <T> List<T> sample(List<T> universe, int samples, Random random) {
        assert universe.size() > samples;
        List<T> reservoir = new ArrayList<T>(samples);
        T prev;
        int universeIndex = 0;
        for (int i = 0; i < samples; i++) {
            prev = universe.get(universeIndex);
            reservoir.add(prev);
            universeIndex++;
        }
        for (int i = universeIndex+1; i < universe.size(); i++) {
            int decreasingChance = random.nextInt(i);
            if(decreasingChance < samples){
                prev = universe.get(i);
                reservoir.set(decreasingChance, prev);
            }
        }
        return reservoir;
    }
}
