package nda.app;

import java.io.FileReader;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.yaml.snakeyaml.Yaml;

import nda.data.BehaviorHandlerI;
import nda.data.CountMatrix;
import nda.data.Interval;
import nda.data.SpikeHandlerI;
import nda.data.text.TextBehaviorHandler;
import nda.data.text.TextSpikeHandler;
import nda.util.ArrayUtils;
import nda.util.RandomUtils;


/**
 * Contact split within contact
 * 
 * @author Giuliano Vilela
 */
public class SplitWithinContactApp {
    private static Options options;

    private static String animalName;
    private static String spikesDir;
    private static String contactsFilepath;
    private static String outputFilepath;

    private static List<String> spikeFilters;
    private static double binSize;
    private static int windowSize;

    private static List<String> labels;
    private static int numRounds;
    private static int minTotalPatterns;
    private static int numPatterns;


    @SuppressWarnings("unchecked")
    private static void readSetupFile(String setupFilepath) throws Exception {
        Yaml yaml = new Yaml();
        FileReader reader = null;

        try {
            reader = new FileReader(setupFilepath);

            Map<String,Object> setup = (Map<String,Object>) yaml.load(reader);
            animalName = (String) setup.get("name");
            spikesDir = (String) setup.get("spikes_dir");
            contactsFilepath = (String) setup.get("contacts_file");
            outputFilepath = (String) setup.get("output_file");

            Map<String,Object> params = (Map<String,Object>) setup.get("params");
            spikeFilters = (List<String>) params.get("areas");
            binSize = (Double) params.get("bin_size");
            windowSize = (Integer) params.get("window_width");

            labels = (List<String>) setup.get("labels");
            numRounds = (Integer) setup.get("rounds");
            minTotalPatterns = (Integer) setup.get("min_total_patterns");
            numPatterns = (Integer) setup.get("num_patterns");
        } finally {
            if (reader != null) reader.close();
        }
    }


    static {
        options = new Options();
        options.addOption("h", "help", false, "print help information");
    }


    private static void usage() {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("java -jar dataset-evaluator.jar [-h] setup", options);
        System.exit(1);
    }


    public static void main(String[] args) throws Exception {
        RandomData random = new RandomDataImpl();

        CommandLineParser parser = new GnuParser();
        CommandLine cml = null;

        try {
            cml = parser.parse(options, args, true);
        }
        catch (ParseException e1) {
            usage();
        }

        if (cml.getArgs().length == 0 || cml.hasOption("h"))
            usage();

        String setupFilepath = cml.getArgs()[0];
        readSetupFile(setupFilepath);

        BehaviorHandlerI behaviorHandler = new TextBehaviorHandler(contactsFilepath);

        for (String filter : spikeFilters) {
            SpikeHandlerI spikeHandler = new TextSpikeHandler(spikesDir, filter);
            CountMatrix countMatrix = new CountMatrix(spikeHandler, binSize);
            countMatrix.setWindowWidth(windowSize);

            for (String label : labels) {
                boolean hasValidInterval = false;
                int intervalCount = 0;

                for (Interval interval : behaviorHandler.getContactIntervals(label)) {
                    if (countMatrix.numPatterns(interval) < 2*minTotalPatterns)
                        continue;

                    hasValidInterval = true;

                    List<double[]> patterns = countMatrix.getPatterns(interval);
                    List<double[]> patternsA = patterns.subList(0, patterns.size()/2);
                    List<double[]> patternsB = patterns.subList(patterns.size()/2, patterns.size());

                    int roundCount = 0;
                    for (int j = 0; j < numRounds; ++j) {
                        Object[] sampleA = RandomUtils.randomSample(random, patternsA, numPatterns);
                        Object[] sampleB = RandomUtils.randomSample(random, patternsB, numPatterns);

                        double value = evaluatePatternDistances(sampleA, sampleB);
                        System.out.printf("0, %s, %s, %s, %.04f, %d, %d, %d, %d, %d, %.04f\n",
                                animalName, filter, label, binSize, windowSize,
                                minTotalPatterns, numPatterns, intervalCount, roundCount++, value);
                    }

                    ++intervalCount;
                }

                if (!hasValidInterval) {
                    System.err.printf("Label %s on area %s has no valid intervals\n", label, filter);
                }
            }
        }
    }


    private static double evaluatePatternDistances(Object[] sampleA, Object[] sampleB) {
        int count = 0;
        double distSum = 0.0;

        for (int i = 0; i < sampleA.length; ++i) {
            for (int j = i+1; j < sampleB.length; ++j) {
                double[] patternA = (double[]) sampleA[i];
                double[] patternB = (double[]) sampleB[j];
                distSum += ArrayUtils.euclideanDistance(patternA, patternB);
                ++count;
            }
        }

        return distSum / count;
    }
}