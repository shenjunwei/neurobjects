package nda.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;



/**
 * Tests for the Setup class.
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class SetupTest {
    private Setup ge5_setup;
    private Setup test_setup;
    private static String invalidFilepath = "data/test/invalid.yml";
    private static String ge5SetupFilepath = "data/test/ge5_setup.yml";
    private static String testSetupFilepath = "data/test/test_setup.yml";


    @Before
    public void setUp() throws Exception {
        ge5_setup = new Setup(ge5SetupFilepath);
        test_setup = new Setup(testSetupFilepath);
    }


    @Test(expected = FileNotFoundException.class)
    public void testMissingFile() throws Exception {
        new Setup("__missing_file__");
    }


    @Test(expected = InvalidSetupFileException.class)
    public void testInvalidFile() throws Exception {
        new Setup(invalidFilepath);
    }


    /**
     * Test method for {@link nda.analysis.Setup#toString()}.
     */
    @Test
    public void testToString() {
        assertFalse(test_setup.toString().isEmpty());

        for (Setup.Dataset dataset : test_setup.getDatasets()) {
            assertFalse(dataset.toString().isEmpty());
            for (Setup.Class class_attr : dataset.getClasses())
                assertFalse(class_attr.toString().isEmpty());
        }
    }


    /**
     * Test method for {@link nda.analysis.Setup#getName()}.
     */
    @Test
    public void testGetName() {
        assertEquals("test_data", test_setup.getName());
        assertEquals("ge5", ge5_setup.getName());
    }


    /**
     * Test method for {@link nda.analysis.Setup#getSpikesDirectory()}.
     */
    @Test
    public void testGetSpikesDirectory() {
        assertEquals("data/test/spikes", test_setup.getSpikesDirectory());
        assertEquals("data/real/ge5/spikes/01", ge5_setup.getSpikesDirectory());
    }


    /**
     * Test method for {@link nda.analysis.Setup#getContactsFilepath()}.
     */
    @Test
    public void testGetContactsFilepath() {
        assertEquals("data/test/behaviors/test_contacts.txt", test_setup.getContactsFilepath());
        assertEquals("data/real/ge5/ge5_contacts.txt", ge5_setup.getContactsFilepath());
    }


    /**
     * Test method for {@link nda.analysis.Setup#getOutputDirectory()}.
     */
    @Test
    public void testGetOutputDirectory() {
        assertEquals("data/test/datasets/test_data", test_setup.getOutputDirectory());
        assertEquals("data/real/ge5/datasets", ge5_setup.getOutputDirectory());
    }


    /**
     * Test method for {@link nda.analysis.Setup#getDatasets()}.
     */
    @Test
    public void testGetDatasets() {
        assertEquals(9, test_setup.getDatasets().size());
        assertEquals(5, ge5_setup.getDatasets().size());
    }


    @Test
    public void testMinimalDataset() {
        List<Setup.Dataset> datasets = test_setup.getDatasets();

        Setup.Dataset ds_ball = datasets.get(0);
        assertEquals("ge4_ball", ds_ball.getName());
        assertEquals(1, ds_ball.getNumberRounds());
        assertEquals(0.8, ds_ball.getTrainRatio(), 1e-8);
        assertEquals(2, ds_ball.getClasses().size());
        assertEquals("[HP, V]", ds_ball.getParameter("neurons").toString());
        assertEquals(0.250, (Double)ds_ball.getParameter("bin_size"), 1e-8);
        assertEquals(5, ((Integer)ds_ball.getParameter("window_width")).intValue());

        Setup.Dataset ds_food = datasets.get(2);
        assertEquals("ge4_food", ds_food.getName());
        assertEquals(1, ds_food.getNumberRounds());
        assertEquals(0.8, ds_food.getTrainRatio(), 1e-8);
        assertEquals(2, ds_food.getClasses().size());
        assertEquals("[HP, V]", ds_food.getParameter("neurons").toString());
        assertEquals(0.250, (Double)ds_food.getParameter("bin_size"), 1e-8);
        assertEquals(5, ((Integer)ds_food.getParameter("window_width")).intValue());
    }


    @Test
    public void testClassesA() {
        Setup.Dataset dataset = test_setup.getDatasets().get(0);

        List<Setup.Class> classes = dataset.getClasses();
        assertEquals(2, classes.size());

        Setup.Class posClass = classes.get(0);
        Setup.Class negClass = classes.get(1);

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
}
