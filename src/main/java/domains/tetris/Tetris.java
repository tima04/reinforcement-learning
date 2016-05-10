package domains.tetris;


import java.util.Random;

public class Tetris{

    boolean[][] board;
    public static int height = 16, width = 10, matHeight = height + 4;
    Tetromino piece;
//    TetrisFeatures features;
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
//        features = new TetrisFeatures(colHeights, new ArrayList<>(), false);
        piece = Tetromino.pieces.get(rand.nextInt(Tetromino.pieces.size()));
    }

}
