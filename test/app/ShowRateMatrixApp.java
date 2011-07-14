package app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Scanner;

import nda.data.CountMatrix;
import nda.data.SpikeHandlerI;
import nda.data.text.TextSpikeHandler;


/**
 * This is a test application for the SpikeRateMatrixI component and its
 * CountMatrix implementation. It shows how to use the CountMatrix to
 * calculate a count rate matrix and create text files containing the
 * count matrix itself and a series of patterns extracted from it.
 * 
 * The application reads various parameter values provided by the user
 * from the standard input.
 * 
 * @author Giuliano Vilela
 * @ingroup ExampleApps
 */
public class ShowRateMatrixApp {
    // Path to a directory with real spike files
    private static final String spikeDir = "data/test/spikes";

    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);

        // Read the neuron filter: used to choose the spike files that will be opened
        System.out.println("Filter: ");
        String filter = in.nextLine().trim();

        // Read the bin size: size of the time window that each bin in the
        // count matrix contains
        System.out.print("Bin size: ");
        double binSize = in.nextDouble();

        // Read the start time: starting point for the pattern extraction
        System.out.print("Start time: ");
        double startTime = in.nextDouble();

        // Read the window width: length of a single part of a pattern
        // see the SpikeRateMatrixI docs for a better explanation
        System.out.print("Window width: ");
        int width = in.nextInt();

        // Read the cursor step: size of the increment used to gather patterns
        // see the SpikeRateMatrixI docs
        System.out.print("Cursor step: ");
        int cursorStep = in.nextInt();

        // Create a SpikeHandlerI containing the desired neurons
        SpikeHandlerI spikeHandler = new TextSpikeHandler(spikeDir, filter);

        // Setup the CountMatrix with the given parameters
        CountMatrix rateMatrix = new CountMatrix(spikeHandler, binSize);
        rateMatrix.setCurrentTime(startTime);
        rateMatrix.setWindowWidth(width);
        rateMatrix.setStep(cursorStep);


        // Open a file to write the complete count matrix.
        // Ex: count_matrix_HP.mat
        String matrixFilename = "count_matrix_" + filter + ".mat";
        BufferedWriter matrixOut = new BufferedWriter(new FileWriter(matrixFilename));

        // Loop over all the counts and write them to matrixOut
        for (int r = 0; r < rateMatrix.numRows(); ++r) {
            for (int c = 0; c < rateMatrix.numColumns(); ++c) {
                String num = Integer.toString(rateMatrix.get(r, c));
                matrixOut.write(num + ' ');
            }
            matrixOut.write('\n');
        }
        matrixOut.close();


        // Open a file to write all the patterns extracted from the CountMatrix
        // Ex: pattern_HP_0,25_5820_00_50_1.mat
        String patternFilename = String.format("pattern_%s_%.02f_%.02f_%d_%d.mat",
                filter, binSize, startTime, width, cursorStep);
        BufferedWriter patternOut = new BufferedWriter(new FileWriter(patternFilename));

        // Loop over all the patterns in the current configuration of the rateMatrix
        for (double[] pattern : rateMatrix) {
            for (int i = 0; i < pattern.length; ++i)
                patternOut.write(pattern[i] + " ");
            patternOut.write('\n');
        }
        patternOut.close();
    }
}
