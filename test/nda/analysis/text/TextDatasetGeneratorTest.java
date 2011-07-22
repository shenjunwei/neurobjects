package nda.analysis.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nda.analysis.Setup;


/**
 * Tests for the TextDatasetGenerator class.
 *
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class TextDatasetGeneratorTest {
    private File outputDir;
    private TextDatasetGenerator generator;
    private static String setupFilepath = "data/test/test_setup_gen.yml";


    @BeforeClass
    public static void setUpClass() throws Exception {
        Setup setup = new Setup(setupFilepath);
        String outputDir = setup.getOutputDirectory();
        File dirFile = new File(outputDir);

        if (!dirFile.exists() && !dirFile.mkdir())
            throw new IllegalArgumentException("Cant create dir");

        for (File file : dirFile.listFiles())
            if (file.isFile()) file.delete();

        assertEquals(0, dirFile.listFiles().length);
    }


    @Before
    public void setUp() throws Exception {
        Setup setup = new Setup(setupFilepath);
        outputDir = new File(setup.getOutputDirectory());

        generator = new TextDatasetGenerator(setup);
        generator.setVerbose(false);
    }


    /**
     * Test method for {@link nda.analysis.text.TextDatasetGenerator#generate()}.
     */
    @Test
    public void testGenerate() throws Exception {
        generator.generate();
        assertTrue(outputDir.listFiles().length > 0);
    }
}
