package nda.analysis.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    private MockDatasetGenerator ge4_generator;
    private MockDatasetGenerator bug_generator;
    private static String setupFilepath = "data/test/test_setup.yml";
    private static String shortSetupFilepath = "data/test/short_setup.yml";
    private static String realSetupFilepath = "data/real/ge4/ge4_setup.yml";
    private static String bugSetupFilepath = "data/test/bug_setup.yml";


    @Before
    public void setUp() throws Exception {
        GeneratorSetup setup = new GeneratorSetup(setupFilepath);
        generator = new MockDatasetGenerator(setup);

        GeneratorSetup short_setup = new GeneratorSetup(shortSetupFilepath);
        short_gen = new MockDatasetGenerator(short_setup);

        GeneratorSetup real_setup = new GeneratorSetup(realSetupFilepath);
        ge4_generator = new MockDatasetGenerator(real_setup);

        GeneratorSetup bug_setup = new GeneratorSetup(bugSetupFilepath);
        bug_generator = new MockDatasetGenerator(bug_setup);
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


    /**
     * Test method for {@link nda.analysis.generation.DatasetGenerator#addInstancesFromClass(nda.analysis.PatternHandler, nda.data.SpikeRateMatrixI, nda.analysis.generation.GeneratorSetup.Class, int)}.
     */
    @Test
    public void testAddInstancesFromClass() throws Exception {
        testAddInstancesFromClass(bug_generator);
        testAddInstancesFromClass(generator);
        testAddInstancesFromClass(ge4_generator);
    }


    public static void testAddInstancesFromClass(MockDatasetGenerator generator) throws Exception {
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
                    double[] pA = trainSet.getPattern(i);

                    for (int j = 0; j < testRel.numInstances(); ++j) {
                        double[] pB = testSet.getPattern(j);
                        assertEquals(pA.length, pB.length);
                        assertNotEquals(pA, pB);
                    }

                    assertPatternBelongsToClass(generator, rateMatrix, class_attr, pA);
                }

                for (int i = 0; i < testRel.numInstances(); ++i) {
                    double[] pB = testSet.getPattern(i);
                    assertPatternBelongsToClass(generator, rateMatrix, class_attr, pB);
                }
            }
        }
    }


    @Test
    public void testAddInstancesFromGE4Food() throws Exception {
        MockDatasetGenerator generator = ge4_generator;
        generator.loadHandlers();

        GeneratorSetup.Dataset food_ds = null;
        for (GeneratorSetup.Dataset dataset : generator.setup.getDatasets())
            if (dataset.getName().equals("ge4_food_p2")) {
                food_ds = dataset;
                break;
            }

        assertNotNull(food_ds);

        Set<String> classNames = new HashSet<String>();
        for (GeneratorSetup.Class class_attr : food_ds.getClasses())
            classNames.add(class_attr.getName());

        GeneratorSetup.Class yesClass = food_ds.getClasses().get(0);
        assertEquals(1, yesClass.getLabels().size());

        SpikeRateMatrixI rateMatrix = generator.buildDatasetRateMatrix(food_ds);
        PatternHandler trainSet = new PatternHandler("train", rateMatrix, classNames);
        PatternHandler testSet = new PatternHandler("test", rateMatrix, classNames);

        generator.addInstancesFromClass(yesClass, rateMatrix, trainSet, testSet);
        assertEquals(yesClass.getNumberTrainSamples(), trainSet.size());
        assertEquals(yesClass.getNumberTestSamples(), testSet.size());

        BehaviorHandlerI behavior = generator.behaviorHandler;
        List<Interval> intervals = behavior.getIntervals("food");
        assertEquals(14, intervals.size());

        List<double[]> patterns = new ArrayList<double[]>();
        patterns.addAll(trainSet.getPatterns("yes"));
        patterns.addAll(testSet.getPatterns("yes"));
        assertEquals(trainSet.size() + testSet.size(), patterns.size());

        for (double[] pattern : patterns)
            assertPatternBelongsToClass(generator, rateMatrix, yesClass, pattern);
    }



    /**
     * Test method for {@link nda.analysis.generation.DatasetGenerator#buildDatasetSingleRound(nda.analysis.generation.GeneratorSetup.Dataset, int)}.
     */
    @Test
    public void testBuildDatasetSingleRound() throws Exception {
        generator.loadHandlers();

        for (GeneratorSetup.Dataset dataset : generator.setup.getDatasets()) {
            Set<String> setNames = new HashSet<String>();
            SpikeRateMatrixI rateMatrix = generator.buildDatasetRateMatrix(dataset);

            for (int round = 0; round < 10; ++round) {
                List<PatternHandler> sets = generator.buildDatasetSingleRound(
                        dataset, round, rateMatrix);

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

                    for (double[] pA : trainPatterns) {
                        for (double[] pB : testPatterns) {
                            assertEquals(pA.length, pB.length);
                            assertNotEquals(pA, pB);
                        }
                        assertPatternBelongsToClass(generator, rateMatrix, class_attr, pA);
                    }

                    for (double[] pB : testPatterns)
                        assertPatternBelongsToClass(generator, rateMatrix, class_attr, pB);
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


    private static void assertNotEquals(double[] a, double[] b) {
        if (a.length != b.length)
            return;

        boolean diff = false;
        for (int i = 0; i < a.length && !diff; ++i)
            diff = Double.compare(a[i], b[i]) != 0;

        assertTrue(diff);
    }


    private static void assertPatternBelongsToClass(
            MockDatasetGenerator generator,
            SpikeRateMatrixI rateMatrix,
            GeneratorSetup.Class class_attr, double[] pattern) {

        boolean any = false;

        loop:
            for (String label : class_attr.getLabels())
                for (Interval interval : generator.behaviorHandler.getIntervals(label))
                    for (double[] p2 : rateMatrix.getPatterns(interval))
                        if (nda.util.ArrayUtils.equals(pattern, p2)) {
                            any = true;
                            break loop;
                        }

        assertTrue(any);
    }
}






