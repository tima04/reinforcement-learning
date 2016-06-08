package domains;

import org.apache.commons.math3.util.Pair;

import java.util.List;

public interface State {
    void getEffect(Action a, List<State> sprimes, List<Double> tprobs);
    List<Pair<Action, Features>> getActionFeaturesList();
    List<Action> getActions();

    Features features();
}
