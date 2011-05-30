package nda.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * Tests for the FileUtils class.
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class FileUtilsTest {

    /**
     * Test method for {@link nda.util.FileUtils#parseFileName(java.lang.String)}.
     */
    @Test
    public void testParseFileName() {
        assertEquals("name", FileUtils.parseFileName("name"));
        assertEquals("NAME2", FileUtils.parseFileName("path1/path2/NAME2.spk"));
        assertEquals("Name3", FileUtils.parseFileName("p/p/2/3/4/5/Name3.txt"));
        assertEquals("a", FileUtils.parseFileName("a.txt"));
        assertEquals("a", FileUtils.parseFileName("b/a"));
    }
}
