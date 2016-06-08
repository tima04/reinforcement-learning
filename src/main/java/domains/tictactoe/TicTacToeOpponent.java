package domains.tictactoe;



import domains.Action;
import util.Pick;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static domains.tictactoe.TicTacToeState.O;


class TicTacToeOpponent {


    public TicTacToeOpponent(){

    }


    public static TicTacToeState play(TicTacToeState state, List<Action> actions, Random ran){
        List<Action> primitives = reduceAvailableActions(state, actions);
        TicTacToeAction primitive= (TicTacToeAction) primitives.get(Pick.indexRandom(primitives.size(), ran));
        return TicTacToeState.boardAfterAction(state, primitive, O);
    }

    /**
     * This method reduces the available actions according to the following criteria:
     * If a line can be completed, the actions that complete it are the only one chosen.
     * If a line needs to be blocked, the actions that block it are the only one chosen
     * Otherwise leave the array intact
     */
    public static List<Action> reduceAvailableActions(TicTacToeState state, List<Action> actions){
        List<Action> reducedActions = new ArrayList<Action>();
        if(actions.size() == 1){
            return actions;
        }
        completeLineIfPossible(state, actions, reducedActions);
        if(reducedActions.size() > 0){
            return reducedActions;
        }
        blockX(state, reducedActions);
        if(reducedActions.size() > 0){
            return reducedActions;
        }
        return actions;
    }

    private static void completeLineIfPossible(TicTacToeState state, List<Action> actions, List<Action> reducedActions) {
        for (Action a : actions) {
            TicTacToeState statePrime = TicTacToeState.boardAfterAction(state, (TicTacToeAction) a, O);
            if (statePrime.hasCompleteLine(O)) {
                reducedActions.add(a);
            }
        }
    }

    private static void blockX(TicTacToeState state, List<Action> reducedActions) {
        int cellToComplete = state.getCellToCompleteLine(TicTacToeState.X);
        if(cellToComplete != -1){
            reducedActions.add(TicTacToeAction.pos(cellToComplete));
        }
    }
}
