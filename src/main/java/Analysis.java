package analysis;

import domains.tetris.TetrisAction;
import domains.tetris.TetrisState;
import report.GeneralReport;

public interface Analysis {
    void startReport(String reportPath);
    void executeAndWriteLineToReport(TetrisState stateBefore, TetrisAction action);
    void finishReport();
}
