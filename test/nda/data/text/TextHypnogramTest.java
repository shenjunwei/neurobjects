package nda.data.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import nda.data.HypnogramI;
import nda.data.Interval;


/**
 * Tests for the TextHypnogram class.
 * 
 * @author Giuliano Vilela
 */
public class TextHypnogramTest {
    private static final Map<Integer,String> CODE_STATES = new HashMap<Integer, String>();
    static {
        CODE_STATES.put(1, "awake");
        CODE_STATES.put(2, "sws");
        CODE_STATES.put(3, "rem");
        CODE_STATES.put(4, "wt");
    }

    private static final String missingFilepath = "data/test/missing_filepath.txt";
    private static final String invalidFilepath = "data/test/invalid.yml";
    private static final String testHypnogramFilepath = "data/test/test_hypnogram.txt";

    private HypnogramI test_hyp;


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        test_hyp = new TextHypnogram(testHypnogramFilepath, 10, CODE_STATES);
    }


    @Test(expected = MissingDataFileException.class)
    public void testMissing() throws Exception {
        new TextHypnogram(missingFilepath, 0, CODE_STATES);
    }


    @Test(expected = InvalidDataFileException.class)
    public void testInvalid() throws Exception {
        new TextHypnogram(invalidFilepath, 10, CODE_STATES);
    }


    /**
     * Test method for {@link nda.data.text.TextHypnogram#getInterval()}.
     */
    @Test
    public void testGetInterval() {
        assertEquals(test_hyp.getInterval(), Interval.make(0, 200));
    }


    /**
     * Test method for {@link nda.data.text.TextHypnogram#getWakingStates()}.
     */
    @Test
    public void testGetStates() {
        assertEquals(4, test_hyp.getStates().size());
        assertTrue(test_hyp.getStates().contains("awake"));
        assertTrue(test_hyp.getStates().contains("sws"));
        assertTrue(test_hyp.getStates().contains("rem"));
        assertTrue(test_hyp.getStates().contains("wt"));
    }


    /**
     * Test method for {@link nda.data.text.TextHypnogram#getStateFor(double)}.
     */
    @Test
    public void testGetStateFor() {
        double[] null_times = { -1, -1e-8, -1e-10, -2, -3, -5, 201, 200+1e-8, 400, 500 };
        for (double t : null_times)
            assertNull(test_hyp.getStateFor(t));

        double[] awake_times = {
                0, 1e-10, 10-1e-10, 90+1e-10, 95, 93, 92, 98, 100, 120-1e-10,
                170+1e-10, 180, 175, 185, 190, 183, 200 };
        for (double t : awake_times)
            assertEquals("awake", test_hyp.getStateFor(t));

        double[] sws_times = { 11, 13, 20-1e-10, 130-1e-10, 129, 161, 169 };
        for (double t : sws_times)
            assertEquals("sws", test_hyp.getStateFor(t));

        double[] rem_times = { 50+1e-10, 51, 53, 59, 60-1e-10, 59, 130+1e-10, 131, 149 };
        for (double t : rem_times)
            assertEquals("rem", test_hyp.getStateFor(t));

        double[] wt_times = { 60.0001, 61, 69, 65, 62, 69.99, 90-1e-10, 85, 88, 89, 160-1e-10, 152 };
        for (double t : wt_times)
            assertEquals("wt", test_hyp.getStateFor(t));
    }


    /**
     * Test method for {@link nda.data.text.TextHypnogram#getIntervalsFor(java.lang.String)}.
     */
    @Test
    public void testGetIntervalsFor() {
        String[] states = { "awake", "sws", "rem", "wt" };
        double[][] times = {
                { 0, 10, 90, 120, 170, 200 },   // awake
                { 10, 40, 120, 130, 160, 170 }, // sws
                { 40, 60, 130, 150 },           // rem
                { 60, 90, 150, 160 }            // wt
        };

        for (int i = 0; i < states.length; ++i) {
            List<Interval> intervals = test_hyp.getIntervalsFor(states[i]);
            assertEquals(times[i].length/2, intervals.size());

            for (int j = 0; j < times[i].length-1; j += 2)
                assertTrue(intervals.contains(Interval.make(times[i][j], times[i][j+1])));
        }

        Interval interval = Interval.make(0, 0);
        for (String state : test_hyp.getStates())
            for (Interval i : test_hyp.getIntervalsFor(state))
                interval = interval.enclose(i);
        assertEquals(interval, Interval.make(0, 200));
    }
}
