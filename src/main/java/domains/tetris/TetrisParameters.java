package domains.tetris;


public class TetrisParameters {

    private int height;
    private int width;

    static TetrisParameters tetrisParameters = null;

    public static TetrisParameters getInstance(){
        if(tetrisParameters == null)
            tetrisParameters = new TetrisParameters();

        return tetrisParameters;
    }

    TetrisParameters(){

    }

    public void setSize(int height, int width){
        this.height = height;
        this.width = width;
    }

    public int height(){
        return height;
    }

    public int width(){
        return width;
    }
}
