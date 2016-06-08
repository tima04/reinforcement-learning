package domains.tetris;



import domains.Action;
import domains.FeatureSet;
import domains.State;
import domains.Task;
import domains.tictactoe.TicTacToeState;
import domains.tictactoe.helpers.TicTacToeFeatureSet;
import util.UtilAmpi;

import java.util.List;
import java.util.Random;

public class TetrisCustomTask implements Task {

    private final double gamma;
    List<Double> alpha;
    FeatureSet featureSet;
    Random random;

    public TetrisCustomTask(double gamma, List<Double> alpha, FeatureSet featureSet, Random random) {
        this.gamma = gamma;
        this.alpha = alpha;
        this.featureSet = featureSet;
        this.random = random;
    }

    @Override
    public double getReward(State s, Action a, State sPrime) {
        List<Double> features = featureSet.make(sPrime.features());
        return UtilAmpi.dotproduct(features, alpha);
    }

    @Override
    public boolean taskEnds(State s) {
        TetrisState state = (TetrisState)s;
        return state.features.gameOver;
    }

    @Override
    public State startState() {
        return new TetrisState(random);
    }

    @Override
    public double gamma() {
        return gamma;
    }

    @Override
    public void init() {

    }

}
