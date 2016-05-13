package report;


import java.io.FileWriter;
import java.io.IOException;

public class GeneralReport{

    private StringBuilder reportString;
    private String filePath;

    public GeneralReport(String filePath){
        this.filePath = filePath;
        reportString = new StringBuilder();
    }

    public void addLine(String line){
        reportString.append(line);
        reportString.append("\n");
    }

    public void generateNew(){
        try {
            FileWriter fw = new FileWriter(filePath, false);
            fw.append(reportString.toString());
            fw.close();
            reportString = new StringBuilder();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generate(){
        try {
            FileWriter fw = new FileWriter(filePath, true);
            fw.append(reportString.toString());
            fw.close();
            reportString = new StringBuilder();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String filePath() {
        return filePath;
    }
}
