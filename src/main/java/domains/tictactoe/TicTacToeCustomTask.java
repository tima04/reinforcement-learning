package domains.tictactoe;



import domains.Action;
import domains.FeatureSet;
import domains.State;
import domains.Task;
import domains.tictactoe.helpers.TicTacToeFeatureSet;
import util.Compute;
import util.UtilAmpi;

import java.util.List;

public class TicTacToeCustomTask implements Task {

    private final double gamma;
    List<Double> alpha;
    FeatureSet featureSet;

    public TicTacToeCustomTask(double gamma, List<Double> alpha, FeatureSet featureSet) {
        this.gamma = gamma;
        this.alpha = alpha;
        this.featureSet = featureSet;
    }

    @Override
    public double getReward(State s, Action a, State sPrime) {
        List<Double> features = featureSet.make( ((TicTacToeState) sPrime).ticTacToeFeatures);
        return UtilAmpi.dotproduct(features, alpha);
    }

    @Override
    public boolean taskEnds(State s) {
        TicTacToeState state = (TicTacToeState)s;
        return (state.hasCompleteLine(TicTacToeState.X) || state.hasCompleteLine(TicTacToeState.O) || !state.isActionPossible());
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
