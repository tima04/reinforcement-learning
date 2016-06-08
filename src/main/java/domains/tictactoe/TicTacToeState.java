package domains.tictactoe;


import domains.Action;
import domains.Features;
import domains.State;
import domains.tictactoe.helpers.TicTacToeFeatures;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Board:
 *   0 1 2
 *   7 8 3
 *   6 5 4
 *
 *   X (Agent) begins.
 */
public class TicTacToeState implements State {

    public final static int EMPTY = 0;
    public final static int X = 1;
    public final static int O = 2;

    public int mark = X;

    public TicTacToeFeatures ticTacToeFeatures;

	private int[] board = new int[9];

	private static final int[][] symmetry= {{0, 1, 2, 3, 4, 5, 6, 7, 8},
											{6,7,0,1,2,3,4,5,8},
											{4,5,6,7,0,1,2,3,8},
											{2,3,4,5,6,7,0,1,8},
											{0,7,6,5,4,3,2,1,8},
											{2,1,0,7,6,5,4,3,8},
											{4,3,2,1,0,7,6,5,8},
											{6,5,4,3,2,1,0,7,8} };



    public static TicTacToeState boardAfterAction(TicTacToeState state, TicTacToeAction a, int mark){
        return new TicTacToeState(state, a, mark);
    }

	public TicTacToeState(){
        for (int i : board) {
            board[i] = EMPTY;
        }
        ticTacToeFeatures = new TicTacToeFeatures(this, null);
    }

    private TicTacToeState(TicTacToeState state, TicTacToeAction a, int mark) {
        for (int i = 0; i < board.length; i++) {
            if(a.id() == i){
                board[i] = mark;
            }else{
                board[i] = state.board()[i];
            }
        }
        this.mark = getOpposite(mark);
        board = getCanonicalBoard(board);
        ticTacToeFeatures = new TicTacToeFeatures(this, null);
    }

    private int getOpposite(int mark) {
        if(mark == X) {
            return O;
        }else {
            return X;
        }
    }


    private TicTacToeState(int[] board){
        this.board = board;
        this.board = getCanonicalBoard(board);
        ticTacToeFeatures = new TicTacToeFeatures(this, null);
        mark = X;
    }

    public int[] board(){
        return board;
    }

    public boolean equals(Object o){
        boolean returnValue;
        if (!(o instanceof TicTacToeState))
            returnValue = false;
        else{
            TicTacToeState other = (TicTacToeState) o;
            returnValue = (hashCode() == other.hashCode());
        }
        return returnValue;
    }

    public int hashCode() {
        return (computeCode(getCanonicalBoard(board)));
    }


    // returns the canonical board position (one with maximum hash code)
    private static int[] getCanonicalBoard(int[] board) {
        int max = 0;
        int[] canonicalBoard = null;
        for (int i = 0; i < symmetry.length; i++) {
            int[] boardSymmetric = getSymmetricBoard(i, board);
            int code = computeCode(boardSymmetric);
            if (code >= max) {
                max = code;
                canonicalBoard = boardSymmetric;
            }
        }
        return canonicalBoard;
    }

	private static int[] getSymmetricBoard(int which, int[] board){
		int[] symmetricBoard = new int[9];
		for (int i = 0; i < symmetricBoard.length; i++) {
			symmetricBoard[symmetry[which][i]] = board[i];
		}
		return symmetricBoard;
	}

	public boolean hasCompleteLine(int mark) {
        return (board[0] == mark && board[1] == mark && board[2] == mark)
                || (board[2] == mark && board[3] == mark && board[4] == mark)
                || (board[4] == mark && board[5] == mark && board[6] == mark)
                || (board[6] == mark && board[7] == mark && board[0] == mark)
                || (board[0] == mark && board[8] == mark && board[4] == mark)
                || (board[2] == mark && board[8] == mark && board[6] == mark)
                || (board[1] == mark && board[8] == mark && board[5] == mark)
                || (board[3] == mark && board[8] == mark && board[7] == mark);
    }

    /**
     * This methods returns the position of the cell that is missing to complete three in line.
     * It returns -1 if the player is not about to win, so a line cannot be completed.
     */
    public int getCellToCompleteLine(int mark) {
        int missing = getMissingCell(0, 1, 2, mark);
        if(missing != -1){return missing;}
        missing = getMissingCell(2, 3, 4, mark);
        if(missing != -1){return missing;}
        missing = getMissingCell(4, 5, 6, mark);
        if(missing != -1){return missing;}
        missing = getMissingCell(6, 7, 0, mark);
        if(missing != -1){return missing;}
        missing = getMissingCell(0, 8, 4, mark);
        if(missing != -1){return missing;}
        missing = getMissingCell(0, 8, 4, mark);
        if(missing != -1){return missing;}
        missing = getMissingCell(2, 8, 6, mark);
        if(missing != -1){return missing;}
        missing = getMissingCell(1, 8, 5, mark);
        if(missing != -1){return missing;}
        missing = getMissingCell(3, 8, 7, mark);
        return missing;
    }

    private int getMissingCell(int first, int second, int third, int mark){
        if(board[first] == mark && board[second] == mark && board[third] == EMPTY){return third;}
        if(board[second] == mark && board[third] == mark && board[first] == EMPTY){return first;}
        if(board[first] == mark && board[third] == mark && board[second] == EMPTY){return second;}
        return -1;
    }



    public String toString(){
        return this.toVertexName();
    }


    public String toVertexName(){
        String name = "";
        for (int aBoard : board) {
            name += aBoard;
        }
        return name;
    }

    public String print(){
        String fKey = "";
        fKey += getMark(board[0])+"|"+getMark(board[1])+"|"+getMark(board[2])+"\n";
        fKey += getMark(board[7])+"|"+getMark(board[8])+"|"+getMark(board[3])+"\n";
        fKey += getMark(board[6])+"|"+getMark(board[5])+"|"+getMark(board[4]);
        return fKey;
    }

    private static int computeCode(int[] board) {
        return board[0]
                +    3 * board[1]
                +    9 * board[2]
                +   27 * board[3]
                +   81 * board[4]
                +  243 * board[5]
                +  729 * board[6]
                + 2187 * board[7]
                + 6561 * board[8]
                ;
    }

    private String getMark(int mark) {
        switch (mark) {
            case X:
                return "X";
            case EMPTY:
                return " ";
            case O:
                return "O";
        }
        return "";
    }

    @Override
    public List<Pair<Action, Features>> getActionFeaturesList(){
        return  getActionFeaturesList(X);
    }

    public List<Pair<Action, Features>> getActionFeaturesList(int mark) {
        List<Pair<Action, Features>> primitives = new ArrayList<Pair<Action, Features>>();
        if(!hasCompleteLine(X) && !hasCompleteLine(O)) {
            for (int i = 0; i < board().length; i++) {
                if (board()[i] == EMPTY) {
                    primitives.add(new Pair(TicTacToeAction.pos(i), TicTacToeState.boardAfterAction(this, TicTacToeAction.pos(i), mark).ticTacToeFeatures));
                }
            }
        }
        if (primitives.isEmpty()) {
            primitives.add(new Pair(TicTacToeAction.pos(9), ticTacToeFeatures));
        }
        return primitives;
    }

    @Override
    public List<Action> getActions(){
        List<Action> primitives = new ArrayList<Action>();
        if(!hasCompleteLine(X) && !hasCompleteLine(O)) {
            for (int i = 0; i < board().length; i++) {
                if (board()[i] == EMPTY) {
                    primitives.add(TicTacToeAction.pos(i));
                }
            }
        }
        if (primitives.isEmpty()) {
            primitives.add(TicTacToeAction.pos(9));
        }
        return primitives;
    }

    @Override
    public Features features() {
        return ticTacToeFeatures;
    }

    @Override
    public void getEffect(Action a, List<State> sprimes, List<Double> tprobs) {
        TicTacToeAction action = (TicTacToeAction) a;
        TicTacToeState sPrime = TicTacToeState.boardAfterAction(this, action, X);
        List<Action> opponentActions = TicTacToeOpponent.reduceAvailableActions(sPrime, sPrime.getActions());
        double prob = 1.0/opponentActions.size();
        for (Action primitive : opponentActions) {
            TicTacToeState sPrime2 = TicTacToeState.boardAfterAction(sPrime, (TicTacToeAction) primitive, O);
            sprimes.add(sPrime2);
            tprobs.add(prob);
        }
    }

    public boolean isActionPossible() {
        if(getActions().contains(TicTacToeAction.pos(9)))
            return false;
        else
            return true;
    }
}
