package domains.tetris2;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tetromino {
    /*

    Example: L rotation 0
    1 X X X
    0 X O O
      0 1 2

     */

    public final static List<boolean[][]> O_dat = Arrays.asList(new boolean[][]{{true, true},
                                                                            {true, true}},
                                                            new boolean[][]{{true, true},
                                                                            {true, true}});

    public final static List<boolean[][]> L_dat = Arrays.asList(new boolean[][]{{true, false, false},
                                                                            {true, true, true}},
                                                            new boolean[][]{{true, true},
                                                                            {true, false},
                                                                            {true, false}},
                                                            new boolean[][]{{true, true, true},
                                                                            {false, false, true}},
                                                            new boolean[][]{{false, true},
                                                                            {false, true},
                                                                            {true, true}});

    public final static List<boolean[][]> J_dat = Arrays.asList(new boolean[][]{{false, false, true},
                                                                                {true, true, true}},
                                                                        new boolean[][]{{true, false},
                                                                                {true, false},
                                                                                {true, true}},
                                                                        new boolean[][]{{true, true, true},
                                                                                {true, false, false}},
                                                                        new boolean[][]{{true, true},
                                                                                {false, true},
                                                                                {false, true}});

    public final static List<boolean[][]> S_dat = Arrays.asList(new boolean[][]{{true, true, false},
                                                                                {false, true, true}},
                                                                        new boolean[][]{{false, true},
                                                                                {true, true},
                                                                                {true, false}});

    public final static List<boolean[][]> Z_dat = Arrays.asList(new boolean[][]{{false, true, true},
                                                                                {true, true, false}},
                                                                        new boolean[][]{{true, false},
                                                                                {true, true},
                                                                                {false, true}});

    public final static List<boolean[][]> I_dat = Arrays.asList(new boolean[][]{{true, true, true, true}},
                                                        new boolean[][]{{true},
                                                                        {true},
                                                                        {true},
                                                                        {true}});

    public final static List<boolean[][]> T_dat = Arrays.asList(new boolean[][]{{false,true,false},{true,true,true}},
                                                                    new boolean[][]{{true,false},{true,true},{true,false}},
                                                                new boolean[][]{{true,true,true},{false,true,false}},
                                                                new boolean[][]{{false,true},{true,true},{false,true}});

    public static Tetromino O = new Tetromino(O_dat.subList(0,1), "O");//O_dat contains two equal rotations of O. We need only one.
    public static Tetromino L = new Tetromino(L_dat, "L");
    public static Tetromino S = new Tetromino(S_dat, "S");
    public static Tetromino I = new Tetromino(I_dat, "I");
    public static Tetromino J = new Tetromino(J_dat, "J");
    public static Tetromino Z = new Tetromino(Z_dat, "Z");
    public static Tetromino T = new Tetromino(T_dat, "T");

    public static List<Tetromino> pieces = Arrays.asList(new Tetromino[]{O,L,S,I,J,Z,T});

    final List<boolean[][]> currentPieceDat;
    int currentRotation;
    List<int[]> pieceHeights;
    List<int[]> pieceHoles;
    private String name;

    private Tetromino(List<boolean[][]> pieceDat, String name){
        this.currentPieceDat = pieceDat;
        pieceHeights = new ArrayList<>();
        for (int i = 0; i < currentPieceDat.size(); i++) {
            pieceHeights.add(getPieceHeights(currentPieceDat.get(i)));
        }
        pieceHoles = new ArrayList<>();
        for (int i = 0; i < currentPieceDat.size(); i++) {
            pieceHoles.add(getPieceHoles(currentPieceDat.get(i)));
        }
        this.name = name;
    }

    private int[] getPieceHeights(boolean[][] piece) {
        int[] heights = new int[piece[0].length];
        for (int col = 0; col < piece[0].length; col++) {
            heights[col] = 0;
            int colHeight = piece.length;
            for (int row = piece.length - 1; row >= 0; row--) {
                if(piece[row][col]){
                    break;
                }
                colHeight--;
            }
            heights[col] = colHeight;
        }
        return heights;
    }

    private int[] getPieceHoles(boolean[][] piece) {
        int[] colHoles = new int[piece[0].length];
        for (int col = 0; col < piece[0].length; col++) {
            colHoles[col] = 0;
            int holes = 0;
            for (int row = 0; row < piece[0].length; row++) {
                if(piece[row][col]){
                    break;
                }else{

                }
                holes++;
            }
            colHoles[col] = holes;
        }
        return colHoles;
    }

    public boolean[][] getRotatedPiece(int rotation){
        assert rotation < currentPieceDat.size();
        return currentPieceDat.get(rotation);
    }

    public int getNumRotations(){
        return currentPieceDat.size();
    }


    public static void main(String[] args) {
        Tetromino t = Tetromino.L;
        for (int[] pieceHoles : t.pieceHoles) {
            for (int i = 0; i < pieceHoles.length; i++) {
                System.out.print(pieceHoles[i]+" ");
            }
            System.out.println();
        }

    }

    public int[] getRotatedPieceHeights(int rot) {
        return pieceHeights.get(rot);
    }

    public int[] getRotatedPieceHoles(int rot) {
        return pieceHoles.get(rot);
    }

    public String name(){
        return name;
    }

    public static Tetromino getPiece(String pieceCode) {
        for(Tetromino p: pieces)
            if(p.name().equals(pieceCode))
                return p;

        return null;
    }

    public Tetromino copy(){
        return Tetromino.getPiece(name);
    }

}
