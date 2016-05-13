package analysis;

import domains.tetris.TetrisAction;
import domains.tetris.TetrisState;

public interface Analysis {
    void startReport(String reportPath);
    void executeAndWriteLineToReport(TetrisState stateBefore, TetrisAction action);
    void finishReport();
}
