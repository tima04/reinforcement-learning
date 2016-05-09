package algorta.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Compute {

    private static final Double	DELTA = Double.MIN_VALUE;

    private Compute() {}  //Static class, can not be instantiated.

    // Returns index of (a randomly-selected) maximum in values.
    public static int indexOfARandomlySelectedMaximum(double[] values, Random random){
        int[] eligibles = indicesOfMax(values);
        return eligibles[randomIndex(eligibles.length, random)];
    }

    // Returns indices of all maximums in values
    public static int[] indicesOfMax(double[] values){
        // find out
        double max = max(values);
        List<Integer> temp = new ArrayList<Integer>(values.length);
        for (int i = 0; i < values.length; i++)
            if (values[i] + DELTA >= max)
                temp.add(i);

        // convert to an array
        int[] out = new int[temp.size()];
        for (int i = 0; i< out.length; i++)
            out[i] = temp.get(i).intValue();
        return out;
    }

    // Returns indices of all maximums in values
    public static int[] indicesOfMax(int[] values){
        // find out
        double max = max(values);
        List<Integer> temp = new ArrayList<Integer>(values.length);
        for (int i = 0; i < values.length; i++)
            if (values[i] + DELTA >= max)
                temp.add(i);

        // convert to an array
        int[] out = new int[temp.size()];
        for (int i = 0; i< out.length; i++)
            out[i] = temp.get(i).intValue();
        return out;
    }

    // Returns maximum of values
    public static double max(double[] values){
        double max = values[0];
        for (int i = 1; i < values.length; i++)
            if (values[i] > max)
                max = values[i];
        return max;
    }

    // Returns maximum of values
    public static double max(int[] values){
        double max = values[0];
        for (int i = 1; i < values.length; i++)
            if (values[i] > max)
                max = values[i];
        return max;
    }

    // Returns a random number between 0 and n-1
    public static int randomIndex(int n, Random random) {
        return random.nextInt(n);
    }


    public static int[] orderedIndices(double[] weights) {
        double[] weightsCopy = weights.clone();
        int[] indices = new int[weightsCopy.length];
        int j = 0;
        while(j < weightsCopy.length) {
            int[] indicesOfMax = indicesOfMax(weightsCopy);
            for (int i = 0; i < indicesOfMax.length; i++) {
                indices[j] = indicesOfMax[i];
                weightsCopy[indicesOfMax[i]] = -Double.MAX_VALUE;
                j++;
            }
        }
        return indices;
    }

    public static int[] orderedIndices(int[] weights) {
        int[] weightsCopy = weights.clone();
        int[] indices = new int[weightsCopy.length];
        int j = 0;
        while(j < weightsCopy.length) {
            int[] indicesOfMax = indicesOfMax(weightsCopy);
            for (int i = 0; i < indicesOfMax.length; i++) {
                indices[j] = indicesOfMax[i];
                weightsCopy[indicesOfMax[i]] = -Integer.MAX_VALUE;
                j++;
            }
        }
        return indices;
    }

    // Returns vector divided by maximum value
    public static double[] vectorDividedByMax(double[] values){
        double vector[] = new double[values.length];
        int[] maxIndex = indicesOfMax(values);
        for (int i = 0; i < values.length; i++) {
            vector[i] = values[i]/values[maxIndex[0]];
        }
        return vector;
    }

    public static int[] concat(int[] first, int[] second) {
        int[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
