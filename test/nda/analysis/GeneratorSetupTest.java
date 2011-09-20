package nda.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import nda.analysis.generation.GeneratorSetup;


/**
 * Tests for the Setup class.
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class GeneratorSetupTest {
    private GeneratorSetup ge5_setup;
    private GeneratorSetup test_setup;
    private GeneratorSetup test_drop;
    private GeneratorSetup test_uniform_sur;
    private GeneratorSetup test_poisson_sur;
    private GeneratorSetup test_col_swap_sur;
    private GeneratorSetup test_neuron_swap_sur;

    private static String invalidFilepath = "data/test/invalid.yml";
    private static String ge5SetupFilepath = "data/test/ge5_setup.yml";
    private static String testSetupFilepath = "data/test/test_setup.yml";
    private static String testSetupMPFilepath = "data/test/test_multiple_params.yml";
    private static String dropSetupFilepath = "data/test/test2_dropping.yml";
    private static String surrogateSetupFilepath = "data/test/test_uniform_surrogates.yml";
    private static String poissonSurSetupFilepath = "data/test/test_poisson_surrogates.yml";
    private static String colSwapSurSetupFilepath = "data/test/test_col_swap.yml";
    private static String neuronSwapSurSetupFilepath = "data/test/test_neuron_swap.yml";


    @Before
    public void setUp() throws Exception {
        ge5_setup = new GeneratorSetup(ge5SetupFilepath);
        test_setup = new GeneratorSetup(testSetupFilepath);
        test_drop = new GeneratorSetup(dropSetupFilepath);
        test_uniform_sur = new GeneratorSetup(surrogateSetupFilepath);
        test_poisson_sur = new GeneratorSetup(poissonSurSetupFilepath);
        test_col_swap_sur = new GeneratorSetup(colSwapSurSetupFilepath);
        test_neuron_swap_sur = new GeneratorSetup(neuronSwapSurSetupFilepath);
    }


    @Test(expected = FileNotFoundException.class)
    public void testMissingFile() throws Exception {
        new GeneratorSetup("__missing_file__");
    }


    @Test(expected = InvalidSetupFileException.class)
    public void testInvalidFile() throws Exception {
        new GeneratorSetup(invalidFilepath);
    }


    /**
     * Test method for {@link nda.analysis.generation.GeneratorSetup#toString()}.
     */
    @Test
    public void testToString() {
        assertFalse(test_setup.toString().isEmpty());

        for (GeneratorSetup.Dataset dataset : test_setup.getDatasets()) {
            assertFalse(dataset.toString().isEmpty());
            for (GeneratorSetup.Class class_attr : dataset.getClasses())
                assertFalse(class_attr.toString().isEmpty());
        }
    }


    /**
     * Test method for {@link nda.analysis.generation.GeneratorSetup#getName()}.
     */
    @Test
    public void testGetName() {
        assertEquals("test_data", test_setup.getName());
        assertEquals("ge5", ge5_setup.getName());
    }


    /**
     * Test method for {@link nda.analysis.generation.GeneratorSetup#getSpikesDirectory()}.
     */
    @Test
    public void testGetSpikesDirectory() {
        assertEquals("data/test/spikes", test_setup.getSpikesDirectory());
        assertEquals("data/real/ge5/spikes/01", ge5_setup.getSpikesDirectory());
    }


    /**
     * Test method for {@link nda.analysis.generation.GeneratorSetup#getContactsFilepath()}.
     */
    @Test
    public void testGetContactsFilepath() {
        assertEquals("data/test/behaviors/test_contacts.txt", test_setup.getContactsFilepath());
        assertEquals("data/real/ge5/ge5_contacts.txt", ge5_setup.getContactsFilepath());
    }


    /**
     * Test method for {@link nda.analysis.generation.GeneratorSetup#getOutputDirectory()}.
     */
    @Test
    public void testGetOutputDirectory() {
        assertEquals("data/test/datasets/test_data", test_setup.getOutputDirectory());
        assertEquals("data/real/ge5/datasets", ge5_setup.getOutputDirectory());
    }


    /**
     * Test method for {@link nda.analysis.generation.GeneratorSetup#getDatasets()}.
     */
    @Test
    public void testGetDatasets() {
        assertEquals(9, test_setup.getDatasets().size());
        assertEquals(5, ge5_setup.getDatasets().size());
    }


    @Test
    public void testMinimalDataset() {
        List<GeneratorSetup.Dataset> datasets = test_setup.getDatasets();

        GeneratorSetup.Dataset ds_ball = datasets.get(0);
        assertEquals("ge4_ball_p1", ds_ball.getName());
        assertEquals(1, ds_ball.getNumberRounds());
        assertEquals(0.8, ds_ball.getTrainRatio(), 1e-8);
        assertEquals(2, ds_ball.getClasses().size());
        assertEquals(0.250, (Double)ds_ball.getParameter("bin_size"), 1e-8);
        assertEquals(5, ((Integer)ds_ball.getParameter("window_width")).intValue());

        GeneratorSetup.Dataset ds_food = datasets.get(2);
        assertEquals("ge4_food_p1", ds_food.getName());
        assertEquals(1, ds_food.getNumberRounds());
        assertEquals(0.8, ds_food.getTrainRatio(), 1e-8);
        assertEquals(2, ds_food.getClasses().size());
        assertEquals(0.250, (Double)ds_food.getParameter("bin_size"), 1e-8);
        assertEquals(5, ((Integer)ds_food.getParameter("window_width")).intValue());
    }


    @Test
    public void testClassesA() {
        GeneratorSetup.Dataset dataset = test_setup.getDatasets().get(0);

        List<GeneratorSetup.Class> classes = dataset.getClasses();
        assertEquals(2, classes.size());

        GeneratorSetup.Class posClass = classes.get(0);
        GeneratorSetup.Class negClass = classes.get(1);

        assertEquals("yes", posClass.getName());
        assertEquals("no", negClass.getName());

        assertEquals(1, posClass.getLabels().size());
        assertTrue(posClass.getLabels().contains("ball"));
        assertEquals(3, posClass.getNumberSamples());
        assertEquals(2, posClass.getNumberTrainSamples());
        assertEquals(1, posClass.getNumberTestSamples());

        assertEquals(3, negClass.getLabels().size());
        assertTrue(negClass.getLabels().contains("brush"));
        assertTrue(negClass.getLabels().contains("food"));
        assertTrue(negClass.getLabels().contains("urchin"));
        assertEquals(6, negClass.getNumberSamples());
        assertEquals(4, negClass.getNumberTrainSamples());
        assertEquals(2, negClass.getNumberTestSamples());
    }


    @Test
    public void testMultipleParams() throws Exception {
        GeneratorSetup test_mp = new GeneratorSetup(testSetupMPFilepath);

        assertEquals(4, test_mp.getParameterChoices().size());
        assertEquals("s2, s3", test_mp.getParameterChoices().get(1).get("areas"));
        assertEquals("*", test_mp.getParameterChoices().get(3).get("areas"));
        assertEquals(0.250, test_mp.getParameterChoices().get(0).get("bin_size"));
        assertEquals(0.250, test_mp.getParameterChoices().get(1).get("bin_size"));
        assertEquals(0.250, test_mp.getParameterChoices().get(2).get("bin_size"));
        assertEquals(10, test_mp.getParameterChoices().get(0).get("window_width"));
        assertEquals(10, test_mp.getParameterChoices().get(1).get("window_width"));
        assertEquals(10, test_mp.getParameterChoices().get(2).get("window_width"));

        assertEquals(16, test_mp.getDatasets().size());

        int count = 0;
        for (GeneratorSetup.Dataset dataset : test_mp.getDatasets())
            count += dataset.getGeneratedFileNames().size();
        assertEquals(16*10*2, count);
    }


    @Test
    public void testNeuronDropSetup() {
        assertEquals(40, test_drop.getDatasets().size());

        String[] ps = { "_p1", "_p2", "_p3" };
        Map<String, Integer> count = new HashMap<String, Integer>();

        for (String p : ps) count.put(p, 0);
        for (GeneratorSetup.Dataset dataset : test_drop.getDatasets()) {
            for (String p : ps) {
                if (dataset.getName().contains(p)) {
                    count.put(p, count.get(p) + 1);
                    break;
                }
            }

            assertNotNull(dataset.getParameter("num_drop"));
        }

        assertEquals((Integer) 12, count.get("_p1"));
        assertEquals((Integer) 12, count.get("_p2"));
        assertEquals((Integer) 16, count.get("_p3"));
    }


    @Test
    public void testUniformSurrogateSetup() {
        assertEquals(39, test_uniform_sur.getDatasets().size());

        String[] ps = { "_p1", "_p2", "_p3" };
        Map<String, Integer> count = new HashMap<String, Integer>();

        for (String p : ps) count.put(p, 0);
        for (GeneratorSetup.Dataset dataset : test_uniform_sur.getDatasets()) {
            for (String p : ps) {
                if (dataset.getName().contains(p)) {
                    count.put(p, count.get(p) + 1);
                    break;
                }
            }

            assertNotNull(dataset.getParameter("num_surrogate"));
            assertEquals("uniform", dataset.getParameter("surrogate_type"));
        }

        assertEquals((Integer) 12, count.get("_p1"));
        assertEquals((Integer) 12, count.get("_p2"));
        assertEquals((Integer) 15, count.get("_p3"));
    }


    @Test
    public void testPoissonSurrogateSetup() {
        assertEquals(39, test_poisson_sur.getDatasets().size());

        String[] ps = { "_p1", "_p2", "_p3" };
        Map<String, Integer> count = new HashMap<String, Integer>();

        for (String p : ps) count.put(p, 0);
        for (GeneratorSetup.Dataset dataset : test_poisson_sur.getDatasets()) {
            for (String p : ps) {
                if (dataset.getName().contains(p)) {
                    count.put(p, count.get(p) + 1);
                    break;
                }
            }

            assertNotNull(dataset.getParameter("num_surrogate"));
            assertEquals("poisson", dataset.getParameter("surrogate_type"));
        }

        assertEquals((Integer) 12, count.get("_p1"));
        assertEquals((Integer) 12, count.get("_p2"));
        assertEquals((Integer) 15, count.get("_p3"));
    }


    @Test
    public void testColSwapSurrogateSetup() {
        assertEquals(27, test_col_swap_sur.getDatasets().size());

        String[] ps = { "_p1", "_p2", "_p3" };
        Map<String, Integer> count = new HashMap<String, Integer>();
        for (String p : ps) count.put(p, 0);
        int[] pct_count = { 0, 0, 0 };

        for (GeneratorSetup.Dataset dataset : test_col_swap_sur.getDatasets()) {
            for (String p : ps) {
                if (dataset.getName().contains(p)) {
                    count.put(p, count.get(p) + 1);
                    break;
                }
            }

            assertTrue(dataset.getName().contains("sur_col_swap"));
            assertNotNull(dataset.getParameter("pct_surrogate"));
            assertEquals("col_swap", dataset.getParameter("surrogate_type"));

            double pct = (Double) dataset.getParameter("pct_surrogate");
            if (pct == 0.1)
                pct_count[0]++;
            else if (pct == 0.5)
                pct_count[1]++;
            else if (pct == 1.0)
                pct_count[2]++;
            else
                fail("Wrong pct");
        }

        assertEquals((Integer) 9, count.get("_p1"));
        assertEquals((Integer) 9, count.get("_p2"));
        assertEquals((Integer) 9, count.get("_p3"));

        for (int c : pct_count) assertEquals(9, c);
    }


    @Test
    public void testNeuronSwapSurrogateSetup() {
        assertEquals(39, test_neuron_swap_sur.getDatasets().size());

        String[] ps = { "_p1", "_p2", "_p3" };
        Map<String, Integer> count = new HashMap<String, Integer>();
        for (String p : ps) count.put(p, 0);

        for (GeneratorSetup.Dataset dataset : test_neuron_swap_sur.getDatasets()) {
            for (String p : ps) {
                if (dataset.getName().contains(p)) {
                    count.put(p, count.get(p) + 1);
                    break;
                }
            }

            assertTrue(dataset.getName().contains("sur_neuron_swap"));
            assertNotNull(dataset.getParameter("pct_surrogate"));
            assertEquals("neuron_swap", dataset.getParameter("surrogate_type"));

            double pct = (Double) dataset.getParameter("pct_surrogate");
            assertEquals(0.5, pct, 1e-8);
        }

        assertEquals((Integer) 12, count.get("_p1"));
        assertEquals((Integer) 12, count.get("_p2"));
        assertEquals((Integer) 15, count.get("_p3"));
    }
}
