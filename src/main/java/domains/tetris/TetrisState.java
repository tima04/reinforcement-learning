package domains.tetris;


import org.apache.commons.math3.util.Pair;
import util.Compute;

import java.util.*;

public class TetrisState {

    boolean[][] board;
    public static final int height = 16, width = 10, matHeight = height + 4;
    Tetromino piece;
    public TetrisFeatures features;
    String stringKey;
    Random rand;

    public TetrisState(Random rand){
        int m = matHeight, n = width;
        board = new boolean[m][n];

        for (int i = 0; i < m; i++) //Initialize empty board
            for (int j = 0; j < n; j++)
                board[i][j] = false;

        int[] colHeights = new int[n];//Initialize col heights in 0
        for (int i = 0; i < n; i++)
            colHeights[i] = 0;

        this.rand = rand;
        features = new TetrisFeatures.Builder(height, width, 0, 0, 0, false).board(board).build();
        piece = Tetromino.pieces.get(rand.nextInt(Tetromino.pieces.size()));
    }

    TetrisState(boolean[][] board, Tetromino piece, int clearedLines, int nBricksCleared, double landingHeight, boolean gameOver, TetrisFeatures oldTetrisFeatures){
        this.board = board;
        this.piece = piece;
        features = new TetrisFeatures.Builder(height, width, clearedLines, nBricksCleared, landingHeight, gameOver).oldFeatures(oldTetrisFeatures).board(board).build();
    }

    /**
     * Creates an object from a String representation.
     * @param stateStr
     * @return
     */
    public static TetrisState parseState(String stateStr) {
        boolean[][] board = new boolean[TetrisState.matHeight][TetrisState.width];
        String[] rows = stateStr.split("\\|");
        Tetromino piece = Tetromino.getPiece(rows[0]);
        for (int i = 1; i < rows.length; i++) {
            String row = rows[i];
            String[] cols = row.split(":");
            for (String col : cols) {
                if (!col.equals("") && i <= height) {
                    board[i-1][Integer.parseInt(col)] = true;
                }
            }
        }
        return new TetrisState(board, piece, 0, 0, 0, false, null);
    }


    public List<Pair<TetrisAction, TetrisFeatures>> getActionsFeaturesList(){
        List<Pair<TetrisAction, TetrisFeatures>> rslt = new ArrayList<>();
        if(!features.gameOver) {
            for (int rot = 0; rot < piece.getNumRotations(); rot++) {
                for (int col = 0; col <= width - piece.getRotatedPiece(rot)[0].length; col++) {
                    int offset = getVerticalOffset(col, rot);
                    TetrisFeatures newFeatures = getTetrisFeatures(col, offset, piece.getRotatedPieceHeights(rot), piece.getRotatedPieceHoles(rot), piece.getRotatedPiece(rot));
                    rslt.add(new Pair(new TetrisAction(col, rot), newFeatures));
                }
            }
        }
        return rslt;
    }

    /**
     * Changes the state for the one resulting after placing the rotated piece on the given column.
     * If the old state is needed, copy should be called first.
     * @param col
     * @param rot
     * @param random
     */
    public void nextState(int col, int rot, Random random, Tetromino tetromino) {
        int offset = getVerticalOffset(col, rot);
        boolean[][] pieceMatrix = piece.getRotatedPiece(rot);
        int[] pieceHeights = piece.getRotatedPieceHeights(rot);
        int[] pieceHoles = piece.getRotatedPieceHoles(rot);

        int[] oldColHeights = features.colHeights;
        int[] newColHeightsBeforeClearing = getNewColHeights(col, offset, pieceHeights, pieceHoles);

        features = getTetrisFeatures(col, offset, pieceHeights, pieceHoles, pieceMatrix);

        //make piece part of board:
        Pair<List<Integer>, Integer> clearedRowsAndCells = clearedRows(col, pieceMatrix, newColHeightsBeforeClearing);
        integratePieceIntoBoard(this.board, pieceMatrix, col, offset, oldColHeights);
        if(!features.gameOver)
            clearRows(this.board, clearedRowsAndCells);

        if(random == null)
            random = new Random();

        if(tetromino != null)
            piece = tetromino;
        else
            piece = Tetromino.pieces.get(random.nextInt(Tetromino.pieces.size()));
    }

    public void nextState(int col, int rot, Random random){
        nextState(col, rot, random, null);
    }

    public List<TetrisState> nextStates(int col, int rot) {
        List<TetrisState> nextStates = new ArrayList<>();
        for (Tetromino tetromino : Tetromino.pieces) {
            TetrisState base = this.copy();
            base.nextState(col, rot, null, tetromino);
            nextStates.add(base);
        }
        return nextStates;
    }

    private void integratePieceIntoBoard(boolean[][] board, boolean[][] pieceMatrix, int col, int offset, int[] colheights) {
        int nrow = pieceMatrix.length;
        int ncol = pieceMatrix[0].length;
        for (int i = 0; i < nrow; i++)
            for (int j = 0; j < ncol; j++)
                board[colheights[col] + i + offset][col + j] = pieceMatrix[i][j] || board[colheights[col] + i + offset][col + j];
    }

    /**
     * This method returns the vertical offset of the piece being placed.
     * The offset is the landing height of the bottom left corner of the piece with respect to the height of the column.
     *
     * Examples:
     * Offset: 0
     * - - - - - - - - - -
     * - X X - - - - - - -
     * - X - - - - - - - -
     * - X - - - - - - - -
     * O O O O O - - - - -
     *
     * Offset: 1
     * - - - - - - - - - -
     * - X X - - - - - - -
     * - X O - - - - - - -
     * - X O - - - - - - -
     * - - O - - - - - - -
     * O O O O O - - - - -
     *
     * Offset: -1
     * - - - - - - - - - -
     * - - - X X X - - - -
     * O O O O O X - - - -
     * @param col
     * @param rot
     * @return
     */
    private int getVerticalOffset(int col, int rot) {
        int offset = 0;
        while (features.colHeights[col] + offset < height && !fits(col, rot, offset)){
            offset++;
        }
        offset--;
        while (features.colHeights[col] + offset >= 0 && fits(col, rot, offset)){
            offset--;
        }
        offset++;
        return offset;
    }

    boolean fits(int col, int rotation, int offset) {
        boolean[][] pieceMatrix = piece.getRotatedPiece(rotation);
        int[] pieceHoles = piece.getRotatedPieceHoles(rotation);
        for (int i = 0; i < pieceHoles.length; i++)
            if(features.colHeights[col] + offset < features.colHeights[col+i] - pieceHoles[i])
                return false;

        int nrow = pieceMatrix.length;
        int ncol = pieceMatrix[0].length;
        for (int i = 0; i < nrow; i++)
            for (int j = 0; j < ncol; j++) {
                if (col + j >= width)
                    return false;
                try {
                    if (pieceMatrix[i][j] && board[features.colHeights[col] + i + offset][col + j])
                        return false;
                }catch(ArrayIndexOutOfBoundsException exception){//GameOver, but the piece fits.
                    return true;
                }
            }
        return true;
    }



    /**
     * It measures all the new feature values.
     * @param col
     * @param offset
     * @param pieceHeights
     * @param pieceHoles
     * @param rotatedPiece
     * @return
     */
    private TetrisFeatures getTetrisFeatures(int col, int offset, int[] pieceHeights, int[] pieceHoles, boolean[][] rotatedPiece){
        int[] newColHeights = getNewColHeights(col, offset, pieceHeights, pieceHoles);
        Set<Pair<Integer, Integer>> newHoleCoordinates = getHolesCoordinates(col, pieceHoles, newColHeights, pieceHeights, features.holesCords);
        double landingHeight = landingHeight(col, rotatedPiece, newColHeights);
        boolean gameOver = false;

        if(Compute.max(newColHeights) > height)
            gameOver = true;

        //We truncate colheights
        for (int i = 0; i < newColHeights.length; i++)
            if(newColHeights[i] > height)
                newColHeights[i] = height;


        Pair<List<Integer>, Integer> clearedRowsAndCells = clearedRows(col, rotatedPiece, newColHeights);

        if(!gameOver && clearedRowsAndCells.getFirst().size() > 0) {
            boolean[][] boardCopy = boardCopy();
            integratePieceIntoBoard(boardCopy, rotatedPiece, col, offset, features.colHeights);
            clearRows(boardCopy, clearedRowsAndCells);
            return new TetrisFeatures.Builder(height, width, clearedRowsAndCells.getFirst().size(), clearedRowsAndCells.getSecond(), landingHeight, gameOver).oldFeatures(features).board(boardCopy).build();
        }

        return new TetrisFeatures.Builder(height, width, 0, 0, landingHeight, gameOver).colHeights(newColHeights).holesCords(newHoleCoordinates).oldFeatures(features).build();
    }

    private boolean[][] boardCopy() {
        boolean[][] boardCopy = new boolean[matHeight][width];
        for (int r = 0; r < matHeight; r++) {
            for (int c = 0; c < width; c++) {
                boardCopy[r][c] = board[r][c];
            }
        }
        return boardCopy;
    }

    private void clearRows(boolean[][] board, Pair<List<Integer>, Integer> clearedRowsAndCells) {
        for (int i = 0; i < clearedRowsAndCells.getFirst().size(); i++) {//Clear Lines
            int row = clearedRowsAndCells.getFirst().get(i) - i;
            for (int r = row; r < height - 1; r++) {
                for (int c = 0; c < width; c++) {
                    board[r][c] = board[r + 1][c];
                }
            }
            for (int c = 0; c < width; c++)
                board[height - 1][c] = false;
        }
    }

    /**
     * Calculates the new column heights.
     * The changes only happen in the columns where the piece is placed. These columns: (col, col + pieceWidth)
     * The height changes in this way:
     * colHeight[thisColumn] = elevation  + colHeight[col] + pieceHeight[thisColumn]
     * thisColumn ranges from 'col' to 'col + pieceWidth'.
     * @param col
     * @param elevation
     * @param pieceHeights
     * @return
     */
    private int[] getNewColHeights(int col, int elevation, int[] pieceHeights, int[] pieceHoles) {
        int[] newColHeights = features.colHeights.clone();
        for (int i = col; i < col + pieceHeights.length; i++) {
            newColHeights[i] = elevation + features.colHeights[col] + pieceHeights[i - col];
        }
        return newColHeights;
    }

    /**
     * Returns the coordinates of the holes
     * @param col
     * @param pieceHoles
     * @param newHeights
     * @param pieceHeights
     * @param holesCoordinates
     * @return
     */
    private Set<Pair<Integer,Integer>> getHolesCoordinates(int col, int[] pieceHoles, int[] newHeights, int[] pieceHeights, Set<Pair<Integer,Integer>> holesCoordinates) {
        Set<Pair<Integer,Integer>> newHolesIndex = new HashSet<>();
        newHolesIndex.addAll(holesCoordinates);
        for (int i = col; i < col + pieceHoles.length; i++) {
            int newHoleDelta = newHeights[i] - features.colHeights[i] - pieceHeights[i - col] + pieceHoles[i - col];
            if(newHoleDelta > 0)
                for (int row = features.colHeights[i]; row < (newHeights[i] - pieceHeights[i - col]+ pieceHoles[i - col]); row++)
                    if(row < height)
                        newHolesIndex.add(new Pair(row , i));
        }
        return newHolesIndex;
    }

    private double landingHeight(int col, boolean[][] rotatedPiece, int[] newHeights){
        int maxNewHeight = maxNewHeight(col, rotatedPiece, newHeights);
        int pieceHeight = rotatedPiece.length;
        return ((double)maxNewHeight - (double)pieceHeight/2);
    }


    /**
     * Returns the maximum height where the piece was placed
     * @param col
     * @param rotatedPiece
     * @param newHeights
     * @return
     */
    private int maxNewHeight(int col, boolean[][] rotatedPiece, int[] newHeights){
        int maxNewHeight = 0;
        int pieceWidth = rotatedPiece[0].length;
        for (int i = 0; i < pieceWidth; i++)
            if(newHeights[i+col] > maxNewHeight)
                maxNewHeight = newHeights[i+col];

        return maxNewHeight;
    }

    /**
     * Returns the index of the rows that are cleared and the number of bricks of the piece.
     * @param col
     * @param rotatedPiece
     * @param newHeights
     * @return
     */
    private Pair<List<Integer>, Integer> clearedRows(int col, boolean[][] rotatedPiece, int[] newHeights) {
        List<Integer> fullRows = new ArrayList<>();
        int pieceHeight = rotatedPiece.length;
        int pieceWidth = rotatedPiece[0].length;
        int totalBrickPieces = 0;
        int maxNewHeight = maxNewHeight(col, rotatedPiece, newHeights);
        int initRow = maxNewHeight - pieceHeight;
        int finalRow = maxNewHeight;
        for (int r = initRow; r < finalRow; r++) {
            boolean fullRow = true;
            int brickPieces = 0;
            for (int c = 0; c < width; c++) {
                if (!board[r][c]) {//Piece has to be where board is still false.
                    if (c >= col && c - col < pieceWidth){//Piece Territory
                        if (!rotatedPiece[r - initRow][c - col]) {
                            fullRow = false;
                            break;
                        }else{
                            brickPieces++;
                        }
                    }else{
                        fullRow = false;
                        break;
                    }
                }
            }
            if(fullRow) {
                fullRows.add(r);
                totalBrickPieces += brickPieces;
            }
        }
        return new Pair(fullRows,totalBrickPieces);
    }

    public String getStringKey() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(piece.name());
        stringBuilder.append("|");
        for (int row = 0; row < features.pileHeight; row++) {
            for (int column = 0; column < width; column++) {
                if(board[row][column]) {
                    stringBuilder.append(column);
                    stringBuilder.append(":");
                }
            }
            stringBuilder.append("|");
        }
        stringKey = stringBuilder.toString();
        return stringKey;
    }

    public TetrisState copy() {
        return new TetrisState(boardCopy(), piece.copy(), features.nClearedLines, features.nBrickCleared, features.landingHeight, features.gameOver, features.oldFeatures);
    }

    public void print() {
        this.printBoard(board);
    }

    void printBoard(boolean[][] board) {
        boolean[][] pieceMatrix = piece.getRotatedPiece(0);
        int pnrow = pieceMatrix.length;
        int pncol = pieceMatrix[0].length;
        String rslt = "";

        for (int i = pnrow - 1; i >= 0; i--) {
            for (int j = 0; j < pncol; j++)
                rslt += pieceMatrix[i][j] ? " 1 " : " 0 ";

            rslt += "\n";
        }


        int nrow = board.length;
        int ncol = board[0].length;
        rslt += "\n";
        for (int i = nrow-1; i >= 0; i--) {
            for (int j = 0; j < ncol; j++)
                rslt += board[i][j] ? " 1": " 0";
            rslt += "\n";
            if(i == height)
                rslt += "\n";
        }
        System.out.println(rslt);
    }


}
