package domains.tetris;


import domains.Action;

public class TetrisAction implements Action {

    public final int col;
    public final int rot;

    public TetrisAction(int col, int rot){
        this.col = col;
        this.rot = rot;
    }

    @Override
    public int id() {
        return hashCode();
    }

    public String name(){
        return col+"_"+rot;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof TetrisAction){
            TetrisAction action = (TetrisAction) o;
            if(action.col == col &&
                    action.rot == rot)
                return true;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return col*100 + rot;
    }


}
