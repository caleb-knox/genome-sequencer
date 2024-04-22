import java.io.*;
import java.util.*;

public class inspectfm {
    
    public static void main(String[] args) {
        
        if (args.length == 0) {
            System.err.println("No arguments provided!");
            return;
        }
        String inputName = args[0];
        int sampleRate = Integer.parseInt(args[1]);
        String outputName = args[2];
        ArrayList<Object> decode = null;
        String genome = null;
        int[] bwt = null;
        int[][] tally = null;
        try {
            File f = new File(inputName);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            decode = (ArrayList<Object>) ois.readObject();
            genome = (String) decode.get(0);
            bwt = (int[]) decode.get(2);
            tally = (int[][]) decode.get(3);
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        String fmCounts = "", tallySpotCheckA = "", tallySpotCheckC = "", tallySpotCheckG = "", tallySpotCheckT = "";
        StringBuilder bwtText = new StringBuilder();
        for (int i = 0; i < bwt.length; i++) {
            bwtText.append(genome.charAt(bwt[i]));
            if (i % sampleRate == 0) {
                tallySpotCheckA += (tally[1][i]) + "\t";
                tallySpotCheckC += (tally[2][i]) + "\t";
                tallySpotCheckG += (tally[3][i]) + "\t";
                tallySpotCheckT += (tally[4][i]) + "\t";
            }
        }

        fmCounts = tally[0][tally[0].length-1] + "\t" + tally[1][tally[1].length-1] + "\t" 
        + tally[2][tally[2].length-1] + "\t" + tally[3][tally[3].length-1] + "\t" + tally[4][tally[4].length-1];
        
        try {
            FileWriter fileWriter = new FileWriter(outputName);
            // Counts
            fileWriter.write(fmCounts + "\n");
            // BWT
            fileWriter.write(bwtText.toString() + "\n");
            // Tally samples
            fileWriter.write(tallySpotCheckA + "\n");
            fileWriter.write(tallySpotCheckC + "\n");
            fileWriter.write(tallySpotCheckG + "\n");
            fileWriter.write(tallySpotCheckT);
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("Failure writing to output!");
        }
        
    }

   
}
