package nda.analysis.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;



/**
 * Tests for the SimpleParallelGenerator class.
 *
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class SimpleParallelGeneratorTest {
    private File outputDir;
    private SimpleParallelGenerator generator;
    private static String setupFilepath = "data/test/test_setup_gen.yml";


    @BeforeClass
    public static void setUpClass() throws Exception {
        GeneratorSetup setup = new GeneratorSetup(setupFilepath);
        String outputDir = setup.getOutputDirectory();
        File dirFile = new File(outputDir);

        if (!dirFile.exists() && !dirFile.mkdir())
            throw new IllegalArgumentException("Cant create dir: " + dirFile);

        for (File file : dirFile.listFiles())
            if (file.isFile()) file.delete();

        assertEquals(0, dirFile.listFiles().length);
    }


    @Before
    public void setUp() throws Exception {
        GeneratorSetup setup = new GeneratorSetup(setupFilepath);
        outputDir = new File(setup.getOutputDirectory());

        generator = new SimpleParallelGenerator(setup);
        generator.setVerbose(false);
    }


    /**
     * Test method for {@link nda.analysis.generation.SimpleParallelGenerator#generate()}.
     */
    @Test
    public void testGenerate() throws Exception {
        generator.generate();
        assertTrue(outputDir.listFiles().length > 0);
    }
}
