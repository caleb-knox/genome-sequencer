import java.io.*;
import java.util.*;

public class buildfm {

	public static void main(String[] args) {
		String input = args[0];
		String output = args[1];

		StringBuffer genome_buf = new StringBuffer();

		try(BufferedReader br = new BufferedReader(new FileReader(input))) {
			for(String line; (line = br.readLine()) != null; ) {
				if (!line.startsWith(">")) {
					genome_buf.append(line.toUpperCase().strip());
				}
			}
			// line is not visible here.
		} catch (IOException e) {
			e.printStackTrace();
		}
		genome_buf.append("$");
		String genome = genome_buf.toString();
		int[] sa = SuffixArray.constructSuffixArray(genome);

		int[][][] bwtTally = constructFM(sa, genome);
        int[] bwt = bwtTally[0][0];
        int[][] tally = bwtTally[1];
        
       

        // Encoding to binary and storing in a file
        try {
            ArrayList<Object> encode = new ArrayList<>();
            encode.add(genome);
            encode.add(sa);
            // FM index
            encode.add(bwt);
            // Tally
            encode.add(tally);
            File of = new File(output);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(of));
            oos.writeObject(encode);
            oos.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
		
    }

    public static int[][][] constructFM(int[] sa, String genome) {
        int[][][] fmTally = new int[2][][];
        int[] bwt = new int[sa.length];
        int[][] tally = new int[5][sa.length];
        for (int i = 0; i < sa.length; i++) {

            if(sa[i] == 0) {
                bwt[i] = sa.length - 1;
            } else {
                bwt[i] = sa[i] - 1;
            }
            
        }
        int count$ = 0, countA = 0, countC = 0, countG = 0, countT = 0;
        for (int i = 0; i < bwt.length; i++) {
            char currChar = genome.charAt(bwt[i]);
            if (currChar == '$') {
                count$++;
            } else if (currChar == 'A') {
                countA++;
            } else if (currChar == 'C') {
                countC++;
            } else if (currChar == 'G') {
                countG++;
            } else if (currChar == 'T') {
                countT++;
            }
            tally[0][i] = count$;
            tally[1][i] = countA;
            tally[2][i] = countC;
            tally[3][i] = countG;
            tally[4][i] = countT;

        }

        fmTally[0] = new int[][]{bwt, null};
        fmTally[1] = tally;
        return fmTally;
    }
}




/*
public class buildfm {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No file provided!");
            return;
        }

        String genome = getFastaString(args[0]) + "$";
        String outputName = args[1];

        // Creating the suffix array, FM index, tally
        int[] sa = constructSA(genome);

        int[][][] bwtTally = constructFM(sa, genome);
        int[] bwt = bwtTally[0][0];
        int[][] tally = bwtTally[1];
        
        // Encoding to binary and storing in a file
        try {
            ArrayList<Object> encode = new ArrayList<>();
            encode.add(genome);
            encode.add(sa);
            // FM index
            encode.add(bwt);
            // Tally
            encode.add(tally);
            File of = new File(outputName);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(of));
            oos.writeObject(encode);
            oos.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getFastaString(String fasta) {
        
        try {
           BufferedReader br = new BufferedReader(new FileReader(fasta));
           String tempLine = br.readLine();
           String line = "";
           while ((tempLine = br.readLine()) != null) {
               line += tempLine;
           }
           br.close();
           return line;

       } catch (FileNotFoundException e) {
           System.err.println("File not found: " + fasta);
       } catch (IOException e) {
           System.err.println("Error reading the file: " + e.getMessage());
       }

       return null;
       

   }

   public static int[][][] constructFM(int[] sa, String genome) {
        int[][][] fmTally = new int[2][][];
        int[] bwt = new int[sa.length];
        int[][] tally = new int[5][sa.length];
        for (int i = 0; i < sa.length; i++) {

            if(sa[i] == 0) {
                bwt[i] = sa.length - 1;
            } else {
                bwt[i] = sa[i] - 1;
            }
            
        }
        int count$ = 0, countA = 0, countC = 0, countG = 0, countT = 0;
        for (int i = 0; i < bwt.length; i++) {
            char currChar = genome.charAt(bwt[i]);
            if (currChar == '$') {
                count$++;
            } else if (currChar == 'A') {
                countA++;
            } else if (currChar == 'C') {
                countC++;
            } else if (currChar == 'G') {
                countG++;
            } else if (currChar == 'T') {
                countT++;
            }
            tally[0][i] = count$;
            tally[1][i] = countA;
            tally[2][i] = countC;
            tally[3][i] = countG;
            tally[4][i] = countT;

        }

        fmTally[0] = new int[][]{bwt, null};
        fmTally[1] = tally;
        return fmTally;
    }

   // DC3 like algo
    public static int[] constructSA(String fasta) {
        Integer[] sa = new Integer[fasta.length()];
        int[] rank = new int[fasta.length()];

        for (int i = 0; i < rank.length; i++) {
            sa[i] = i;
            rank[i] = fasta.charAt(i);
        }

        for (int i = 1; i < fasta.length(); i *= 2) {
            final int iteration = i;

            Arrays.sort(sa, (iOne, iTwo) -> {
                if (rank[iOne] != rank[iTwo]) {
                    return Integer.compare(rank[iOne], rank[iTwo]);
                } else {
                    int rankA = 0;
                    int rankB = 0;

                    if (iOne + iteration < fasta.length()) {
                        rankA = rank[iOne + iteration];
                    } else {
                        rankA = -1;
                    }

                    if (iTwo + iteration < fasta.length()) {
                        rankB = rank[iTwo + iteration];
                    } else {
                        rankB = -1;
                    }
                    return Integer.compare(rankA, rankB);
                }
            });

            int updateRanks = 0;
            int[] tempRank = new int[fasta.length()];
            tempRank[sa[0]] = 0;

            for (int j = 1; j < fasta.length(); j++) {
                if (rank[sa[j - 1]] != rank[sa[j]] || rank[sa[j - 1] + iteration] != rank[sa[j] + iteration]) {
                    updateRanks++;
                }
                tempRank[sa[j]] = updateRanks;
            }

            System.arraycopy(tempRank, 0, rank, 0, fasta.length());
        }

        int[] returnSA = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            returnSA[i] = sa[i];
        }

        return returnSA;
    }

}*/