package policy;

import domains.Action;
import domains.Features;
import domains.State;
import domains.tetris.TetrisAction;
import domains.tetris.TetrisFeatures;
import domains.tetris.TetrisState;
import org.apache.commons.math3.util.Pair;

import java.util.List;

@FunctionalInterface
public interface PickAction {
    int pick(State state, List<Pair<Action, Features>> actions);
}
