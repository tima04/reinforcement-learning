package domains.tetris;


import domains.Action;
import domains.Features;
import domains.State;
import domains.Task;
import domains.tictactoe.TicTacToeState;

import java.util.Random;

//usage: get demonstrated trajectories for IRL.
public class TetrisTruncatedTaskLines implements Task {

    private final double gamma;
    private final int limitSteps;
    private final Random random;

    int steps;

    public TetrisTruncatedTaskLines(double gamma, int limitSteps, Random random) {
        this.gamma = gamma;
        this.limitSteps = limitSteps;
        this.random = random;
        steps = 1;
    }

    @Override
    public double getReward(State s, Action a, State sPrime) {
        TetrisState tsPrime = (TetrisState)sPrime;
        steps++;
        return tsPrime.features.nClearedLines;

    }

    @Override
    public boolean taskEnds(State s) {
        TetrisState tsPrime = (TetrisState)s;
        return tsPrime.features.gameOver || steps >= limitSteps;
    }

    @Override
    public boolean taskEnds(Features f) {
        TetrisFeatures tetrisFeatures = (TetrisFeatures)f;
        return tetrisFeatures.gameOver || steps >= limitSteps;
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
        steps = 1;
    }

}
