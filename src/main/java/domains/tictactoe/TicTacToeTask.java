package domains.tictactoe;


import domains.Action;
import domains.State;
import domains.Task;

public class TicTacToeTask implements Task {

    private final double rewardStep = -0.001;
    private final double rewardGoal = 1;
    private final double rewardLose = -1;
    private final double gamma;

    public TicTacToeTask(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public double getReward(State s, Action a, State sPrime) {
        TicTacToeState tsPrime = (TicTacToeState)sPrime;
        TicTacToeState ts = (TicTacToeState)s;

//        if (tsPrime.hasCompleteLine(TicTacToeState.X) || ts.hasCompleteLine(TicTacToeState.X))
//            System.out.println("WON");
//        else if (tsPrime.hasCompleteLine(TicTacToeState.O) || ts.hasCompleteLine(TicTacToeState.O))
//            System.out.println("LOST");

        if (tsPrime.hasCompleteLine(TicTacToeState.X) || ts.hasCompleteLine(TicTacToeState.X))
            return rewardStep + rewardGoal;
        else if (tsPrime.hasCompleteLine(TicTacToeState.O) || ts.hasCompleteLine(TicTacToeState.O))
            return rewardStep + rewardLose;
        else
            return rewardStep;

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
