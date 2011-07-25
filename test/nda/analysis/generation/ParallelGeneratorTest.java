package nda.analysis.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.math.random.RandomDataImpl;
import org.junit.Before;
import org.junit.Test;

import weka.core.Instances;

import nda.analysis.PatternHandler;
import nda.analysis.Setup;


/**
 * Tests for the ParallelGenerator class.
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class ParallelGeneratorTest {

    private static long RANDOM_SEED = 1938663736661024855L;

    // Uncomment the following block to test with a new seed
    /*static {
        RANDOM_SEED = new Random().nextLong();
        System.out.println("RANDOM_SEED = " + RANDOM_SEED);
    }*/


    // Needed in order to test an abstract class with concrete private methods
    private static class MockParallelGenerator extends ParallelGenerator {
        private MockParallelGenerator(Setup setup) {
            super(setup);

            RandomDataImpl random = (RandomDataImpl) randomData;
            random.reSeed(RANDOM_SEED);
        }

        @Override
        public void generate() throws GenerationException { }
    }


    private MockParallelGenerator generator;
    private static String setupFilepath = "data/test/test_setup.yml";


    @Before
    public void setUp() throws Exception {
        Setup setup = new Setup(setupFilepath);
        generator = new MockParallelGenerator(setup);
    }


    /**
     * Test method for {@link nda.analysis.generation.ParallelGenerator#getExecutor()}.
     */
    @Test
    public void testGetExecutor() {
        assertNotNull(generator.getExecutor());
        assertFalse(generator.getExecutor().isShutdown());
        assertFalse(generator.getExecutor().isTerminated());
    }


    /**
     * Test method for {@link nda.analysis.generation.ParallelGenerator#buildAll(nda.analysis.Setup)}.
     */
    @Test
    public void testBuildAll() throws Exception {
        generator.loadHandlers();
        Setup setup = generator.setup;

        List<PatternHandler> expected_patterns = new ArrayList<PatternHandler>();
        List<PatternHandler> generated_patterns = new ArrayList<PatternHandler>();

        for (Setup.Dataset dataset : setup.getDatasets())
            expected_patterns.addAll(generator.buildDataset(dataset));

        List<Future<List<PatternHandler>>> results = generator.buildAll(generator.setup);
        for (Future<List<PatternHandler>> result : results)
            generated_patterns.addAll(result.get());

        assertEquals(expected_patterns.size(), generated_patterns.size());

        for (int i = 0; i < expected_patterns.size(); ++i) {
            Instances expected = expected_patterns.get(i).getRelation();
            Instances generated = expected_patterns.get(i).getRelation();

            assertTrue(expected.equalHeaders(generated));
            assertEquals(expected.numInstances(), generated.numInstances());
        }
    }


    /**
     * Test method for {@link nda.analysis.generation.ParallelGenerator#buildTasks(nda.analysis.Setup)}.
     */
    @Test
    public void testBuildTasks() throws Exception {
        generator.loadHandlers();
        Setup setup = generator.setup;

        List<PatternHandler> expected_patterns = new ArrayList<PatternHandler>();
        List<PatternHandler> generated_patterns = new ArrayList<PatternHandler>();

        for (Setup.Dataset dataset : setup.getDatasets())
            expected_patterns.addAll(generator.buildDataset(dataset));

        List<Callable<List<PatternHandler>>> tasks = generator.buildTasks(setup);
        for (Callable<List<PatternHandler>> task : tasks)
            generated_patterns.addAll(task.call());

        assertEquals(expected_patterns.size(), generated_patterns.size());

        for (int i = 0; i < expected_patterns.size(); ++i) {
            Instances expected = expected_patterns.get(i).getRelation();
            Instances generated = expected_patterns.get(i).getRelation();

            assertTrue(expected.equalHeaders(generated));
            assertEquals(expected.numInstances(), generated.numInstances());
        }
    }
}
