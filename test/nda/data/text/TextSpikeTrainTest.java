package nda.data.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import nda.data.Interval;
import nda.data.SpikeTrainI;


/**
 * Tests for the TextSpikeTrain class.
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class TextSpikeTrainTest {
    private TextSpikeTrain spikeTest;
    private String spikeTestName = "Test";
    private String spikeTestPath = "data/test/test_spikes/test.spk";

    private TextSpikeTrain spikeHP02a;
    private String spikeHP02aName = "HP_02a";
    private String spikeHP02aPath = "data/test/spikes/HP_02a.spk";

    private String missingSpikePath = "data/test/test_spikes/MISSING.spk";
    private String invalidSpikePath = "data/test/test_spikes/invalid.spk";
    private String unsortedSpikePath = "data/test/test_spikes/unsorted.spk";


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

        SpikeTrainI test = new TextSpikeTrain(times, name);
        assertEquals(6, test.size());
        assertTrue(Interval.make(0, 5).contains(test.getInterval()));
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeTrain#TextSpikeTrain(java.lang.String)}.
     */
    @Test
    public void testTextSpikeTrainString() throws Exception {
        SpikeTrainI st = new TextSpikeTrain(spikeTestPath);
        assertEquals("test", st.getNeuronName());
        assertNull(st.getNeuronArea());
        assertEquals(10, st.size());
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeTrain#TextSpikeTrain(java.lang.String, java.lang.String, nda.data.Interval)}.
     */
    @Test
    public void testTextSpikeTrainStringStringInterval() throws Exception {
        Interval interval = Interval.make(4.5, 10.5);
        SpikeTrainI st = new TextSpikeTrain(spikeTestPath, spikeTestName, interval);

        assertEquals(6, st.size());
        assertTrue(interval.contains(st.getInterval()));
    }


    /**
     * Test method for {@link nda.data.SpikeTrain#toString()}.
     */
    @Test
    public void testToString() {
        String str = spikeTest.toString();
        assertFalse(str.isEmpty());
        assertTrue(str.contains(spikeTest.getNeuronName()));
    }

    /**
     * Test method for {@link nda.data.SpikeTrain#getTimes()}.
     */
    @Test
    public void testGetTimes() {
        double[] times = spikeHP02a.getTimes();

        assertEquals(times.length, spikeHP02a.size());
        assertEquals(5811.772725, times[0], 1e-8);
        assertEquals(5850.647000, times[times.length-2], 1e-8);
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeTrain#extractInterval(nda.data.Interval)}.
     */
    @Test
    public void testExtractInterval() {
        Interval it1 = Interval.make(0.5, 5);
        Interval it2 = Interval.make(5, 5);

        SpikeTrainI st1 = spikeTest.extractInterval(it1);
        assertEquals(5, st1.size());
        assertTrue(it1.contains(st1.getInterval()));

        SpikeTrainI st2 = spikeTest.extractInterval(it2);
        assertEquals(1, st2.size());
        assertEquals(5, st2.get(0), 1e-8);

        SpikeTrainI st3 = spikeHP02a.extractInterval(it1);
        assertTrue(st3.isEmpty());

        SpikeTrainI st4 = st1.extractInterval(Interval.EMPTY);
        SpikeTrainI st5 = st1.extractInterval(Interval.INF);
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
