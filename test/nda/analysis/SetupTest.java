package nda.analysis;

import static org.junit.Assert.assertEquals;
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
    private Setup ge4_setup;
    private Setup ge5_setup;
    private static String invalidFilepath = "data/test/invalid.yml";
    private static String ge4SetupFilepath = "data/test/ge4_setup.yml";
    private static String ge5SetupFilepath = "data/test/ge5_setup.yml";


    @Before
    public void setUp() throws Exception {
        ge4_setup = new Setup(ge4SetupFilepath);
        ge5_setup = new Setup(ge5SetupFilepath);
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
     * Test method for {@link nda.analysis.Setup#getName()}.
     */
    @Test
    public void testGetName() {
        assertEquals("ge4", ge4_setup.getName());
        assertEquals("ge5", ge5_setup.getName());
    }


    /**
     * Test method for {@link nda.analysis.Setup#getSpikesDirectory()}.
     */
    @Test
    public void testGetSpikesDirectory() {
        assertEquals("data/real/ge4/spikes", ge4_setup.getSpikesDirectory());
        assertEquals("data/real/ge5/spikes", ge5_setup.getSpikesDirectory());
    }


    /**
     * Test method for {@link nda.analysis.Setup#getContactsFilepath()}.
     */
    @Test
    public void testGetContactsFilepath() {
        assertEquals("data/real/ge4/ge4_contacts.txt", ge4_setup.getContactsFilepath());
        assertEquals("data/real/ge5/ge5_contacts.txt", ge5_setup.getContactsFilepath());
    }


    /**
     * Test method for {@link nda.analysis.Setup#getOutputDirectory()}.
     */
    @Test
    public void testGetOutputDirectory() {
        assertEquals("data/real/ge4/datasets", ge4_setup.getOutputDirectory());
        assertEquals("data/real/ge5/datasets", ge5_setup.getOutputDirectory());
    }


    /**
     * Test method for {@link nda.analysis.Setup#getDatasets()}.
     */
    @Test
    public void testGetDatasets() {
        assertEquals(2, ge4_setup.getDatasets().size());
    }


    @Test
    public void testMinimalDataset() {
        Setup.Dataset dataset = ge4_setup.getDatasets().get(0);

        assertEquals("ge4_a", dataset.getName());
        assertEquals(3, dataset.getNumberRounds());
        assertEquals(0.8, dataset.getTrainRatio(), 1e-8);
        assertEquals(2, dataset.getClasses().size());
        assertEquals("hp_", dataset.getParameter("neurons"));
        assertEquals(0.250, (Double)dataset.getParameter("bin_size"), 1e-8);
        assertEquals(2, ((Integer)dataset.getParameter("window_width")).intValue());
    }


    @Test
    public void testSecondDataset() {
        Setup.Dataset dataset = ge4_setup.getDatasets().get(1);

        assertEquals("ge4_b", dataset.getName());
        assertEquals(5, dataset.getNumberRounds());
        assertEquals(0.5, (Double)dataset.getParameter("bin_size"), 1e-8);
        assertEquals("hp_", dataset.getParameter("neurons"));
    }


    @Test
    public void testClassesA() {
        Setup.Dataset dataset = ge4_setup.getDatasets().get(0);

        List<Setup.Class> classes = dataset.getClasses();
        assertEquals(2, classes.size());

        Setup.Class posClass = classes.get(0);
        Setup.Class negClass = classes.get(1);

        assertEquals("yes", posClass.getName());
        assertEquals("no", negClass.getName());

        assertEquals(2, posClass.getLabels().size());
        assertTrue(posClass.getLabels().contains("ball"));
        assertTrue(posClass.getLabels().contains("brush"));
        assertEquals(3, posClass.getNumberSamples());
        assertEquals(2, posClass.getNumberTrainSamples());
        assertEquals(1, posClass.getNumberTestSamples());

        assertEquals(2, negClass.getLabels().size());
        assertTrue(negClass.getLabels().contains("food"));
        assertTrue(negClass.getLabels().contains("urchin"));
        assertEquals(6, negClass.getNumberSamples());
        assertEquals(4, negClass.getNumberTrainSamples());
        assertEquals(2, negClass.getNumberTestSamples());
    }
}
