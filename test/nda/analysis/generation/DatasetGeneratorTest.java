package nda.analysis.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.MessageDigest;
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
    private static long RANDOM_SEED = 8146340101830722149L;

    // Should be updated when the seed above changes or any of the
    // generators are modified. This one was based on commit 3049aba.
    byte[] INTEGRITY_HASH = {-50,70,-26,119,-98,-2,-73,105,113,-78,93,-118,-96,-21,-33,-18};

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

    private static String setupFilepath = "data/test/test_setup.yml";
    private static String shortSetupFilepath = "data/test/short_setup.yml";
    private static String bugSetupFilepath = "data/test/bug_setup.yml";
    private static String dropSetupFilepath = "data/test/test_dropping.yml";
    private static String surrogateSetupFilepath = "data/test/test_uniform_surrogates.yml";
    private static String poissonSurSetupFilepath = "data/test/test_poisson_surrogates.yml";
    private static String colSwapSurSetupFilepath = "data/test/test_col_swap.yml";
    private static String neuronSwapSurSetupFilepath = "data/test/test_neuron_swap.yml";
    private static String matrixSwapSurSetupFilepath = "data/test/test_matrix_swap.yml";

    private MockDatasetGenerator generator;
    private MockDatasetGenerator short_gen;
    private MockDatasetGenerator bug_generator;
    private MockDatasetGenerator drop_generator;
    private MockDatasetGenerator surrogate_gen;
    private MockDatasetGenerator poisson_sur_gen;
    private MockDatasetGenerator col_swap_sur_gen;
    private MockDatasetGenerator neuron_swap_sur_gen;
    private MockDatasetGenerator matrix_swap_sur_gen;


    @Before
    public void setUp() throws Exception {
        GeneratorSetup setup = new GeneratorSetup(setupFilepath);
        generator = new MockDatasetGenerator(setup);

        GeneratorSetup short_setup = new GeneratorSetup(shortSetupFilepath);
        short_gen = new MockDatasetGenerator(short_setup);

        GeneratorSetup bug_setup = new GeneratorSetup(bugSetupFilepath);
        bug_generator = new MockDatasetGenerator(bug_setup);

        GeneratorSetup drop_setup = new GeneratorSetup(dropSetupFilepath);
        drop_generator = new MockDatasetGenerator(drop_setup);

        GeneratorSetup sur_setup = new GeneratorSetup(surrogateSetupFilepath);
        surrogate_gen = new MockDatasetGenerator(sur_setup);

        GeneratorSetup poisson_setup = new GeneratorSetup(poissonSurSetupFilepath);
        poisson_sur_gen = new MockDatasetGenerator(poisson_setup);

        GeneratorSetup col_swap_setup = new GeneratorSetup(colSwapSurSetupFilepath);
        col_swap_sur_gen = new MockDatasetGenerator(col_swap_setup);

        GeneratorSetup neuron_swap_setup = new GeneratorSetup(neuronSwapSurSetupFilepath);
        neuron_swap_sur_gen = new MockDatasetGenerator(neuron_swap_setup);

        GeneratorSetup matrix_swap_setup = new GeneratorSetup(matrixSwapSurSetupFilepath);
        matrix_swap_sur_gen = new MockDatasetGenerator(matrix_swap_setup);
    }


    /**
     * Test method for {@link nda.analysis.generation.DatasetGenerator#loadHandlers()}.
     */
    @Test
    public void testLoadSpikeHandlerI() throws Exception {
        generator.loadHandlers();

        assertNotNull(generator.globalSpikeHandler);
        assertEquals(10, generator.globalSpikeHandler.getNumberOfSpikeTrains());

        assertNotNull(generator.globalBehaviorHandler);
        assertEquals(4, generator.globalBehaviorHandler.getLabelSet().size());
    }


    /**
     * Test method for {@link nda.analysis.generation.DatasetGenerator#addInstancesFromClass(nda.analysis.PatternHandler, nda.data.SpikeRateMatrixI, nda.analysis.generation.GeneratorSetup.Class, int)}.
     */
    @Test
    public void testAddInstancesFromClass() throws Exception {
        testAddInstancesFromClass(bug_generator);
        testAddInstancesFromClass(generator);
    }


    public static void testAddInstancesFromClass(MockDatasetGenerator generator) throws Exception {
        generator.loadHandlers();

        for (GeneratorSetup.Dataset dataset : generator.setup.getDatasets()) {
            SpikeRateMatrixI rateMatrix = generator.getDatasetRateMatrix(dataset);

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
                    assertPatternBelongsToClass(generator, rateMatrix, class_attr, pA);
                }

                for (int i = 0; i < testRel.numInstances(); ++i) {
                    double[] pB = testSet.getPattern(i);
                    assertPatternBelongsToClass(generator, rateMatrix, class_attr, pB);
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
            SpikeRateMatrixI rateMatrix = generator.getDatasetRateMatrix(dataset);

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


    @Test
    public void testNeuronDropDatasets() throws Exception {
        drop_generator.loadHandlers();

        assertEquals(12, drop_generator.setup.getDatasets().size());
        assertEquals(10, drop_generator.globalSpikeHandler.getNumberOfSpikeTrains());

        for (GeneratorSetup.Dataset dataset : drop_generator.setup.getDatasets()) {
            assertNotNull(dataset.getParameter("neuron_drop"));
            assertNotNull(dataset.getParameter("num_drop"));

            int num_drop = (Integer) dataset.getParameter("num_drop");
            assertTrue(dataset.getName().contains("d" + num_drop));

            for (PatternHandler set : drop_generator.buildDataset(dataset)) {
                assertEquals(10 * (4 - num_drop), set.getDimension());
            }
        }
    }


    @Test
    public void testUniformSurrogateDatasets() throws Exception {
        surrogate_gen.loadHandlers();

        assertEquals(39, surrogate_gen.setup.getDatasets().size());
        assertEquals(10, surrogate_gen.globalSpikeHandler.getNumberOfSpikeTrains());

        for (GeneratorSetup.Dataset dataset : surrogate_gen.setup.getDatasets()) {
            assertNotNull(dataset.getParameter("surrogate"));
            assertNotNull(dataset.getParameter("num_surrogate"));
            assertEquals("uniform", dataset.getParameter("surrogate_type"));

            int num_surrogate = (Integer) dataset.getParameter("num_surrogate");
            assertTrue(dataset.getName().contains("sur_uniform" + num_surrogate));

            for (PatternHandler set : surrogate_gen.buildDataset(dataset)) {
                if (dataset.getParameter("areas").equals("hp") ||
                        dataset.getParameter("areas").equals("s1"))
                    assertEquals(30, set.getDimension());
                else
                    assertEquals(40, set.getDimension());
            }
        }
    }


    @Test
    public void testPoissonSurrogateDatasets() throws Exception {
        poisson_sur_gen.loadHandlers();

        assertEquals(39, poisson_sur_gen.setup.getDatasets().size());
        assertEquals(10, poisson_sur_gen.globalSpikeHandler.getNumberOfSpikeTrains());

        for (GeneratorSetup.Dataset dataset : poisson_sur_gen.setup.getDatasets()) {
            assertNotNull(dataset.getParameter("surrogate"));
            assertNotNull(dataset.getParameter("num_surrogate"));
            assertEquals("poisson", dataset.getParameter("surrogate_type"));

            int num_surrogate = (Integer) dataset.getParameter("num_surrogate");
            assertTrue(dataset.getName().contains("sur_poisson" + num_surrogate));

            for (PatternHandler set : poisson_sur_gen.buildDataset(dataset)) {
                if (dataset.getParameter("areas").equals("hp") ||
                        dataset.getParameter("areas").equals("s1"))
                    assertEquals(30, set.getDimension());
                else
                    assertEquals(40, set.getDimension());
            }
        }
    }


    @Test
    public void testColSwapSurrogateDatasets() throws Exception {
        col_swap_sur_gen.loadHandlers();

        assertEquals(27, col_swap_sur_gen.setup.getDatasets().size());
        assertEquals(10, col_swap_sur_gen.globalSpikeHandler.getNumberOfSpikeTrains());

        for (GeneratorSetup.Dataset dataset : col_swap_sur_gen.setup.getDatasets()) {
            assertNotNull(dataset.getParameter("surrogate"));
            assertNotNull(dataset.getParameter("pct_surrogate"));
            assertEquals("col_swap", dataset.getParameter("surrogate_type"));
            assertTrue(dataset.getName().contains("sur_col_swap"));

            for (PatternHandler set : col_swap_sur_gen.buildDataset(dataset)) {
                if (dataset.getParameter("areas").equals("hp") ||
                        dataset.getParameter("areas").equals("s1"))
                    assertEquals(30, set.getDimension());
                else
                    assertEquals(40, set.getDimension());
            }
        }
    }


    @Test
    public void testNeuronSwapSurrogateDatasets() throws Exception {
        neuron_swap_sur_gen.loadHandlers();

        assertEquals(39, neuron_swap_sur_gen.setup.getDatasets().size());
        assertEquals(10, neuron_swap_sur_gen.globalSpikeHandler.getNumberOfSpikeTrains());

        for (GeneratorSetup.Dataset dataset : neuron_swap_sur_gen.setup.getDatasets()) {
            assertNotNull(dataset.getParameter("surrogate"));
            assertNotNull(dataset.getParameter("num_surrogate"));
            assertNotNull(dataset.getParameter("pct_surrogate"));
            assertEquals("neuron_swap", dataset.getParameter("surrogate_type"));
            assertTrue(dataset.getName().contains("sur_neuron_swap"));

            for (PatternHandler set : neuron_swap_sur_gen.buildDataset(dataset)) {
                if (dataset.getParameter("areas").equals("hp") ||
                        dataset.getParameter("areas").equals("s1"))
                    assertEquals(30, set.getDimension());
                else
                    assertEquals(40, set.getDimension());
            }
        }
    }


    @Test
    public void testMatrixSwapSurrogateDatasets() throws Exception {
        matrix_swap_sur_gen.loadHandlers();

        assertEquals(27, matrix_swap_sur_gen.setup.getDatasets().size());
        assertEquals(10, matrix_swap_sur_gen.globalSpikeHandler.getNumberOfSpikeTrains());

        for (GeneratorSetup.Dataset dataset : matrix_swap_sur_gen.setup.getDatasets()) {
            assertNotNull(dataset.getParameter("surrogate"));
            assertNotNull(dataset.getParameter("pct_surrogate"));
            assertEquals("matrix_swap", dataset.getParameter("surrogate_type"));
            assertTrue(dataset.getName().contains("sur_matrix_swap"));

            for (PatternHandler set : matrix_swap_sur_gen.buildDataset(dataset)) {
                if (dataset.getParameter("areas").equals("hp") ||
                        dataset.getParameter("areas").equals("s1"))
                    assertEquals(30, set.getDimension());
                else
                    assertEquals(40, set.getDimension());
            }
        }
    }


    @Test
    public void testIntegrity() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        DatasetGenerator[] generators = {
                generator, drop_generator, surrogate_gen,
                poisson_sur_gen, col_swap_sur_gen,
                neuron_swap_sur_gen, matrix_swap_sur_gen
        };

        for (DatasetGenerator generator : generators) {
            generator.loadHandlers();

            for (GeneratorSetup.Dataset dataset : generator.setup.getDatasets()) {
                for (PatternHandler data : generator.buildDataset(dataset)) {
                    String str = data.getRelation().toString();
                    digest.update(str.getBytes("UTF-8"));
                }
            }
        }

        byte[] hash = digest.digest();
        assertTrue(nda.util.ArrayUtils.equals(INTEGRITY_HASH, hash));
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
                for (Interval interval : generator.globalBehaviorHandler.getContactIntervals(label))
                    for (double[] p2 : rateMatrix.getPatterns(interval))
                        if (nda.util.ArrayUtils.equals(pattern, p2)) {
                            any = true;
                            break loop;
                        }

        assertTrue(any);
    }
}
