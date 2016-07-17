package util;
import java.util.ArrayList;
import java.util.List;

public class DistinctCounter {

    public static int howManyDistinct(double[][] objects){
        List<double[]> distinctFeatureSets = new ArrayList<>();
        for (int i = 0; i < objects.length; i++) {
                if(!containsEqual(distinctFeatureSets, objects[i]))
                    distinctFeatureSets.add(objects[i]);
        }
        return distinctFeatureSets.size();
    }

    public static int howManyDistinct(List<double[]> objects){
        List<double[]> distinctFeatureSets = new ArrayList<double[]>();
        for (int i = 0; i < objects.size(); i++) {
            if(!containsEqual(distinctFeatureSets, objects.get(i)))
                distinctFeatureSets.add(objects.get(i));
        }
        return distinctFeatureSets.size();
    }

    public static int howManyDistinct(double[][] objects, int[] indices){
        List<Integer> indicesList = new ArrayList<>();
        for (int i = 0; i < indices.length; i++)
            indicesList.add(indices[i]);

        return howManyDistinct(objects, indicesList);
    }

    public static int howManyDistinct(double[][] objects, List<Integer> indicesList){
        List<double[]> distinctFeatureSets = new ArrayList<>();
        for (int i = 0; i < objects.length; i++) {
            if(!containsEqual(distinctFeatureSets, objects[i]) && indicesList.contains(new Integer(i)))
                distinctFeatureSets.add(objects[i]);
        }
        return distinctFeatureSets.size();
    }


    private static boolean containsEqual(List<double[]> distinctFeatureSets, double[] values) {
        for (double[] distinctFeatureSet : distinctFeatureSets) {
            boolean equal = true;
            for (int i = 0; i < distinctFeatureSet.length; i++) {
                if(distinctFeatureSet[i] != values[i]){
                    equal = false;
                    break;
                }
            }
            if(equal){
                return equal;
            }
        }
        return false;
    }
}
