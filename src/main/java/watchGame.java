import domains.Action;
import domains.Features;
import domains.tetris.*;
import org.apache.commons.math3.util.Pair;
import policy.PickAction;
import policy.SingleCueTallyRest;
import processing.core.PApplet;
import processing.event.KeyEvent;
import util.LoadTrajectories;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class watchGame extends PApplet {

    private List<List<Pair<TetrisState, TetrisAction>>> trajectories;

    int stateIdx = 0;
    int gameIdx = 0;

    PickAction pick;

    Random random = new Random(1);

    public static void main(String arg[]){
        PApplet.main(new String[] { /*"--present",*/ "watchGame" });
    }

    public void settings() {
        size(800, 800);
    }

    String dirPath = "src/main/resources/tetris/rawGames/people_new/Malte";

    public void setup(){

        TetrisParameters.getInstance().setSize(20, 10);
        try {
            trajectories = LoadTrajectories.parse(LoadTrajectories.loadData(dirPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int[] weights = new int[]{-1,-1,1};
        pick = new SingleCueTallyRest(weights, 0, new TetrisFeatureSet("people"), random);
    }




    /**
     * Method that draws everything. It happens every frame.
     */
    public void draw(){
        background(255);
        int game = gameIdx+1;
        int state = stateIdx+1;
        text("game: "+game +"/"+trajectories.size(), 50, 50);
        text("state: "+state +"/"+ trajectories.get(gameIdx).size(), 50, 65);
        drawState(trajectories.get(gameIdx).get(stateIdx), 100, 0);
    }

    private void drawState(Pair<TetrisState, TetrisAction> tetrisStateTetrisActionPair, int offset_x, int offset_y) {
        TetrisState state = tetrisStateTetrisActionPair.getFirst();
        int square_w = 20;
        boolean[][] tetrimino = state.piece().getRotatedPiece(0);
        for (int r = 0; r < tetrimino.length; r++) {
            for (int c = 0; c < tetrimino[0].length; c++) {
                if(tetrimino[r][c]){
                    fill(200, 0, 0);
                }else{
                    fill(200);
                }
                rect(offset_x + c * square_w + 4*square_w, offset_y + 4 * square_w - r * square_w, square_w, square_w);
            }
        }
        boolean[][] board = state.board();
        for (int r = 0; r < board.length - 4; r++) {
            for (int c = 0; c < board[0].length; c++) {
                if(board[r][c]){
                    fill(100, 0, 0);
                }else{
                    fill(200);
                }
                rect(offset_x + c * square_w, offset_y + square_w * 8  + 20 * square_w - r * square_w, square_w, square_w);
            }
        }

        TetrisAction action = tetrisStateTetrisActionPair.getSecond();
        TetrisState stateCopy = state.copy();
        stateCopy.nextStateNoClearing(action, state.piece());
        boolean[][] newBoard = stateCopy.board();
        for (int r = 0; r < board.length - 4; r++) {
            for (int c = 0; c < board[0].length; c++) {
                if(newBoard[r][c] && !board[r][c]){
                    fill(250, 0, 0, 100);
                    rect(offset_x + c * square_w, offset_y + square_w * 8  + 20 * square_w - r * square_w, square_w, square_w);
                }
            }
        }

        List<Pair<TetrisAction, TetrisFeatures>> actionsFeaturesListTetris = state.getActionsFeaturesList();
        List<Pair<Action, Features>> actionsFeaturesList = actionsFeaturesListTetris.stream().map(p -> new Pair<Action, Features>(p.getFirst(), p.getSecond())).collect(Collectors.toList());
        int[] actions = pick.pick(state, actionsFeaturesList);
        for (int i : actions) {
            TetrisAction pickedAction = actionsFeaturesListTetris.get(i).getFirst();
            stateCopy = state.copy();
            stateCopy.nextStateNoClearing(pickedAction, state.piece());
            newBoard = stateCopy.board();
            for (int r = 0; r < board.length - 4; r++) {
                for (int c = 0; c < board[0].length; c++) {
                    if(newBoard[r][c] && !board[r][c]){
                        fill(0, 0, 250, 60);
                        rect(offset_x + c * square_w, offset_y + square_w * 8  + 20 * square_w - r * square_w, square_w, square_w);
                    }
                }
            }
        }
    }

    public void keyPressed(KeyEvent event){
        int keyCode = event.getKeyCode();
        if (keyCode == PApplet.RIGHT) {
            stateIdx++;
        } else if (keyCode == PApplet.LEFT) {
            stateIdx--;
        }else if (keyCode == PApplet.UP) {
            if(gameIdx < trajectories.size() - 1) {
                gameIdx++;
                stateIdx = 0;
            }else{
                gameIdx = 0;
                stateIdx = 0;
            }
        }else if (keyCode == PApplet.DOWN) {
            if(gameIdx > 0) {
                gameIdx--;
                stateIdx = 0;
            }else{
                gameIdx = trajectories.size() - 1;
                stateIdx = 0;
            }
        }
        if(stateIdx >= trajectories.get(gameIdx).size()){
            stateIdx = 0;
            if(gameIdx < trajectories.size() - 1) {
                gameIdx++;
            }else{
                gameIdx = 0;
            }
        }
        if(stateIdx < 0){
            gameIdx--;
            if(gameIdx < 0) {
                gameIdx = trajectories.size() - 1;
            }
            stateIdx = trajectories.get(gameIdx).size()-1;
        }
    }


}
