package project.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class SimulationResults {
    public int day;
    public int animalCount;
    public int grassCount;
    public int bigGrassCount;
    public int emptyFieldCount;
    public String mostPopularGenome;
    public int mostPopularGenomeCount;
    public double averageLifeLength;
    public int totalDeadAnimals;
    public double averageEnergy;
    public double averageChildrenCount;

    public static String getCSVHeaderLine() {
        String result;
        result = "day," +
                "animalCount," +
                "grassCount," +
                "bigGrassCount," +
                "emptyFieldCount," +
                "mostPopularGenomeCount," +
                "averageLifeLength," +
                "totalDeadAnimals," +
                "averageEnergy," +
                "averageChildrenCount";

        return result;
    }
    public String getCSVLine() {
        String result;
        result = String.format(
                "%d,%d,%d,%d,%d,%d,%.1f,%d,%.1f,%.1f",
                day,
                animalCount,
                grassCount,
                bigGrassCount,
                emptyFieldCount,
                mostPopularGenomeCount,
                averageLifeLength,
                totalDeadAnimals,
                averageEnergy,
                averageChildrenCount);

        return result;
    }

    // Zapisanie linii wyników do pliku CSV
    public void writeLineToCSV(File outputCSVFile) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(outputCSVFile, true))) {
            pw.println(this.getCSVLine());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Zapisanie nagłówka CSV do pliku
    public static void writeHeaderToCSV(File outputCSVFile) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(outputCSVFile))) {
            pw.println(SimulationResults.getCSVHeaderLine());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
