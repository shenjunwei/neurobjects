package nda.data.text;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import nda.data.Interval;
import nda.data.SpikeTrain;


/**
 * Tests for the TextSpikeTrain class.
 * 
 * @author giulianoxt
 */
public class TextSpikeTrainTest extends TextSpikeTrain {
    private TextSpikeTrain spikeTest;
    private String spikeTestName = "Test";
    private String spikeTestPath = "setup/spikes/Test.spk";
    
    private TextSpikeTrain spikeHP02a;
    private String spikeHP02aName = "HP_02a"; 
    private String spikeHP02aPath = "setup/spikes/HP_02a.spk";
    
    
    @Before
    public void setUp() throws Exception {
        spikeTest = new TextSpikeTrain(spikeTestPath, spikeTestName);
        spikeHP02a = new TextSpikeTrain(spikeHP02aPath, spikeHP02aName);
    }

    /**
     * Test method for {@link nda.data.text.TextSpikeTrain#TextSpikeTrain(double[], java.lang.String)}.
     */
    @Test
    public void testTextSpikeTrainDoubleArrayString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link nda.data.text.TextSpikeTrain#TextSpikeTrain(java.lang.String)}.
     */
    @Test
    public void testTextSpikeTrainString() throws Exception {
        SpikeTrain st = new TextSpikeTrain(spikeTestPath);
        assertEquals("test", st.getName());
        assertEquals(10, st.getNumberOfSpikes());
    }

    /**
     * Test method for {@link nda.data.text.TextSpikeTrain#TextSpikeTrain(java.lang.String, java.lang.String, nda.data.Interval)}.
     */
    @Test
    public void testTextSpikeTrainStringStringInterval() throws Exception {
        Interval interval = Interval.make(4.5, 10.5);
        SpikeTrain st = new TextSpikeTrain(spikeTestPath, spikeTestName, interval);

        assertEquals(6, st.getNumberOfSpikes());
        assertTrue(interval.contains(st.getInterval()));
    }

    /**
     * Test method for {@link nda.data.SpikeTrain#toString()}.
     */
    @Test
    public void testToString() {
        String str = spikeTest.toString();
        assertFalse(str.isEmpty());
        assertTrue(str.contains(spikeTest.getName()));
    }

    /**
     * Test method for {@link nda.data.SpikeTrain#getTimes()}.
     */
    @Test
    public void testGetTimes() {
        double[] times = spikeHP02a.getTimes();
        
        assertEquals(times.length, spikeHP02a.getNumberOfSpikes());
        assertEquals(5811.772725, times[0], 1e-8);
        assertEquals(5850.647000, times[times.length-2], 1e-8);
    }

    /**
     * Test method for {@link nda.data.SpikeTrain#isEmpty()}.
     */
    @Test
    public void testIsEmpty() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link nda.data.SpikeTrain#getInterspikeInterval()}.
     */
    @Test
    public void testGetInterspikeInterval() {
        double[] test_isi = spikeTest.getInterspikeInterval();
        double[] hp_isi = spikeHP02a.getInterspikeInterval();
        double[] hp_times = spikeHP02a.getTimes();
        
        assertArrayEquals(new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 }, test_isi, 1e-8);
        assertEquals(hp_times[13]-hp_times[12], hp_isi[12], 1e-8);
    }

    /**
     * Test method for {@link nda.data.text.TextSpikeTrain#extractInterval(nda.data.Interval)}.
     */
    @Test
    public void testExtractInterval() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link nda.data.text.TextSpikeTrain#getNumberOfSpikes()}.
     */
    @Test
    public void testGetNumberOfSpikes() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link nda.data.text.TextSpikeTrain#setInitialValues(java.lang.String)}.
     */
    @Test
    public void testSetInitialValues() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link nda.data.text.TextSpikeTrain#parseFileName(java.lang.String)}.
     */
    @Test
    public void testParseFileName() {
        assertEquals("name", parseFileName("name"));
        assertEquals("name2", parseFileName("p/p1/p2/p3/name2.spk"));
    }

    /**
     * Test method for {@link nda.data.text.TextSpikeTrain#isSorted(java.util.List)}.
     */
    @Test
    public void testIsSorted() {
        fail("Not yet implemented");
    }
}
