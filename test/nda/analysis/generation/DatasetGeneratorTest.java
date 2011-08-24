package nda.analysis.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.random.RandomDataImpl;
import org.junit.Before;
import org.junit.Test;

import weka.core.Instances;

import nda.analysis.PatternHandler;
import nda.data.BehaviorHandlerI;
import nda.data.Interval;
import nda.data.SpikeRateMatrixI;


/**
 * Tests for the DatasetGenerator abstract class.
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class DatasetGeneratorTest {

    // Make the test reproducible
    private static long RANDOM_SEED = 6667848180907283896L;

    // Uncomment the following block to test with a new seed
    /*static {
        RANDOM_SEED = new Random().nextLong();
        System.out.println("RANDOM_SEED = " + RANDOM_SEED);
    }*/


    // Needed in order to test an abstract class with concrete private methods
    private static class MockDatasetGenerator extends DatasetGenerator {
        private MockDatasetGenerator(GeneratorSetup setup) {
            super(setup);

            RandomDataImpl random = (RandomDataImpl) randomData;
            random.reSeed(RANDOM_SEED);
        }

        @Override
        public void generate() throws GenerationException { }
    }


    private MockDatasetGenerator generator;
    private MockDatasetGenerator short_gen;
    private static String setupFilepath = "data/test/test_setup.yml";
    private static String shortSetupFilepath = "data/test/short_setup.yml";


    @Before
    public void setUp() throws Exception {
        GeneratorSetup setup = new GeneratorSetup(setupFilepath);
        generator = new MockDatasetGenerator(setup);

        GeneratorSetup short_setup = new GeneratorSetup(shortSetupFilepath);
        short_gen = new MockDatasetGenerator(short_setup);
    }


    /**
     * Test method for {@link nda.analysis.generation.DatasetGenerator#loadHandlers()}.
     */
    @Test
    public void testLoadSpikeHandlerI() throws Exception {
        generator.loadHandlers();

        assertNotNull(generator.globalSpikeHandler);
        assertEquals(10, generator.globalSpikeHandler.getNumberOfSpikeTrains());

        assertNotNull(generator.behaviorHandler);
        assertEquals(4, generator.behaviorHandler.getLabelSet().size());
    }


    @Test
    public void testSampleBallInstances() throws Exception {
        generator.loadHandlers();

        List<GeneratorSetup.Dataset> datasets = generator.setup.getDatasets();

        GeneratorSetup.Dataset ball_ds = datasets.get(0);
        GeneratorSetup.Class yesClass = ball_ds.getClasses().get(0);
        SpikeRateMatrixI rateMatrix = generator.buildDatasetRateMatrix(ball_ds);

        assertEquals("ge4_ball_p1", ball_ds.getName());
        assertEquals("yes", yesClass.getName());
        assertEquals(1, yesClass.getLabels().size());

        String yesLabel = yesClass.getLabels().get(0);
        assertEquals("ball", yesLabel);
        assertEquals(1, generator.behaviorHandler.getIntervals(yesLabel).size());

        Interval ball_interval = generator.behaviorHandler.getIntervals(yesLabel).get(0);

        int numPatterns = rateMatrix.numPatterns(ball_interval);
        int numSamples = numPatterns / 2;

        assertTrue(numPatterns > 0);
        assertTrue(numSamples > 0);

        int[] sample_indexes = generator.sampleTrainTest(numPatterns, numSamples, 0)[0];

        Map<String, List<Integer>> sample = generator.sampleInstancesColumns(
                rateMatrix, yesClass, sample_indexes);

        assertEquals(1, sample.keySet().size());

        List<Integer> sample_cols = sample.get(yesLabel);
        assertEquals(numSamples, sample_cols.size());

        for (Integer column : sample_cols) {
            int width = rateMatrix.getWindowWidth();
            int old_column = rateMatrix.getCurrentColumn();

            assertTrue(column >= 0 && column < rateMatrix.numColumns());
            assertTrue(column + width <= rateMatrix.numColumns());

            Interval ball_interval_test = Interval.make(
                    ball_interval.start() - rateMatrix.getBinSize(),
                    ball_interval.end() + rateMatrix.getBinSize()
            );

            rateMatrix.setCurrentColumn(column);
            assertTrue(ball_interval_test.contains(rateMatrix.getCurrentTime()));
            rateMatrix.setCurrentColumn(column + width - 1);
            assertTrue(ball_interval_test.contains(rateMatrix.getCurrentTime()));
            rateMatrix.setCurrentColumn(old_column);
        }
    }


    /**
     * Test method for {@link nda.analysis.generation.DatasetGenerator#sampleInstances(nda.data.SpikeRateMatrixI, nda.analysis.generation.GeneratorSetup.Class, int)}.
     */
    @Test
    public void testSampleInstances1vsN() throws Exception {
        generator.loadHandlers();
        BehaviorHandlerI behaviorHandler = generator.behaviorHandler;

        for (GeneratorSetup.Dataset dataset : generator.setup.getDatasets()) {
            List<GeneratorSetup.Class> classes = dataset.getClasses();

            if (classes.size() != 2) {
                // only test 1_vs_n datasets in this method
                continue;
            }

            GeneratorSetup.Class yesClass = classes.get(0);
            GeneratorSetup.Class noClass = classes.get(1);

            SpikeRateMatrixI rateMatrix = generator.buildDatasetRateMatrix(dataset);

            assertEquals("yes", yesClass.getName());
            assertEquals("no", noClass.getName());
            assertEquals(1, yesClass.getLabels().size());
            assertTrue(noClass.getLabels().size() >= 1);

            int numPatterns = 0;
            for (String label : noClass.getLabels())
                for (Interval interval : behaviorHandler.getIntervals(label))
                    numPatterns += rateMatrix.numPatterns(interval);

            int numSamplesTrain = noClass.getNumberTrainSamples();
            assertTrue(numPatterns > 0);
            assertTrue(numSamplesTrain > 0 && numSamplesTrain <= numPatterns);

            int[] sample_indexes = generator.sampleTrainTest(
                    numPatterns, numSamplesTrain, 0)[0];

            Map<String, List<Integer>> sample = generator.sampleInstancesColumns(
                    rateMatrix, noClass, sample_indexes);

            assertTrue(noClass.getLabels().containsAll(sample.keySet()));

            int generatedSamples = 0;

            for (Map.Entry<String,List<Integer>> entry : sample.entrySet()) {
                String usedLabel = entry.getKey();
                List<Integer> sample_cols = entry.getValue();

                generatedSamples += sample_cols.size();

                for (Integer column : sample_cols) {
                    int width = rateMatrix.getWindowWidth();
                    int old_column = rateMatrix.getCurrentColumn();

                    assertTrue(column >= 0 && column < rateMatrix.numColumns());
                    assertTrue(column + width <= rateMatrix.numColumns());

                    rateMatrix.setCurrentColumn(column);

                    double columnTime = rateMatrix.getCurrentTime();
                    assertBelongsToLabel(usedLabel, columnTime, rateMatrix.getBinSize());

                    boolean found = false;

                    for (Interval interval : behaviorHandler.getIntervals(usedLabel)) {
                        // TODO: document the reason for this
                        Interval test_interval = Interval.make(
                                interval.start() - rateMatrix.getBinSize(),
                                interval.end() + rateMatrix.getBinSize()
                        );

                        if (test_interval.contains(columnTime)) {
                            found = true;

                            rateMatrix.setCurrentColumn(column+width-1);
                            double endColumnTime = rateMatrix.getCurrentTime();
                            assertTrue(test_interval.contains(endColumnTime));

                            assertBelongsToLabel(
                                    usedLabel, endColumnTime,
                                    rateMatrix.getBinSize());
                        }
                    }

                    assertTrue(found);
                    rateMatrix.setCurrentColumn(old_column);
                }
            }

            assertEquals(numSamplesTrain, generatedSamples);
        }
    }



    /**
     * Test method for {@link nda.analysis.generation.DatasetGenerator#addInstancesFromClass(nda.analysis.PatternHandler, nda.data.SpikeRateMatrixI, nda.analysis.generation.GeneratorSetup.Class, int)}.
     */
    @Test
    public void testAddInstancesFromClass() throws Exception {
        generator.loadHandlers();

        for (GeneratorSetup.Dataset dataset : generator.setup.getDatasets()) {
            SpikeRateMatrixI rateMatrix = generator.buildDatasetRateMatrix(dataset);

            Set<String> classNames = new HashSet<String>();
            for (GeneratorSetup.Class class_attr : dataset.getClasses())
                classNames.add(class_attr.getName());

            for (GeneratorSetup.Class class_attr : dataset.getClasses()) {
                PatternHandler trainSet = new PatternHandler(
                        "train", rateMatrix, classNames);

                PatternHandler testSet = new PatternHandler(
                        "test", rateMatrix, classNames);

                generator.addInstancesFromClass(class_attr, rateMatrix,
                        trainSet, testSet);

                assertTrue(classNames.containsAll(trainSet.getLabelSet()));
                assertTrue(classNames.containsAll(testSet.getLabelSet()));

                Instances trainRel = trainSet.getRelation();
                Instances testRel = testSet.getRelation();

                assertEquals(class_attr.getNumberTrainSamples(), trainRel.numInstances());
                assertEquals(class_attr.getNumberTestSamples(), testRel.numInstances());

                for (int i = 0; i < trainRel.numInstances(); ++i) {
                    for (int j = 0; j < testRel.numInstances(); ++j) {
                        double[] pA = trainRel.instance(i).toDoubleArray();
                        double[] pB = testRel.instance(j).toDoubleArray();

                        assertEquals(pA.length, pB.length);
                        assertNotEquals(pA, pB);
                    }
                }
            }
        }
    }


    /**
     * Test method for {@link nda.analysis.generation.DatasetGenerator#buildDatasetSingleRound(nda.analysis.generation.GeneratorSetup.Dataset, int)}.
     */
    @Test
    public void testBuildDatasetSingleRound() throws Exception {
        generator.loadHandlers();

        for (GeneratorSetup.Dataset dataset : generator.setup.getDatasets()) {
            Set<String> setNames = new HashSet<String>();

            for (int round = 0; round < 10; ++round) {
                List<PatternHandler> sets = generator.buildDatasetSingleRound(
                        dataset, round);

                assertEquals(2, sets.size());
                PatternHandler trainSet = sets.get(0);
                PatternHandler testSet = sets.get(1);

                setNames.add(trainSet.getRelation().relationName());
                setNames.add(testSet.getRelation().relationName());

                for (GeneratorSetup.Class class_attr : dataset.getClasses()) {
                    String class_label = class_attr.getName();

                    List<double[]> trainPatterns = trainSet.getPatterns(class_label);
                    List<double[]> testPatterns = testSet.getPatterns(class_label);

                    assertEquals(class_attr.getNumberTrainSamples(), trainPatterns.size());
                    assertEquals(class_attr.getNumberTestSamples(), testPatterns.size());

                    assertEquals(trainSet.getLabelSet(), testSet.getLabelSet());
                    assertEquals(trainSet.getDimension(), testSet.getDimension());
                }
            }

            assertEquals(20, setNames.size());
        }
    }


    /**
     * Test method for {@link nda.analysis.generation.DatasetGenerator#buildDataset(nda.analysis.generation.GeneratorSetup.Dataset)}.
     */
    @Test
    public void testBuildDataset() throws Exception {
        generator.loadHandlers();

        for (GeneratorSetup.Dataset dataset : generator.setup.getDatasets()) {
            Set<String> setNames = new HashSet<String>();

            List<PatternHandler> sets = generator.buildDataset(dataset);
            assertEquals(2 * dataset.getNumberRounds(), sets.size());

            for (int i = 0; i < sets.size()-1; i += 2) {
                PatternHandler trainSet = sets.get(i);
                PatternHandler testSet = sets.get(i+1);

                String  trainSetName = trainSet.getRelation().relationName();
                String testSetName = testSet.getRelation().relationName();

                assertTrue(trainSetName.contains("train"));
                assertTrue(testSetName.contains("test"));
                assertTrue(trainSetName.contains(dataset.getName()));
                assertTrue(testSetName.contains(dataset.getName()));

                setNames.add(trainSetName);
                setNames.add(testSetName);

                for (GeneratorSetup.Class class_attr : dataset.getClasses()) {
                    String class_label = class_attr.getName();

                    List<double[]> trainPatterns = trainSet.getPatterns(class_label);
                    List<double[]> testPatterns = testSet.getPatterns(class_label);

                    assertEquals(class_attr.getNumberTrainSamples(), trainPatterns.size());
                    assertEquals(class_attr.getNumberTestSamples(), testPatterns.size());

                    assertEquals(trainSet.getLabelSet(), testSet.getLabelSet());
                    assertEquals(trainSet.getDimension(), testSet.getDimension());
                }
            }

            assertEquals(sets.size(), setNames.size());
        }
    }


    @Test
    public void testClassesLabels() {
        for (GeneratorSetup.Dataset dataset : generator.setup.getDatasets()) {
            Set<String> class_labels = new HashSet<String>();

            int count = 0;
            for (GeneratorSetup.Class class_attr : dataset.getClasses()) {
                class_labels.addAll(class_attr.getLabels());
                count += class_attr.getLabels().size();
            }

            assertEquals(count, class_labels.size());
        }
    }


    /**
     * Test method for {@link nda.analysis.generation.DatasetGenerator#sampleTrainTest(int, int)}.
     */
    @Test
    public void testSampleTrainTest() {
        int[] n_list = new int[] { 0, 1, 1, 3, 3, 10, 17, 20, 99, 100000 };
        int[] train_l = new int[] { 0, 1, 0, 1, 2, 0, 17, 13, 37, 53123 };
        int[] test_l = new int[] { 0, 0, 1, 1, 0, 5, 0, 4, 99-37, 11321 };

        for (int i = 0; i < n_list.length; ++i) {
            int n = n_list[i];
            int train = train_l[i];
            int test = test_l[i];

            int[][] inds = generator.sampleTrainTest(n, train, test);

            List<Integer> train_inds = Arrays.asList(ArrayUtils.toObject(inds[0]));
            List<Integer> test_inds = Arrays.asList(ArrayUtils.toObject(inds[1]));

            assertTrue(train + test <= n);
            assertEquals(train, train_inds.size());
            assertEquals(test, test_inds.size());

            Set<Integer> all = new HashSet<Integer>();
            all.addAll(train_inds);
            all.addAll(test_inds);

            // no repetitions
            assertEquals(train+test, all.size());

            for (Integer j : all)
                assertTrue(j >= 0 && j < n);
        }
    }


    @Test(expected = GenerationException.class)
    public void testPatternsSizeError() throws Exception {
        short_gen.loadHandlers();

        for (GeneratorSetup.Dataset dataset : short_gen.setup.getDatasets())
            short_gen.buildDataset(dataset);
    }


    private void assertNotEquals(double[] a, double[] b) {
        if (a.length != b.length)
            return;

        boolean diff = false;
        for (int i = 0; i < a.length && !diff; ++i)
            diff = Double.compare(a[i], b[i]) != 0;

        assertTrue(diff);
    }


    private void assertBelongsToLabel(String label, double time, double binSize) {
        boolean any = false;

        for (int i = -1; i <= 1 && !any; ++i) {
            double test_time = time + i * binSize;
            any = label.equals(generator.behaviorHandler.getLabel(test_time));
        }

        assertTrue(any);
    }
}
