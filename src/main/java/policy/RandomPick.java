package policy;

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

    public int pick(TetrisState state, List<Pair<TetrisAction, TetrisFeatures>> actions) {
        return random.nextInt(actions.size());
    }
}