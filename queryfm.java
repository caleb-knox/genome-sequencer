import java.io.*;
import java.util.*;

public class queryfm {
    public static void main(String[] args) {
        
        if (args.length == 0) {
            System.err.println("No arguments provided!");
            return;
        }
        String inputName = args[0];
        String queries = args[1];
        String queryMode = args[2];
        String outputName = args[3];
        ArrayList<Object> decode = null;
        int[] sa = null;
        int[] vals = new int[5];
        vals[0] = 0;
        vals[1] = 1;
        int c = 0, g = 0, t = 0;
        String genome = null;
        int[] bwt = null;
        int[][] tally = null;
        try {
            File f = new File(inputName);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            decode = (ArrayList<Object>) ois.readObject();
            genome = (String) decode.get(0);
            sa = (int[]) decode.get(1);
            for (int i = 0; i < sa.length; i++) {
                    if (genome.charAt(sa[i]) == 'C' && c == 0) {
                        vals[2] = i;
                        c = 1;
                    }
                    if (genome.charAt(sa[i]) == 'G' && g == 0) {
                        vals[3] = i;
                        g = 1;
                    }
                    if (genome.charAt(sa[i]) == 'T' && t == 0) {
                        vals[4] = i;
                        t = 1;
                    }
                }
            bwt = (int[]) decode.get(2);
            
            tally = (int[][]) decode.get(3);
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            FileWriter fileWriter = new FileWriter(outputName);
            int startLine = 0;
            while(startLine >= 0) {
                String[] query = getFastaString(queries, startLine);
                // query[0] is the name of the query, query[1] is the query do all computations in here
                int[] ret = new int[2]; 
                String op = "";
                
                if (queryMode.equals("complete")) {

                    ret = completeSearch(query[1], bwt, tally, sa, genome, vals);
                    int matchLen = 0;
                    if (ret[0] != -1) {
                        matchLen = query[1].length();
                    }
                    
                    op = getOutput(query[0], matchLen, ret[0], ret[1], sa);
                } else {
                    ret = partialSearch(query[1], bwt, tally, sa, genome);
                }
                
                startLine = Integer.parseInt(query[2]);
                if (startLine >= 0) {
                    fileWriter.write(op + "\n");
                } else {
                    fileWriter.write(op);
                }
            }
            
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("Failure writing to output!");
        }
        
    }

    public static String getOutput(String queryName, int matchLen, int startIndex, int endIndex, int[] sa) {
        StringBuilder op = new StringBuilder();
        op.append(queryName + "\t" + matchLen + "\t" + ((startIndex == -1 || endIndex == -1) ? 0 : endIndex - startIndex + 1) + "\t");
            if (startIndex != -1) {
                for (int i = startIndex; i <= endIndex; i++) {
                    if (i != endIndex) {
                        op.append(sa[i] + "\t");
                    } else {
                        op.append(sa[i]);
                    }
            }
        }

        return op.toString();
    }

    public static int[] partialSearch(String query, int[] bwt, int[][] tally, int[] sa, String genome) {
        return null;
    }

    public static int[] findFRange(char character, int startT, int endT, int[][] tally, int[] sa, String genome, int[] vals) {
        int v = 0;
        int v2 = 0;
        if (character == '$') {
            v = vals[0];
            v2 = vals[1];
        } else if (character == 'A') {
            v = vals[1];
            v2 = vals[2];
        } else if (character == 'C') {
            v = vals[2];
            v2 = vals[3];
        } else if (character == 'G') {
            v = vals[3];
            v2 = vals[4];
        } else if (character == 'T') {
            v = vals[4];
            v2 = sa.length;
        }
        if (startT == -1) {
            
            return new int[]{v, v2 - 1};
        } else {

            return new int[]{v+startT - 1, v+startT + (endT - startT) - 1};

        }
    }

    public static int[] findLTally(char character, int startIndex, int endIndex, int[][] tally, int[] bwt, String genome) {
        int st = -1, et = -1;

        if (startIndex > 0) {
            if (character == '$') {
                st = tally[0][startIndex];
            } else if (character == 'A') {
                st = tally[1][startIndex];
            } else if (character == 'C') {
                st = tally[2][startIndex];
            } else if (character == 'G') {
                st = tally[3][startIndex];
            } else if (character == 'T') {
                st = tally[4][startIndex];
            }
        }
        
            
            
            
        if (character == '$') {
            et = tally[0][endIndex];
        } else if (character == 'A') {
            et = tally[1][endIndex];
        } else if (character == 'C') {
            et = tally[2][endIndex];
        } else if (character == 'G') {
            et = tally[3][endIndex];
        } else if (character == 'T') {
            et = tally[4][endIndex];
        }
                
            
        
        if (st != -1 && et == -1) {
            et = st;
        }
        if (st == -1) {
            return new int[]{-1, -1};
        }
        return new int[]{st, et};
    }

    public static int[] completeSearch(String query, int[] bwt, int[][] tally, int[] sa, String genome, int[] vals) {
        
        // query subtraction from the end
        int qs = 1;

        // range of values on the first and last column
        int[] fRange = new int[2];
        int[] lRange = new int[2];

        // 
        while (qs <= query.length()) {
            if (qs == 1) {
                // first find all of the last character range
                fRange = findFRange(query.charAt(query.length() - qs), -1, 0, tally, sa, genome, vals);
                
                if (fRange[0] == -1) {
                    return new int[]{-1, -1};
                }
            } else {
                // find all characters starting at rank -- rank that match the current character
                fRange = findFRange(query.charAt(query.length() - qs), lRange[0], lRange[1], tally, sa, genome, vals);
                
                if (fRange[0] == -1) {
                    return new int[]{-1, -1};
                }
            }
            if (qs == query.length()) {
                return new int[]{fRange[0], fRange[1]};
            }
            qs++;
            // we have the range of values in the F, now we want to find the range of values in L that are within the range
            // of values in F that match the next character in the query -- we want to find the tally count of the first char
            lRange = findLTally(query.charAt(query.length() - qs), fRange[0], fRange[1], tally, bwt, genome);
            
            if (lRange[0] == -1) {
                return new int[]{-1, -1};
            }

        }

        
        return new int[]{fRange[0], fRange[1]};
    }

    public static String[] getFastaString(String fasta, int startLine) {
        
         try {
            BufferedReader br = new BufferedReader(new FileReader(fasta));
            String tempLine;
            String line = "", name = "";
            int linesCovered = startLine;
            boolean end = false;

            for (int i = 0; i < startLine; i++) {
                br.readLine();
            }

            while ((tempLine = br.readLine()) != null) {
                if (tempLine.charAt(0) == '>') {
                    if (end == true) {
                        br.close();
                        return new String[]{name, line, linesCovered + ""};
                    }
                    linesCovered++;
                    end = true;
                    name = tempLine.substring(1);
                } else {
                    linesCovered++;
                    line += tempLine;
                }
            }
            br.close();
            return new String[]{name, line, "-1"};

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + fasta);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        return null;
        
    }

}
