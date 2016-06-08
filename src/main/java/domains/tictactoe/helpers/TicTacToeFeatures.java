package domains.tictactoe.helpers;



import domains.Features;
import domains.tictactoe.TicTacToeState;

import java.util.ArrayList;
import java.util.List;

public class TicTacToeFeatures implements Features{

    int xCorners;
    int oCorners;
    int xCenter;
    int oCenter;
    int xWinChance;
    int oWinChance;
    int emptyThreeRow;
    public int win;
    int xSide;
    int xBlocks;
    int oBlocks;
    int xCornersDelta;
    int xCenterDelta;
    int xSideDelta;
    int xBlocksDelta;
    int oBlocksDelta;


    private TicTacToeState state;

    public TicTacToeFeatures(TicTacToeState state, TicTacToeFeatures oldFeatures) {
        this.state = state;
        calculateFeatures(oldFeatures);
    }


    private void calculateFeatures(TicTacToeFeatures oldFeatures) {
        xCorners = (int) calculateCorners(TicTacToeState.X);
        oCorners = (int) calculateCorners(TicTacToeState.O);
        xCenter = (int) calculateCenter(TicTacToeState.X);
        oCenter = (int) calculateCenter(TicTacToeState.O);
        xWinChance = (int) calculateWinChance(TicTacToeState.X);
        oWinChance = (int)calculateWinChance(TicTacToeState.O);
        emptyThreeRow = (int) emptyThreeInRow();
        win = (int) win(TicTacToeState.X);
        xSide = (int) calculateSide(TicTacToeState.X);
        oBlocks = (int) calculateBlocks(TicTacToeState.X, TicTacToeState.O);
        xBlocks = (int) calculateBlocks(TicTacToeState.O, TicTacToeState.X);
        if(oldFeatures != null) {
            xCornersDelta = xCorners - oldFeatures.xCorners;
            xCenterDelta = xCenter - oldFeatures.xCenter;
            xSideDelta = xSide - oldFeatures.xSide;
            xBlocksDelta = xBlocks - oldFeatures.xBlocks;
            oBlocksDelta = oBlocks - oldFeatures.oBlocks;
        }else{
            xCornersDelta = xCorners - 0;
            xCenterDelta = xCenter - 0;
            xSideDelta = xSide - 0;
            xBlocksDelta = xBlocks - 0;
            oBlocksDelta = oBlocks - 0;
        }
    }



    private double win(int x) {
        if(state.hasCompleteLine(x)){
            return 1;
        }else{
            return 0;
        }
    }

    /**
     *   Board:
     *   0 1 2
     *   7 8 3
     *   6 5 4
     */

    private double calculateWinChance(int x) {
        if(state.getCellToCompleteLine(x) != -1){
            return 1;
        }
        return 0;
    }

    private double calculateCenter(int x) {
        int[] board = state.board();
        int numX = 0;
        numX+= board[8] == x?1:0;
        return numX;
    }

    private double calculateCorners(int x) {
        int[] board = state.board();
        int numX = 0;
        numX+= board[0] == x?1:0;
        numX+= board[2] == x?1:0;
        numX+= board[6] == x?1:0;
        numX+= board[4] == x?1:0;
        return numX;
    }

    private double calculateSide(int x) {
        int[] board = state.board();
        int numX = 0;
        numX+= board[1] == x?1:0;
        numX+= board[3] == x?1:0;
        numX+= board[7] == x?1:0;
        numX+= board[5] == x?1:0;
        return numX;
    }

    private double emptyThreeInRow() {
        int[] board = state.board();
        int numX = 0;
        if(board[0] == 0 && board[1] == 0 && board[2] == 0){
            numX++;
        }
        if(board[2] == 0 && board[3] == 0 && board[4] == 0){
            numX++;
        }
        if(board[6] == 0 && board[5] == 0 && board[4] == 0){
            numX++;
        }
        if(board[6] == 0 && board[7] == 0 && board[0] == 0){
            numX++;
        }
        if(board[8] == 0 && board[7] == 0 && board[3] == 0){
            numX++;
        }
        if(board[8] == 0 && board[1] == 0 && board[5] == 0){
            numX++;
        }
        if(board[8] == 0 && board[0] == 0 && board[4] == 0){
            numX++;
        }
        if(board[8] == 0 && board[6] == 0 && board[2] == 0){
            numX++;
        }

        return numX;
    }


    private double calculateBlocks(int x, int opposite_x) {
        int blocks = 0;
        int[] board = state.board();
        for (int[] ints : threeInRow()) {
            if((board[ints[0]] == x && board[ints[1]] == opposite_x && board[ints[2]] == opposite_x) ||
                    (board[ints[0]] == opposite_x && board[ints[1]] == x && board[ints[2]] == opposite_x) ||
                    (board[ints[0]] == opposite_x && board[ints[1]] == opposite_x && board[ints[2]] == x) ){
                blocks++;
            }
        }
        return blocks;
    }


    private List<int[]> threeInRow(){
        List<int[]> threeInRow = new ArrayList();
        threeInRow.add(new int[]{0,1,2});
        threeInRow.add(new int[]{7,8,3});
        threeInRow.add(new int[]{6,5,4});
        threeInRow.add(new int[]{0,7,6});
        threeInRow.add(new int[]{1,8,5});
        threeInRow.add(new int[]{2,3,4});
        threeInRow.add(new int[]{0,8,4});
        threeInRow.add(new int[]{2,8,6});
        return threeInRow;
    }

}
