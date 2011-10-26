package nda.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nda.data.text.TextBehaviorHandler;
import nda.data.text.TextSpikeHandler;


/**
 * Tests for the CountMatrix class.
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class CountMatrixTest {
    private static final double EPS = 1e-10;
    private static final String spikeDir = "data/test/spikes/";
    private static final String testDir = "data/test/test_spikes/";

    private static SpikeHandlerI handler_v1;
    private static SpikeHandlerI handler_test;
    private static SpikeHandlerI handler_s1;

    private CountMatrix cm_v1;
    private CountMatrix cm_test;
    private CountMatrix cm_v1b;
    private CountMatrix cm_s1;


    @BeforeClass
    public static void setUpClass() throws Exception {
        handler_v1 = new TextSpikeHandler(spikeDir, "V1");
        handler_test = new TextSpikeHandler(testDir, "test");
        handler_s1 = new TextSpikeHandler(spikeDir, "S1");
    }


    @Before
    public void setUp() {
        cm_v1 = new CountMatrix(handler_v1, 0.250);
        cm_test = new CountMatrix(handler_test, 2.0);
        cm_v1b = new CountMatrix(handler_v1, 50000);
        cm_s1 = new CountMatrix(handler_s1, 0.250);
        cm_s1.setWindowWidth(10);
    }


    /**
     * Test method for {@link nda.data.CountMatrix#CountMatrix(nda.data.SpikeHandlerI, int)}.
     */
    @Test
    public void testCountMatrixSpikeHandlerIInt() {
        CountMatrix count_m = new CountMatrix(handler_v1, 10);

        assertEquals(10, count_m.numColumns());
        assertEquals(handler_v1.size(), count_m.numRows());
        assertTrue(count_m.getInterval().contains(handler_v1.getRecordingInterval()));
        assertEquals(handler_v1.getNeuronNames(), count_m.getNeuronNames());

        double exp_bs = handler_v1.getRecordingInterval().duration() / 10;
        assertEquals(exp_bs, count_m.getBinSize(), EPS);
    }


    /**
     * Test method for {@link nda.data.CountMatrix#CountMatrix(nda.data.SpikeHandlerI, double)}.
     */
    @Test
    public void testCountMatrixSpikeHandlerIDouble() {
        CountMatrix count_m = new CountMatrix(handler_test, 0.5);

        assertEquals(0.5, count_m.getBinSize(), EPS);
        assertEquals(handler_test.size(), count_m.numRows());
        assertTrue(count_m.getInterval().contains(handler_test.getRecordingInterval()));
        assertEquals(handler_test.getNeuronNames(), count_m.getNeuronNames());

        int exp_bc = (int) (handler_test.getRecordingInterval().duration() / 0.5);
        assertEquals(exp_bc, count_m.numColumns());
    }


    /**
     * Test method for {@link nda.data.CountMatrix#get(int, int)}.
     */
    @Test
    public void testGet() {
        assertEquals(2, cm_test.get(0, 1));
        assertEquals(2, cm_test.get(0, 4));
        assertEquals(0, cm_test.get(0, 5));
        assertEquals(2, cm_test.get(1, 0));
        assertEquals(0, cm_test.get(2, 0));
    }


    /**
     * Test method for {@link nda.data.CountMatrix#getColumn(int)}.
     */
    @Test
    public void testGetColumn() {
        int exp_sum = 0;
        for (SpikeTrainI st : handler_v1)
            exp_sum += st.size();

        int sum = 0;
        for (int c = 0; c < cm_v1.numColumns(); ++c)
            for (int value : cm_v1.getColumn(c))
                sum += value;

        assertEquals(exp_sum, sum);
    }


    /**
     * Test method for {@link nda.data.CountMatrix#getMatrix()}.
     */
    @Test
    public void testGetMatrix() {
        for (int r = 0; r < cm_v1.numRows(); ++r) {
            int sum = 0;
            for (int count : cm_v1.getRow(r)) sum += count;
            assertEquals(handler_v1.get(r).size(), sum);
        }
    }


    /**
     * Test method for {@link nda.data.CountMatrix#getNeuronNames()}.
     */
    @Test
    public void testGetNeuronNames() {
        assertEquals(handler_v1.getNeuronNames(), cm_v1.getNeuronNames());
        assertEquals(handler_test.getNeuronNames(), cm_test.getNeuronNames());
    }


    /**
     * Test method for {@link nda.data.CountMatrix#getPattern(nda.data.Interval)}.
     */
    @Test
    public void testGetPatternInterval() {
        double[] pattern = cm_test.getPattern(Interval.make(1, 10));
        checkPattern(pattern, cm_test, 0, 4);
    }


    /**
     * Test method for {@link nda.data.CountMatrix#getPattern(int)}.
     */
    @Test
    public void testGetPatternInt() {
        double[] p1 = cm_test.getPattern(5);
        checkPattern(p1, cm_test, 0, 4);

        double[] p2 = cm_test.getPattern(25);
        checkPattern(p2, cm_test, 1, 25);
    }


    /**
     * Test method for {@link nda.data.CountMatrix#getPattern(double, int)}.
     */
    @Test
    public void testGetPatternDoubleInt() {
        double[] p1 = cm_test.getPattern(2.5, 2);
        checkPattern(p1, cm_test, 1, 2);

        double first = handler_v1.getRecordingInterval().start();

        double[] p2 = cm_v1.getPattern(first + cm_v1.getBinSize(), 5);
        checkPattern(p2, cm_v1, 1, 5);
    }


    /**
     * Test method for {@link nda.data.CountMatrix#getPattern(int, int)}.
     */
    @Test
    public void testGetPatternIntInt() {
        checkPattern(cm_test.getPattern(3, 5), cm_test, 3, 7);
        checkPattern(cm_test.getPattern(8, 2), cm_test, 8, 9);
        checkPattern(cm_test.getPattern(0, 3), cm_test, 0, 2);
        checkPattern(cm_test.getPattern(5, 2), cm_test, 5, 6);
        checkPattern(cm_v1.getPattern(2, 40), cm_v1, 2, 41);
        checkPattern(cm_v1.getPattern(37, 58), cm_v1, 37, 94);
    }


    /**
     * Test method for {@link nda.data.CountMatrix#getPatterns(nda.data.Interval)}.
     */
    @Test
    public void testGetPatternsInterval() {
        Interval interval = cm_v1.getInterval();
        double st = interval.start();
        double end = interval.end();
        double bsz = cm_v1.getBinSize();
        double step = 0.9 * bsz;

        assertEquals(1, cm_v1.getPatterns(Interval.make(st, st+step)).size());
        assertEquals(133, cm_v1.getPatterns(Interval.make(st, st+133*bsz-step)).size());
        assertEquals(555, cm_v1.getPatterns(Interval.make(end-555*bsz+step, end)).size());

        int total = cm_v1.numPatterns(cm_v1.getInterval());
        assertEquals(total, cm_v1.getPatterns(Interval.make(st-step, end+step)).size());
        assertEquals(total, cm_v1.getPatterns(Interval.make(st-3*step, end)).size());
        assertEquals(total, cm_v1.getPatterns(Interval.make(st, end+3*step)).size());
    }


    /**
     * Test method for {@link nda.data.CountMatrix#next()}.
     */
    @Test
    public void testForeach() {
        cm_v1.setWindowWidth(17);

        int c = 0;
        for (double[] pattern : cm_v1) {
            checkPattern(pattern, cm_v1, c, c+16);
            c += 1;
        }

        cm_v1.setCurrentColumn(0);
        assertEquals(cm_v1.numPatterns(17), c);
    }


    @Test
    @SuppressWarnings("unused")
    public void testBugFix() {
        cm_v1b.setWindowWidth(49999);
        assertEquals(2, cm_v1b.numPatterns(cm_v1b.getWindowWidth()));

        int count = 0;
        for (double[] pattern : cm_v1b) count += 1;
        assertEquals(2, count);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        cm_v1.getPattern(40);
        cm_v1.getPattern(213);
        cm_v1.getPattern(12);
        cm_v1.iterator().remove();
    }


    /**
     * Test method for {@link nda.data.CountMatrix#containsWindow(nda.data.Interval)}.
     */
    @Test
    public void testContainsWindowInterval() {
        assertTrue(cm_test.containsWindow(Interval.make(5, 80)));
        assertFalse(cm_test.containsWindow(Interval.make(1, 101)));
    }


    @Test
    public void checkInvalidTimes() {
        assertFalse(cm_v1.setCurrentTime(-1));
        assertFalse(cm_test.setCurrentTime(230948920));
    }


    /**
     * Test method for {@link nda.data.CountMatrix#numPatterns(nda.data.Interval)}.
     */
    @Test
    public void testNumPatterns() {
        Interval interval_v1 = cm_v1.getInterval();
        double st = interval_v1.start();
        double end = interval_v1.end();

        double step = interval_v1.duration() * 0.03;
        for (int i = 0; i < 30; ++i) {
            if (i % 2 == 0)
                st += step;
            else
                end -= step;

            Interval interval_test = Interval.make(st, end);

            List<double[]> patterns = cm_v1.getPatterns(interval_test);
            assertEquals(patterns.size(), cm_v1.numPatterns(interval_test));
        }

        assertEquals(cm_v1.numColumns()-cm_v1.getWindowWidth()+1,
                cm_v1.numPatterns(interval_v1));
        assertEquals(cm_v1b.numColumns()-cm_v1b.getWindowWidth()+1,
                cm_v1b.numPatterns(cm_v1b.getInterval()));
        assertEquals(cm_test.numColumns()-cm_test.getWindowWidth()+1,
                cm_test.numPatterns(cm_test.getInterval()));
    }


    @Test
    public void testBugfix() throws Exception {
        SpikeHandlerI handlerAll = new TextSpikeHandler(spikeDir);
        CountMatrix cm = new CountMatrix(handlerAll, 1.0);
        cm.setWindowWidth(1);

        Interval interval = Interval.make(5808, 5812);
        assertEquals(5, cm.numPatterns(interval));
    }


    @Test
    public void testFoodPatterns() throws Exception {
        BehaviorHandlerI behavior = new TextBehaviorHandler("data/test/real_contacts.txt");
        List<Interval> intervals = behavior.getContactIntervals("food");

        cm_s1.setWindowWidth(10);

        for (Interval interval : intervals) {
            List<double[]> patterns = cm_s1.getPatterns(interval);
            assertEquals(cm_s1.numPatterns(interval), patterns.size());

            for (double[] pattern : patterns) {
                cm_s1.setCurrentTime(interval.start());

                boolean any = false;
                while (cm_s1.getCurrentTime() <= interval.end() && !any) {
                    double[] pB = cm_s1.getPattern(10);
                    any = any || nda.util.ArrayUtils.equals(pattern, pB);
                }
                assertTrue(any);
            }
        }
    }


    private void checkPattern(double[] pattern, CountMatrix m, int start_c, int end_c) {
        int exp_l = m.numRows() * (end_c-start_c+1);
        assertEquals(exp_l, pattern.length);

        for (int r = 0; r < m.numRows(); ++r) {
            int row_l = end_c - start_c + 1;
            for (int c = start_c; c <= end_c; ++c) {
                int p_i = r * row_l + c - start_c;
                assertEquals(m.get(r, c), pattern[p_i], EPS);
            }
        }
    }
}
