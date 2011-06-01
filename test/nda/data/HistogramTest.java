package nda.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import nda.data.text.TextSpikeHandler;
import nda.util.FileUtils;


/**
 * Tests for the Histogram class.
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class HistogramTest {
    private final double[] v1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private final double[] v2 = { 5, 6, 1, 3, 8, 7, 4, 0, 9, 2 };
    private final double[] v3 = { 6 };
    private final double EPS = 1e-10;

    private final Interval i1 = Interval.make(0, 10);

    private SpikeTrain st_hp;
    private Histogram hist_hp;

    private final String spikeDir = "setup/spikes/";
    private final String spikePath = "setup/spikes/HP_02a.spk";
    private final String bigPath = "setup/test_spikes/big.spk";
    private final String bigCRPath = "setup/test_spikes/big_cr_1000_4000_0643.txt";


    @Before
    public void setUp() throws Exception {
        SpikeHandlerI handler = new TextSpikeHandler(spikeDir);
        st_hp = handler.getSpikeTrain("HP_02a");
        hist_hp = new Histogram(st_hp.getInterval(), 0.5);
        hist_hp.load(st_hp);
    }


    /**
     * Test method for {@link nda.data.Histogram#Histogram(nda.data.Interval)}.
     */
    @Test
    public void testHistogramInterval() {
        Histogram hist = new Histogram(i1);

        assertEquals(i1, hist.getInterval());
        assertEquals(0.250, hist.getBinSize(), EPS);
        assertEquals(40, hist.getNumberBins());
    }


    /**
     * Test method for {@link nda.data.Histogram#Histogram(nda.data.Interval, int)}.
     */
    @Test
    public void testHistogramIntervalInt() {
        Histogram hist = new Histogram(i1, 5);
        assertEquals(5, hist.getNumberBins());
        assertEquals(2, hist.getBinSize(), EPS);

        hist = new Histogram(i1, 1);
        assertEquals(1, hist.getNumberBins());
        assertEquals(10, hist.getBinSize(), EPS);
    }


    /**
     * Test method for {@link nda.data.Histogram#Histogram(nda.data.Interval, double)}.
     */
    @Test
    public void testHistogramIntervalDouble() {
        Histogram hist = new Histogram(i1, 0.5);
        assertEquals(20, hist.getNumberBins());
        assertEquals(0.5, hist.getBinSize(), EPS);

        hist = new Histogram(i1, 10.0);
        assertEquals(1, hist.getNumberBins());
        assertEquals(10, hist.getBinSize(), EPS);
    }


    /**
     * Test method for {@link nda.data.Histogram#toString()}.
     */
    @Test
    public void testToString() {
        Histogram hist = new Histogram(i1);
        hist.load(v1);

        String str = hist.toString();

        assertTrue(str.contains("Histogram"));
        assertTrue(str.contains(hist.getInterval().toString()));
        assertTrue(str.contains(String.format("%.03f", hist.getBinSize())));
    }


    /**
     * Test method for {@link nda.data.Histogram#reset()}.
     */
    @Test
    public void testReset() {
        Histogram hist = new Histogram(i1, 1.0);
        hist.load(v1);

        assertEquals(10, hist.getNumberBins());
        assertEquals(1, hist.getBinCount(3));

        hist.reset();
        assertEquals(10, hist.getNumberBins());
        assertEquals(0, hist.getBinCount(3));
    }


    /**
     * Test method for {@link nda.data.Histogram#load(nda.data.SpikeTrain)}.
     */
    @Test
    public void testLoadSpikeTrain() throws Exception {
        SpikeHandlerI handler = new TextSpikeHandler(spikeDir);
        SpikeTrain st = handler.getSpikeTrain("HP_02a");

        Histogram hist = new Histogram(i1, 10);
        hist.load(st);

        assertEquals(10, hist.getNumberBins());

        Interval int_b0 = Interval.make(0, hist.getBinSize());
        int b0 = st.extractInterval(int_b0).getNumberOfSpikes();
        assertEquals(hist.getBinCount(0), b0);
    }


    /**
     * Test method for {@link nda.data.Histogram#load(double[])}.
     */
    @Test
    public void testLoadDoubleArray() {
        Histogram hist = new Histogram(i1, 5);
        hist.load(v2);

        assertEquals(2, hist.getBinSize(), EPS);
        assertEquals(5, hist.getNumberBins());
        assertEquals(2, hist.getBinCount(1));

        hist = new Histogram(i1, 5);
        hist.load(v3);

        assertEquals(0, hist.getBinCount(0));
        assertEquals(0, hist.getBinCount(2));
        assertEquals(1, hist.getBinCount(3));
    }


    /**
     * Test method for {@link nda.data.Histogram#getBinCount(int)}.
     */
    @Test
    public void testGetBinCount() {
        Histogram hist = new Histogram(i1, 5);
        hist.load(v2);

        for (int i = 0; i < hist.getNumberBins(); ++i)
            assertEquals(2, hist.getBinCount(i));
    }


    /**
     * Test method for {@link nda.data.Histogram#add(double value)}.
     */
    @Test
    public void testBoundaries() {
        double binSize = 2.4346;
        Histogram hist = new Histogram(Interval.make(0, 5000), binSize);
        int numBins = hist.getNumberBins();

        hist.add(binSize - EPS);
        hist.add(0 - EPS);
        assertEquals(1, hist.getBinCount(0));

        hist.add(binSize + EPS);
        hist.add(binSize);
        assertEquals(2, hist.getBinCount(1));

        hist.add(numBins * binSize);
        assertEquals(0, hist.getBinCount(numBins-1));
    }


    @Test
    public void testSum() {
        int[] counts = hist_hp.getBinCounts();

        int sum = 0;
        for (int x : counts) sum += x;

        assertEquals(st_hp.getNumberOfSpikes()-1, sum);
    }


    @Test
    public void testLoadFile() throws Exception {
        Histogram hist = new Histogram(hist_hp);
        hist.load(spikePath);

        assertTrue(Arrays.equals(hist_hp.getBinCounts(), hist.getBinCounts()));
    }


    /**
     * Test method for {@link nda.data.Histogram#getBinFor(double)}.
     */
    @Test
    public void testGetBinFor() {
        double binSize = 1.21355;
        Histogram hist = new Histogram(Interval.make(0, 5000), binSize);

        assertEquals(0, hist.getBinFor(binSize-EPS));
        assertEquals(1, hist.getBinFor(binSize));
        assertEquals(1, hist.getBinFor(binSize+EPS));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testOutside() {
        new Histogram(i1, 1.21355).getBinFor(20);
    }


    /**
     * Test method for {@link nda.data.Histogram#saveBinCounts(int[])}.
     */
    @Test
    public void testSaveBinCounts() {
        int[] counts = hist_hp.getBinCounts();
        int[] save = new int[counts.length];
        hist_hp.saveBinCounts(save);

        assertTrue(Arrays.equals(counts, save));
    }


    @Test
    public void testRealDataFromMatlab() throws Exception {
        Histogram hist = new Histogram(Interval.make(1000, 4000), 0.643);
        hist.load(bigPath);

        int[] java_cr = hist.getBinCounts();
        int[] mat_cr = FileUtils.readIntArray(bigCRPath);

        assertTrue(Arrays.equals(mat_cr, java_cr));
    }
}
