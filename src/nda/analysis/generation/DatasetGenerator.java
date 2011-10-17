package nda.analysis.generation;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import nda.util.LRUCache;
import nda.util.RandomUtils;
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

    private static final int DEFAULT_RATE_MATRIX_CACHE_SIZE = 10;

    protected boolean verbose;
    protected RandomData randomData;

    protected GeneratorSetup setup;
    protected SpikeHandlerI globalSpikeHandler;
    protected BehaviorHandlerI globalBehaviorHandler;
    protected Map<Integer,CountMatrix> globalCountMatrixCache;


    public DatasetGenerator(String setupFilepath)
    throws FileNotFoundException, InvalidSetupFileException {
        this(new GeneratorSetup(setupFilepath));
    }


    public DatasetGenerator(GeneratorSetup _setup) {
        setup = _setup;
        randomData = new RandomDataImpl();
    }


    public abstract void generate() throws GenerationException;


    public void loadHandlers() throws GenerationException {
        try {
            String spikeDir = setup.getSpikesDirectory();
            globalSpikeHandler = new TextSpikeHandler(spikeDir);

            String behaviorFilepath = setup.getContactsFilepath();
            globalBehaviorHandler = new TextBehaviorHandler(behaviorFilepath);

            globalCountMatrixCache = new LRUCache<Integer, CountMatrix>(
                    DEFAULT_RATE_MATRIX_CACHE_SIZE);
        } catch (Exception e) {
            throw new GenerationException(e);
        }
    }


    /**
     * Builds all rounds for a single dataset.
     * 
     * This method generates dataset.getNumberRounds() * 2 PatternHandler's.
     * Each round has a train relation and a test relation.
     */
    public List<PatternHandler> buildDataset(GeneratorSetup.Dataset dataset)
    throws GenerationException {

        int estimate = dataset.getNumberRounds() * 2;
        List<PatternHandler> patterns = new ArrayList<PatternHandler>(estimate);

        // assumes that there is only one surrogate type per dataset
        if (DatasetTransformer.needsSpikeTrainTransform(dataset)) {

            for (int round = 1; round <= dataset.getNumberRounds(); ++round) {
                CountMatrix roundMatrix = getDatasetRateMatrix(dataset);
                patterns.addAll(buildDatasetSingleRound(dataset, round, roundMatrix));
            }
        }
        else {
            // Usa o globalSpikeHandler do generator, criado em loadHandlers
            CountMatrix datasetMatrix = getDatasetRateMatrix(dataset);

            for (int round = 1; round <= dataset.getNumberRounds(); ++round) {
                CountMatrix roundMatrix;

                if (DatasetTransformer.needsRateMatrixTransform(dataset))
                    roundMatrix = DatasetTransformer.applyRateMatrixTransform(
                            randomData, datasetMatrix, dataset);
                else
                    roundMatrix = datasetMatrix;

                // usa o globalBehaviorHandler do generator
                patterns.addAll(buildDatasetSingleRound(dataset, round, roundMatrix));
            }
        }

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

        int numLabels = class_attr.getLabels().size();
        int totalTrain = class_attr.getNumberTrainSamples();
        int totalTest = class_attr.getNumberTestSamples();

        int numTrainPerLabel = totalTrain / numLabels;
        int numTrainResidual = totalTrain % numLabels;

        int numTestPerLabel = totalTest / numLabels;
        int numTestResidual = totalTest % numLabels;

        List<String> labels = class_attr.getLabels();

        Object[] residualTrainLabels = RandomUtils.randomSample(
                randomData, labels, numTrainResidual
        );

        Object[] residualTestLabels = RandomUtils.randomSample(
                randomData, labels, numTestResidual
        );

        for (String label : labels) {
            List<double[]> patterns = new ArrayList<double[]>();

            for (Interval interval : globalBehaviorHandler.getContactIntervals(label))
                patterns.addAll(rateMatrix.getPatterns(interval));

            int numTrain = numTrainPerLabel;
            int numTest = numTestPerLabel;
            int totalForLabel = patterns.size();

            if (ArrayUtils.contains(residualTrainLabels, label)) numTrain++;
            if (ArrayUtils.contains(residualTestLabels, label)) numTest++;

            if (numTrain + numTest > totalForLabel) {
                throw new GenerationException(String.format(
                        "Can't sample %d train patterns and %d test patterns from " +
                        "label %s of dataset %s (it only has %d patterns)",
                        numTrain, numTest, label,
                        class_attr.getDataset().getName(), totalForLabel));
            }

            int[][] inds = sampleTrainTest(totalForLabel, numTrain, numTest);
            int[] trainInds = inds[0];
            int[] testInds = inds[1];

            for (int i : trainInds) {
                double[] pattern = patterns.get(i);
                trainSet.addPattern(pattern, class_attr.getName());
            }

            for (int i : testInds) {
                double[] pattern = patterns.get(i);
                testSet.addPattern(pattern, class_attr.getName());
            }
        }
    }


    protected CountMatrix getDatasetRateMatrix(GeneratorSetup.Dataset dataset)
    throws GenerationException {

        boolean hasSpikeTransform = DatasetTransformer.needsSpikeTrainTransform(dataset);

        int params_id = dataset.getParameterChoiceId();
        CountMatrix rateMatrix = globalCountMatrixCache.get(params_id);

        if (hasSpikeTransform || rateMatrix == null) {
            String neuronFilter = (String) dataset.getParameter("areas");
            SpikeHandlerI datasetHandler = globalSpikeHandler.withFilter(neuronFilter);

            if (hasSpikeTransform) {
                datasetHandler = DatasetTransformer.applySpikeTrainTransform(
                        randomData, (TextSpikeHandler) datasetHandler, dataset);
            }

            double binSize = (Double) dataset.getParameter("bin_size");
            int window_width = (Integer) dataset.getParameter("window_width");

            rateMatrix = new CountMatrix(datasetHandler, binSize);
            rateMatrix.setWindowWidth(window_width);

            if (!hasSpikeTransform) {
                globalCountMatrixCache.put(params_id, rateMatrix);
            }
        }

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

        return new int[][] { train_inds, test_inds };
    }


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
