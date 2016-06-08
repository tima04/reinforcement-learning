package domains;


import java.util.ArrayList;
import java.util.List;

public interface FeatureSet {
    default List<Double> make(Features features){
        return new ArrayList<>();
    }

    default List<String> featureNames(){
        return new ArrayList<>();
    }

    default String name(){
        return "";
    }
}
