package nda.analysis.generation;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;

import nda.analysis.InvalidSetupFileException;
import nda.analysis.PatternHandler;
import nda.data.BehaviorHandlerI;
import nda.data.CountMatrix;
import nda.data.Interval;
import nda.data.SpikeHandlerI;
import nda.data.SpikeRateMatrixI;
import nda.data.text.TextBehaviorHandler;
import nda.data.text.TextSpikeHandler;
import nda.util.FileUtils;
import nda.util.Verbose;


/**
 * Dataset generator component.
 * 
 * Reads settings from a setup config file and generates Datasets
 * in an implementation defined way.
 * 
 * @author Giuliano Vilela
 */
public abstract class DatasetGenerator implements Verbose {
    protected GeneratorSetup setup;
    protected SpikeHandlerI globalSpikeHandler;
    protected BehaviorHandlerI behaviorHandler;
    protected RandomData randomData;
    protected boolean verbose;


    public DatasetGenerator(String setupFilepath)
    throws FileNotFoundException, InvalidSetupFileException {
        this(new GeneratorSetup(setupFilepath));
    }


    public DatasetGenerator(GeneratorSetup _setup) {
        setup = _setup;
        randomData = new RandomDataImpl();
    }


    public abstract void generate() throws GenerationException;


    /**
     * Builds all rounds for a single dataset.
     * 
     * This method generates dataset.getNumberRounds() * 2 PatternHandler's.
     * Each round has a train relation and a test relation.
     */
    protected List<PatternHandler> buildDataset(GeneratorSetup.Dataset dataset)
    throws GenerationException {
        int estimate = dataset.getNumberRounds() * 2;
        List<PatternHandler> patterns = new ArrayList<PatternHandler>(estimate);

        SpikeRateMatrixI rateMatrix = buildDatasetRateMatrix(dataset);

        for (int round = 1; round <= dataset.getNumberRounds(); ++round)
            patterns.addAll(buildDatasetSingleRound(dataset, round, rateMatrix));

        return patterns;
    }


    /**
     * Builds a single round of a dataset.
     * 
     * This method generates 2 PatternHandler's (train and test).
     */
    protected List<PatternHandler> buildDatasetSingleRound(
            GeneratorSetup.Dataset dataset, int round,
            SpikeRateMatrixI rateMatrix)
            throws GenerationException {

        Set<String> labels = new HashSet<String>();
        for (GeneratorSetup.Class class_attr : dataset.getClasses())
            labels.add(class_attr.getName());

        String trainFileName = dataset.getGeneratedFileName("train", round);
        String testFileName = dataset.getGeneratedFileName("test", round);

        String trainSetName = FileUtils.parseFileName(trainFileName);
        String testSetName = FileUtils.parseFileName(testFileName);

        PatternHandler trainSet = new PatternHandler(trainSetName, rateMatrix, labels);
        PatternHandler testSet = new PatternHandler(testSetName, rateMatrix, labels);

        for (GeneratorSetup.Class class_attr : dataset.getClasses()) {
            addInstancesFromClass(class_attr, rateMatrix, trainSet, testSet);
        }

        List<PatternHandler> sets = new ArrayList<PatternHandler>(2);
        sets.add(trainSet);
        sets.add(testSet);
        return sets;
    }


    /**
     * @param patternHandler
     * @param rateMatrix
     * @param class_attr
     * @param number
     */
    protected void addInstancesFromClass(
            GeneratorSetup.Class class_attr,
            SpikeRateMatrixI rateMatrix,
            PatternHandler trainSet, PatternHandler testSet)
    throws GenerationException {

        int totalPatterns = 0;
        for (String label : class_attr.getLabels())
            for (Interval interval : behaviorHandler.getIntervals(label))
                totalPatterns += rateMatrix.numPatterns(interval);

        int numTrain = class_attr.getNumberTrainSamples();
        int numTest = class_attr.getNumberTestSamples();

        if (numTrain + numTest > totalPatterns) {
            throw new GenerationException(String.format(
                    "Can't sample %d train patterns and %d test patterns from " +
                    "class %s (%d patterns from labels %s) of dataset %s",
                    numTrain, numTest, class_attr.getName(), totalPatterns,
                    ArrayUtils.toString(class_attr.getLabels().toArray()),
                    class_attr.getDataset().getName()));
        }

        int[][] inds = sampleTrainTest(totalPatterns, numTrain, numTest);
        int[] trainInds = inds[0];
        int[] testInds = inds[1];


        /**
         * TODO: remove this
         */

        List<double[]> all_patterns = new ArrayList<double[]>();
        for (String label : class_attr.getLabels())
            for (Interval interval : behaviorHandler.getIntervals(label))
                all_patterns.addAll(rateMatrix.getPatterns(interval));

        for (int i : trainInds) {
            double[] pattern = all_patterns.get(i);
            trainSet.addPattern(pattern, class_attr.getName());
        }

        for (int i : testInds) {
            double[] pattern = all_patterns.get(i);
            testSet.addPattern(pattern, class_attr.getName());
        }

        /*Map<String, List<Integer>> trainColumns = sampleInstancesColumns(
                rateMatrix, class_attr, trainInds);

        for (List<Integer> columns : trainColumns.values()) {
            for (Integer col : columns) {
                rateMatrix.setCurrentColumn(col);
                double[] pattern = rateMatrix.iterator().next();

                trainSet.addPattern(pattern, class_attr.getName());
            }
        }

        Map<String, List<Integer>> testColumns = sampleInstancesColumns(
                rateMatrix, class_attr, testInds);

        for (List<Integer> columns : testColumns.values()) {
            for (Integer col : columns) {
                rateMatrix.setCurrentColumn(col);
                double[] pattern = rateMatrix.iterator().next();

                testSet.addPattern(pattern, class_attr.getName());
            }
        }*/
    }


    /**
     * Map label -> List <column indices in rateMatrix>
     * 
     * Samples number patterns from rateMatrix, only considering labels from
     * class_attr.getLabels().
     * 
     * @param rateMatrix
     * @param class_attr
     * @param sample_indexes Indexes from randomSample(total, K), where total =
     *          sum (rateMatrix.numPatterns(interval)
     *                 for interval in behaviorHandler.getIntervals(label)
     *                 for label in class_attr.getLabels())
     */
    /*protected Map<String, List<Integer>> sampleInstancesColumns(
            SpikeRateMatrixI rateMatrix,
            GeneratorSetup.Class class_attr, int[] sample_indexes) {

        List<String> labels = class_attr.getLabels();

        Map<String,List<Integer>> sample =
            new HashMap<String,List<Integer>>();

        int numInstances = sample_indexes.length;

        for (String label : labels)
            sample.put(label, new ArrayList<Integer>(numInstances));

        int sample_p = 0;
        int seen = 0;

        loop:
            for (String label : labels) {
                List<Interval> intervals = behaviorHandler.getIntervals(label);

                int intervals_p = 0;
                while (intervals_p < intervals.size()) {
                    Interval interval = intervals.get(intervals_p);

                    if (sample_p == sample_indexes.length)
                        break loop;

                    int next_sz = rateMatrix.numPatterns(interval);
                    int next_ind_sample = sample_indexes[sample_p];

                    if (next_ind_sample < seen + next_sz) {
                        int real_ind = next_ind_sample - seen;
                        int column = getPatternColumn(rateMatrix, interval, real_ind);

                        List<Integer> list = sample.get(label);
                        list.add(column);

                        sample_p++;
                    }
                    else {
                        intervals_p++;
                        seen += next_sz;
                    }
                }
            }

        return sample;
    }*/


    protected void loadHandlers() throws GenerationException {
        try {
            String spikeDir = setup.getSpikesDirectory();
            globalSpikeHandler = new TextSpikeHandler(spikeDir);

            String behaviorFilepath = setup.getContactsFilepath();
            behaviorHandler = new TextBehaviorHandler(behaviorFilepath);
        } catch (Exception e) {
            throw new GenerationException(e);
        }
    }


    protected SpikeRateMatrixI buildDatasetRateMatrix(GeneratorSetup.Dataset dataset)
    throws GenerationException {

        String neuronFilter = (String) dataset.getParameter("areas");
        SpikeHandlerI datasetHandler = globalSpikeHandler.withFilter(neuronFilter);

        double binSize = (Double) dataset.getParameter("bin_size");
        int window_width = (Integer) dataset.getParameter("window_width");

        SpikeRateMatrixI rateMatrix = new CountMatrix(datasetHandler, binSize);
        rateMatrix.setWindowWidth(window_width);
        return rateMatrix;
    }


    /**
     * Create a random sample of train and test indexes from the set [0,n-1].
     * 
     * Return an int[2][] M, where M[0] is an int[train] with the train indexes and
     * M[1] is an int[test] with the test indexes.
     */
    protected int[][] sampleTrainTest(int n, int train, int test) {
        Set<Integer> all = new HashSet<Integer>();
        for (int i = 0; i < n; ++i)
            all.add(i);

        Object[] train_objs, test_objs;

        if (train > 0)
            train_objs = randomData.nextSample(all, train);
        else
            train_objs = new Object[0];

        for (Object obj : train_objs)
            all.remove(obj);

        if (test > 0)
            test_objs = randomData.nextSample(all, test);
        else
            test_objs = new Object[0];

        int[] train_inds = new int[train];
        for (int i = 0; i < train; ++i)
            train_inds[i] = (Integer) train_objs[i];

        int[] test_inds = new int[test];
        for (int i = 0; i < test; ++i)
            test_inds[i] = (Integer) test_objs[i];

        Arrays.sort(train_inds);
        Arrays.sort(test_inds);
        return new int[][] { train_inds, test_inds };
    }


    /*protected static int getPatternColumn(
            SpikeRateMatrixI rateMatrix, Interval interval, int ind) {

        int old_column = rateMatrix.getCurrentColumn();

        rateMatrix.setCurrentTime(interval.start());
        int cur_pos = rateMatrix.getCurrentColumn();
        int column = cur_pos + ind;

        rateMatrix.setCurrentColumn(old_column);
        return column;
    }*/


    @SuppressWarnings("deprecation")
    protected String formatWekaStr(String weka_str) {
        String header =
            "% -- File automatically generated by neurobjects.analysis at "
            + new Date().toGMTString() + " -- %\n"
            + "% -- Original setup file:\n%\n% ";

        String setup_str = setup.toString().replace("\n", "\n% ");
        return header + setup_str + "\n\n" + weka_str + "\n";
    }


    @Override
    public void setVerbose(boolean _verbose) {
        verbose = _verbose;
    }


    @Override
    public boolean getVerbose() {
        return verbose;
    }
}
