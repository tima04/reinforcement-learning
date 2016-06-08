package domains.tetris;


import domains.Action;
import domains.Features;
import domains.State;
import domains.Task;
import domains.tictactoe.TicTacToeState;

public class TetrisTaskLines implements Task {

    private final double gamma;

    public TetrisTaskLines(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public double getReward(State s, Action a, State sPrime) {
        TetrisState tsPrime = (TetrisState)sPrime;
        return tsPrime.features.nClearedLines;

    }

    @Override
    public boolean taskEnds(State s) {
        TetrisState tsPrime = (TetrisState)s;
        return tsPrime.features.gameOver;
    }

    @Override
    public boolean taskEnds(Features f) {
        TetrisFeatures tetrisFeatures = (TetrisFeatures)f;
        return tetrisFeatures.gameOver;
    }

    @Override
    public State startState() {
        return new TicTacToeState();
    }

    @Override
    public double gamma() {
        return gamma;
    }

    @Override
    public void init() {

    }

}
