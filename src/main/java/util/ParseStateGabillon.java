package util;


import static java.lang.System.out;

public class ParseStateGabillon {

     static int brick_masks[] = {
             0x8000, /* X............... */
             0x4000, /* .X.............. */
             0x2000, /* ..X............. */
             0x1000, /* etc */
             0x0800,
             0x0400,
             0x0200,
             0x0100,
             0x0080,
             0x0040,
             0x0020,
             0x0010,
             0x0008,
             0x0004,
             0x0002, /* ..............X. */
             0
     };

    static boolean[][] parse(String str){
        String[] boardStr = str.split(" ");
        boolean[][] board = new boolean[boardStr.length + 4][10];
        int row = 0;
        for (String lineStr : boardStr) {
            int lineInt = Integer.parseInt(lineStr);
            for (int i = 1; i <= 10; i++) {
                int col = i -1;
                int masked = lineInt & brick_masks[i];
                if (masked > 1) {
                    board[row][col] = true;
                }else {
                    board[row][col] = false;
                }
            }
            row++;
        }
        for (int r = boardStr.length; r < board.length; r++) {
            for (int c = 0; c < 10; c++) {
                board[r][c] = false;
            }
        }
        return board;
    }
}
