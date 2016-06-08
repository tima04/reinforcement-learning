package policy;

import domains.Action;
import domains.Features;
import domains.State;
import domains.tetris.TetrisAction;
import domains.tetris.TetrisFeatures;
import domains.tetris.TetrisState;
import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.Random;

public class RandomPick implements PickAction{

    Random random;
    public RandomPick(Random random){
        this.random = random;
    }

    public int pick(State state, List<Pair<Action, Features>> actions) {
        return random.nextInt(actions.size());
    }
}