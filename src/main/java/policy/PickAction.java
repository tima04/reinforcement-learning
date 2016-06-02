package policy;

import domains.tetris.TetrisAction;
import domains.tetris.TetrisFeatures;
import domains.tetris.TetrisState;
import org.apache.commons.math3.util.Pair;

import java.util.List;


public interface PickAction {
    int pick(TetrisState state, List<Pair<TetrisAction, TetrisFeatures>> actions);
}
