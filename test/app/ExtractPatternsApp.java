package app;

import java.util.HashSet;
import java.util.Set;

import nda.data.CountMatrix;
import nda.data.Interval;
import nda.data.PatternHandler;
import nda.data.SpikeHandlerI;
import nda.data.SpikeRateMatrixI;
import nda.data.text.TextSpikeHandler;


/**
 * This is a test application for the PatternHandler component.
 * 
 * It shows how to open a set of files with spike train data, estimate the spike rate
 * function and create a Weka ARFF file containing patterns extracted from the spike rate
 * matrix.
 * 
 * @author Giuliano Vilela
 * @ingroup ExampleApps
 */
public class ExtractPatternsApp {
    private static final String spikeDir = "setup/spikes";

    public static void main(String[] args) throws Exception {
        // Create a SpikeHandlerI containing the desired neurons
        SpikeHandlerI spikeHandler = new TextSpikeHandler(spikeDir, "S1");

        /*
         * Setup the CountMatrix with the given parameters.
         * Each line of the matrix has 5 bins (columns)
         */
        SpikeRateMatrixI rateMatrix = new CountMatrix(spikeHandler, 5);

        /*
         * The spike rate matrix has the following layout:
         * 
         *           b0  b1  b2  b3  b4
         *   S1_03A  ..  ..  ..  ..  ..
         *   S1_07A  ..  ..  ..  ..  ..
         *   S1_08C  ..  ..  ..  ..  ..
         * 
         * We'll extract 3 patterns from it, beginning at the start of the spike interval
         * and having window width 3, so each pattern has 3*3 = 9 values.
         */
        Interval interval = rateMatrix.getInterval();
        rateMatrix.setCurrentTime(interval.start());
        rateMatrix.setWindowWidth(3);
        rateMatrix.setStep(1);

        // Each pattern will have one of the following labels
        Set<String> labels = new HashSet<String>();
        labels.add("A");
        labels.add("B");
        labels.add("C");

        // Create a PatterHandler object with the above configuration
        PatternHandler relation = new PatternHandler("S1_Patterns", rateMatrix, labels);

        // Insert the patterns
        int i = 0;
        for (double[] pattern : rateMatrix) {
            String label;

            if (i == 0)
                label = "A";
            else if (i == 1)
                label = "B";
            else
                label = "C";

            i++;
            relation.addPattern(pattern, label);
        }

        // Generate an ARFF representation of the PatternHandler and print it
        String weka_str = relation.toWekaFormat();
        System.out.println(weka_str);
    }
}
