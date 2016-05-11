package domains.tetris;


import org.apache.commons.math3.util.Pair;
import util.Compute;

import java.util.*;

public class Tetris{

    boolean[][] board;
    public static int height = 16, width = 10, matHeight = height + 4;
    Tetromino piece;
    TetrisFeatures features;
    String stringKey;
    Random rand;

    public Tetris(Random rand){
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

    Tetris(boolean[][] board, TetrisFeatures features, Tetromino piece){
        this.board = board;
        this.features = features;
        this.piece = piece;
    }

    public static Tetris parseState(String stateStr) {
        boolean[][] board = new boolean[Tetris.matHeight][Tetris.width];
        String[] rows = stateStr.split("\\|");
        Tetromino piece = Tetromino.getPiece(rows[0]);
        for (int i = 1; i < rows.length; i++) {
            String row = rows[i];
            String[] cols = row.split(":");
            for (String col : cols) {
                if (!col.equals("")) {
                    board[i-1][Integer.parseInt(col)] = true;
                }
            }
        }
        return new Tetris(board, getTetrisFeatures(board), piece);
    }

    public void nextState(TetrisAction a){
        nextState(a.col, a.rot);
    }

    public void nextState(int col, int rot){
        nextState(col, rot, null);
    }

    public List<Pair<TetrisAction, TetrisFeatures>> getActionsFeaturesList(){
        List<Pair<TetrisAction, TetrisFeatures>> rslt = new ArrayList<>();
        for (int rot = 0; rot < piece.getNumRotations(); rot++) {
            for (int col = 0; col <= width - piece.getRotatedPiece(rot)[0].length; col++) {
                int offset = getVerticalOffset(col, rot);
                TetrisFeatures newFeatures = getTetrisFeatures(col, offset, piece.getRotatedPieceHeights(rot), piece.getRotatedPieceHoles(rot), piece.getRotatedPiece(rot));
                rslt.add(new Pair(new TetrisAction(col, rot), newFeatures));
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
    public void nextState(int col, int rot, Random random) {
        int offset = getVerticalOffset(col, rot);
        boolean[][] pieceMatrix = piece.getRotatedPiece(rot);
        int[] pieceHeights = piece.getRotatedPieceHeights(rot);
        int[] pieceHoles = piece.getRotatedPieceHoles(rot);

        //make piece part of board:
        int nrow = pieceMatrix.length;
        int ncol = pieceMatrix[0].length;
        for (int i = 0; i < nrow; i++)
            for (int j = 0; j < ncol; j++)
                board[features.colHeights[col] + i + offset][col + j] = pieceMatrix[i][j] || board[features.colHeights[col] + i + offset][col + j];



        features = getTetrisFeatures(col, offset, pieceHeights, pieceHoles, pieceMatrix);

        if(random == null)
            random = new Random();
        piece = Tetromino.pieces.get(random.nextInt(Tetromino.pieces.size()));
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


    private static TetrisFeatures getTetrisFeatures(boolean[][] board) {
        return new TetrisFeatures.Builder(height, width, 0, 0, 0, true).board(board).build();
    }

    private TetrisFeatures getTetrisFeatures(int col, int offset, int[] pieceHeights, int[] pieceHoles, boolean[][] rotatedPiece){
        int[] newColHeights = getNewColHeights(col, offset, pieceHeights);
        Set<Pair<Integer, Integer>> newHoleCoordinates = getHolesCoordinates(col, pieceHoles, newColHeights, pieceHeights, features.holesCords);
        double landingHeight = landingHeight(col, rotatedPiece, newColHeights);
        boolean gameOver = false;

        if(Compute.max(newColHeights) > height)
            gameOver = true;

        return new TetrisFeatures.Builder(height, width, 0, 0, landingHeight, gameOver).colHeights(newColHeights).holesCords(newHoleCoordinates).build();
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
    private int[] getNewColHeights(int col, int elevation, int[] pieceHeights) {
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
}
