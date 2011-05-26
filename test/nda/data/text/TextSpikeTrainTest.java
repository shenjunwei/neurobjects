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
public class TextSpikeTrainTest {
    private TextSpikeTrain spikeTest;
    private String spikeTestName = "Test";
    private String spikeTestPath = "setup/test_spikes/test.spk";
    
    private TextSpikeTrain spikeHP02a;
    private String spikeHP02aName = "HP_02a"; 
    private String spikeHP02aPath = "setup/spikes/HP_02a.spk";
    
    private String missingSpikePath = "setup/test_spikes/MISSING.spk";
    private String invalidSpikePath = "setup/test_spikes/invalid.spk";
    private String unsortedSpikePath = "setup/test_spikes/unsorted.spk";
    
    
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
        double[] times = new double[] { 0, 1, 2, 3, 4, 5 };
        String name = "testDoubleArray";
        
        SpikeTrain test = new TextSpikeTrain(times, name);
        assertEquals(6, test.getNumberOfSpikes());
        assertTrue(Interval.make(0, 5).contains(test.getInterval()));
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
        
        SpikeTrain emptySt = new TextSpikeTrain(
                spikeTestPath, spikeTestName, Interval.EMPTY);
        
        assertEquals(0, emptySt.getNumberOfSpikes());
        assertTrue(emptySt.getInterval().isEmpty());
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
        Interval it1 = Interval.make(0.5, 5);
        Interval it2 = Interval.make(5, 5);
        
        SpikeTrain st1 = spikeTest.extractInterval(it1);
        assertEquals(5, st1.getNumberOfSpikes());
        assertTrue(it1.contains(st1.getInterval()));
        
        SpikeTrain st2 = spikeTest.extractInterval(it2);
        assertEquals(1, st2.getNumberOfSpikes());
        assertEquals(5, st2.getSpike(0), 1e-8);
        
        SpikeTrain st3 = spikeHP02a.extractInterval(it1);
        assertTrue(st3.isEmpty());
        
        SpikeTrain st4 = st1.extractInterval(Interval.EMPTY);
        SpikeTrain st5 = st1.extractInterval(Interval.INF);
        assertTrue(st4.isEmpty());
        assertTrue(st5.getInterval().contains(st1.getInterval()));
    }

    @Test(expected = MissingDataFileException.class)
    public void testMissingFile() throws Exception {
        new TextSpikeTrain(missingSpikePath); 
    }
    
    @Test(expected = InvalidDataFileException.class)
    public void testInvalidFile() throws Exception {
        new TextSpikeTrain(invalidSpikePath);
    }
    
    @Test(expected = InvalidDataFileException.class)
    public void testUnsortedFile() throws Exception {
        new TextSpikeTrain(unsortedSpikePath);
    }
}
