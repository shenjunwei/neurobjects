package nda.analysis;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;

import nda.data.BehaviorHandlerI;
import nda.data.CountMatrix;
import nda.data.Interval;
import nda.data.SpikeHandlerI;
import nda.data.SpikeRateMatrixI;
import nda.data.text.TextBehaviorHandler;
import nda.data.text.TextSpikeHandler;


/**
 * Dataset generator component.
 * 
 * Reads settings from a setup config file and generates Datasets
 * in an implementation defined way.
 * 
 * @author Giuliano Vilela
 */
public abstract class DatasetGenerator {
    protected Setup setup;
    protected SpikeHandlerI spikeHandler;
    protected BehaviorHandlerI behaviorHandler;
    protected RandomData randomData;


    public DatasetGenerator(String setupFilepath)
    throws FileNotFoundException, InvalidSetupFileException {
        this(new Setup(setupFilepath));
    }


    public DatasetGenerator(Setup _setup) {
        setup = _setup;
        randomData = new RandomDataImpl();
    }


    public abstract void generate() throws DatasetGenerationException;


    /**
     * Builds all rounds for a single dataset.
     * 
     * This method generates dataset.getNumberRounds() * 2 PatternHandler's.
     * Each round has a train relation and a test relation.
     */
    protected List<PatternHandler> buildDatasetAll(Setup.Dataset dataset)
    throws DatasetGenerationException {

        loadSpikeHandlerI();
        loadBehaviorHandlerI();

        int estimate = dataset.getNumberRounds() * 2;
        List<PatternHandler> patterns = new ArrayList<PatternHandler>(estimate);

        for (int round = 1; round <= dataset.getNumberRounds(); ++round)
            patterns.addAll(buildDatasetSingleRound(dataset, round));

        return patterns;
    }


    /**
     * Builds a Dataset from a single round.
     * 
     * This method generates 2 PatternHandler's (train and test).
     */
    protected List<PatternHandler> buildDatasetSingleRound(
            Setup.Dataset dataset, int round)
            throws DatasetGenerationException {

        SpikeRateMatrixI rateMatrix = buildDatasetRateMatrix(dataset);

        Set<String> labels = new HashSet<String>();
        for (Setup.Class class_attr : dataset.getClasses())
            labels.add(class_attr.getName());

        String dataset_name = dataset.getName();
        String trainSetName = String.format(
                "%s_%02d_%s", dataset_name, round, "train");
        String testSetName = String.format(
                "%s_%02d_%s", dataset_name, round, "test");

        PatternHandler trainSet = new PatternHandler(trainSetName, rateMatrix, labels);
        PatternHandler testSet = new PatternHandler(testSetName, rateMatrix, labels);

        for (Setup.Class class_attr : dataset.getClasses()) {
            addInstancesFromClass(class_attr, rateMatrix, trainSet, testSet);
        }

        List<PatternHandler> sets = new ArrayList<PatternHandler>(2);
        sets.add(trainSet);
        sets.add(testSet);
        return sets;
    }


    /**
     * s
     * 
     * @param patternHandler
     * @param rateMatrix
     * @param class_attr
     * @param number
     */
    protected void addInstancesFromClass(
            Setup.Class class_attr,
            SpikeRateMatrixI rateMatrix,
            PatternHandler trainSet, PatternHandler testSet) {

        int totalPatterns = 0;
        for (String label : class_attr.getLabels())
            for (Interval interval : behaviorHandler.getIntervals(label))
                totalPatterns += rateMatrix.numPatterns(interval);

        int[][] inds = sampleTrainTest(totalPatterns,
                class_attr.getNumberTrainSamples(),
                class_attr.getNumberTestSamples());

        int[] trainInds = inds[0];
        int[] testInds = inds[1];

        Map<String, List<Integer>> trainColumns = sampleInstancesColumns(
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
        }
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
    protected Map<String, List<Integer>> sampleInstancesColumns(
            SpikeRateMatrixI rateMatrix,
            Setup.Class class_attr, int[] sample_indexes) {

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
    }


    protected void loadSpikeHandlerI() throws DatasetGenerationException {
        try {
            String spikeDir = setup.getSpikesDirectory();
            spikeHandler = new TextSpikeHandler(spikeDir);
        } catch (Exception e) {
            throw new DatasetGenerationException(e);
        }
    }


    protected void loadBehaviorHandlerI() throws DatasetGenerationException {
        try {
            String behaviorFilepath = setup.getContactsFilepath();
            behaviorHandler = new TextBehaviorHandler(behaviorFilepath);
        } catch (Exception e) {
            throw new DatasetGenerationException(e);
        }
    }


    @SuppressWarnings("unchecked")
    protected SpikeRateMatrixI buildDatasetRateMatrix(Setup.Dataset dataset)
    throws DatasetGenerationException {
        List<String> filterList = (List<String>) dataset.getParameter("neurons");
        String neuronFilter = StringUtils.join(filterList, ", ");

        double binSize = (Double) dataset.getParameter("bin_size");
        int window_step = (Integer) dataset.getParameter("window_step");
        int window_width = (Integer) dataset.getParameter("window_width");

        try {
            spikeHandler.setFilter(neuronFilter);
        } catch (Exception e) {
            throw new DatasetGenerationException(e);
        }

        SpikeRateMatrixI rateMatrix = new CountMatrix(spikeHandler, binSize);
        rateMatrix.setStep(window_step);
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


    private static int getPatternColumn(
            SpikeRateMatrixI rateMatrix, Interval interval, int ind) {

        int old_column = rateMatrix.getCurrentColumn();

        rateMatrix.setCurrentTime(interval.start());
        int cur_pos = rateMatrix.getCurrentColumn();
        int step = rateMatrix.getStep();
        int column = cur_pos + ind * step;

        rateMatrix.setCurrentColumn(old_column);
        return column;
    }
}
