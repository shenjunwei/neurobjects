package app;

import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;

import weka.core.AttributeStats;
import weka.core.Instances;

import nda.analysis.PatternHandler;
import nda.data.BehaviorHandlerI;
import nda.data.CountMatrix;
import nda.data.Interval;
import nda.data.SpikeHandlerI;
import nda.data.SpikeRateMatrixI;
import nda.data.text.TextBehaviorHandler;
import nda.data.text.TextSpikeHandler;


/**
 * This is a test application.
 * 
 * @author Giuliano Vilela
 * @ingroup ExampleApps
 */
public class PatternsWithBehaviorApp {
    private static String spikeDir = "data/real/ge4/spikes/01";
    private static String defaultBehavior = "data/real/ge4/ge4_contacts.txt";

    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);

        System.out.print("Neurons: ");
        String neuronFilter = in.next();
        if (neuronFilter.equals("*"))
            neuronFilter = "";

        System.out.print("Bin size: ");
        double binSize = in.nextDouble();

        System.out.print("Window width: ");
        int windowWidth = in.nextInt();

        System.out.print("Add random patterns (0 or 1)? ");
        int addRandom = in.nextInt();

        String behaviorFilepath = defaultBehavior;

        Set<String> labels = readLabels(in);

        System.out.print("Output file: ");
        String outputFilepath = in.next();

        // Load the spike data
        System.out.println("Loading the spike data...");
        SpikeHandlerI spikeHandler = new TextSpikeHandler(spikeDir, neuronFilter);

        // Calculate the spike rate function
        System.out.println("Calculating the spike rate function...");
        SpikeRateMatrixI rateMatrix = new CountMatrix(spikeHandler, binSize);
        rateMatrix.setWindowWidth(windowWidth);

        // Load the behavior file
        BehaviorHandlerI behaviorHandler = new TextBehaviorHandler(behaviorFilepath);

        // Build the PatternHandler
        System.out.println("Extracting patterns...\n");
        PatternHandler patternHandler = buildRelation(rateMatrix, behaviorHandler, labels);
        if (addRandom != 0) {
            addRandomPatterns(patternHandler);
        }

        // Create the ARFF file containing the patterns
        System.out.println("Writing " + patternHandler.size() + " patterns to output file...");
        String wekaStr = patternHandler.toWekaFormat();
        FileWriter out = new FileWriter(outputFilepath);
        out.write(wekaStr);
        out.close();

        System.out.println("Exiting!");
    }

    private static Set<String> readLabels(Scanner in) {
        in.nextLine();
        Set<String> labels = new HashSet<String>();

        while (true) {
            System.out.print("Add label ('\\n' to stop): ");

            String label = in.nextLine();
            if (label.isEmpty()) break;

            labels.add(label);
        }

        return labels;
    }

    private static PatternHandler buildRelation(
            SpikeRateMatrixI rateMatrix,
            BehaviorHandlerI behaviorHandler,
            Set<String> labels) {

        Set<String> labelSet = new HashSet<String>(labels);
        labelSet.add("random");

        PatternHandler patternHandler = new PatternHandler("R", rateMatrix, labelSet);

        for (String label : labels) {
            System.out.println("Label: " + label);
            for (Interval interval : behaviorHandler.getContactIntervals(label))
                addPatterns(patternHandler, rateMatrix, interval, label);
        }

        return patternHandler;
    }

    private static void addPatterns(
            PatternHandler patterns,
            SpikeRateMatrixI rateMatrix,
            Interval interval, String label) {

        System.out.println(
                "  - interval: " + interval +
                ", duration: " + interval.duration());

        List<double[]> pl = rateMatrix.getPatterns(interval);
        patterns.addPatterns(pl, label);
    }

    private static void addRandomPatterns(PatternHandler patternHandler) {
        int dimension = patternHandler.getDimension();
        int numPatterns = patternHandler.size();
        int numRandom = numPatterns / 2;

        // Random number generator (poisson distribution)
        double mean = getMean(patternHandler);
        RandomData randomDistr = new RandomDataImpl();

        System.out.println(
                "Adding " + numPatterns + " random patterns " +
                "with mean " + mean + "...");

        // Generate random patterns from [minCount, maxCount]
        for (int i = 0; i < numRandom; ++i) {

            double[] random_pattern = new double[dimension];
            for (int j = 0; j < dimension; ++j) {
                double value = randomDistr.nextPoisson(mean);
                random_pattern[j] = Math.rint(value);
            }

            patternHandler.addPattern(random_pattern, "random");
        }
    }

    private static double getMean(PatternHandler patternHandler) {
        Instances relation = patternHandler.getRelation();

        double sum = 0;
        for (int i = 0; i < relation.numAttributes()-1; ++i) {
            AttributeStats stats = relation.attributeStats(i);
            sum += stats.numericStats.mean;
        }

        return sum / (relation.numAttributes()-1);
    }
}
