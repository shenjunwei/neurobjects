package nda.analysis.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nda.data.BehaviorHandlerI;
import nda.data.CountMatrix;
import nda.data.Interval;
import nda.data.text.TextBehaviorHandler;
import nda.data.text.TextSpikeHandler;
import nda.util.ArrayUtils;


/**
 * @author Giuliano Vilela
 */
public class DatasetTransformerTest {
    // Make the test reproducible
    private static long RANDOM_SEED = 2322995294324404246L;

    // Uncomment the following block to test with a new seed
    /*static {
        RANDOM_SEED = new Random().nextLong();
        System.out.println("RANDOM_SEED = " + RANDOM_SEED);
    }*/


    private static RandomData random;
    private static final String spikeDir = "data/test/spikes/";
    private static TextSpikeHandler handler_all;
    private static BehaviorHandlerI behavior;

    private CountMatrix cm_all;


    @BeforeClass
    public static void setUpClass() throws Exception {
        handler_all = new TextSpikeHandler(spikeDir, "*");
        behavior = new TextBehaviorHandler("data/test/real_contacts.txt");

        random = new RandomDataImpl();
        ((RandomDataImpl)random).reSeed(RANDOM_SEED);
    }


    @Before
    public void setUp() throws Exception {
        cm_all = new CountMatrix(handler_all, 0.250);
        assertEquals(10, cm_all.numRows());
    }


    /**
     * Test method for {@link nda.analysis.generation.DatasetTransformer#withNeuronDrop(org.apache.commons.math.random.RandomData, nda.data.CountMatrix, int)}.
     */
    @Test
    public void testWithNeuronDrop() {
        for (int k = 1; k < 10; ++k) {
            CountMatrix dropped = DatasetTransformer.withNeuronDrop(random, cm_all, k);

            assertSameParameters(cm_all, dropped);
            assertEquals(cm_all.numRows()-k, dropped.numRows());
            assertEquals(cm_all.numColumns(), dropped.numColumns());

            Set<Integer> pos_set = new HashSet<Integer>();

            for (int i = 0; i < dropped.numRows(); ++i) {
                String st = dropped.getNeuronNames().get(i);
                int pos = cm_all.getNeuronNames().indexOf(st);
                assertTrue(pos != -1);
                pos_set.add(pos);

                boolean found = false;
                int[] row = dropped.getRow(i);
                for (int j = 0; j < cm_all.numRows(); ++j) {
                    int[] ot_row = cm_all.getRow(j);
                    if (Arrays.equals(row, ot_row)) {
                        found = true;
                    }
                }
                assertTrue(found);
            }

            assertEquals(dropped.numRows(), pos_set.size());
        }
    }


    @Test
    public void testUniformSurrogates() {
        for (int k = 1; k <= 10; ++k) {
            CountMatrix sur_matrix = DatasetTransformer.withRandomSurrogates(
                    random, cm_all, k, "uniform");

            assertSameParameters(cm_all, sur_matrix);
            assertSameDimensions(cm_all, sur_matrix);

            int num_diff = 0;
            for (int i = 0; i < sur_matrix.numRows(); ++i) {
                assertEquals(
                        cm_all.getNeuronNames().get(i),
                        sur_matrix.getNeuronNames().get(i));

                int[] row_a = sur_matrix.getRow(i);
                int[] row_b = cm_all.getRow(i);

                if (!Arrays.equals(row_a, row_b))
                    num_diff++;

                assertTrue(ArrayUtils.getMin(row_a) >= ArrayUtils.getMin(row_b));
                assertTrue(ArrayUtils.getMax(row_a) <= ArrayUtils.getMax(row_b));
            }

            assertEquals(k, num_diff);
        }
    }


    @Test
    public void testPoissonSurrogates() {
        for (int k = 1; k <= 10; ++k) {
            CountMatrix sur_matrix = DatasetTransformer.withRandomSurrogates(
                    random, cm_all, k, "poisson");

            assertSameParameters(cm_all, sur_matrix);
            assertSameDimensions(cm_all, sur_matrix);

            int num_diff = 0;
            for (int i = 0; i < sur_matrix.numRows(); ++i) {
                assertEquals(
                        cm_all.getNeuronNames().get(i),
                        sur_matrix.getNeuronNames().get(i));

                int[] row_a = sur_matrix.getRow(i);
                int[] row_b = cm_all.getRow(i);

                if (!Arrays.equals(row_a, row_b))
                    num_diff++;

                // may fail sometimes due to randomness
                assertEquals(ArrayUtils.getAverage(row_a), ArrayUtils.getAverage(row_b), 1e-1);
            }

            assertEquals(k, num_diff);
        }
    }


    @Test
    public void testColumnSwapSurrogates() {
        double[] pct_values = { 0.0, 0.1, 0.3, 0.5, 0.7, 0.8, 1.0 };

        for (double pct : pct_values) {
            CountMatrix sur_matrix = DatasetTransformer.withColumnSwap(random, cm_all, pct);

            assertSameParameters(cm_all, sur_matrix);
            assertSameDimensions(cm_all, sur_matrix);

            for (int r = 0; r < cm_all.numRows(); ++r) {
                int[] row_a = cm_all.getRow(r);
                int[] row_b = sur_matrix.getRow(r);

                assertEquals(ArrayUtils.getAverage(row_a), ArrayUtils.getAverage(row_b), 1e-8);
                assertEquals(ArrayUtils.getMin(row_a), ArrayUtils.getMin(row_b));
                assertEquals(ArrayUtils.getMax(row_a), ArrayUtils.getMax(row_b));

                if (pct == 0.0)
                    assertTrue(Arrays.equals(row_a, row_b));
                else
                    assertFalse(Arrays.equals(row_a, row_b));
            }

            int num_diff = 0;

            for (int c = 0; c < sur_matrix.numColumns(); ++c) {
                boolean equals = true;

                for (int r = 0; r < sur_matrix.numRows() && equals; ++r)
                    if (sur_matrix.get(r, c) != cm_all.get(r, c))
                        equals = false;

                if (!equals) ++num_diff;
            }

            int numSwaps = (int) Math.round(pct * cm_all.numColumns());
            assertTrue(num_diff >= (2*numSwaps / 3.0));
            assertTrue(num_diff <= (numSwaps*2));
        }
    }


    @Test
    public void testNeuronSwapSurrogates() {
        double[] pct_values = { 0.0, 0.4, 0.8, 1.0 };

        for (int numSurrogates = 1; numSurrogates <= 10; ++numSurrogates) {
            for (double pct : pct_values) {

                CountMatrix sur_matrix = DatasetTransformer.withNeuronSwap(
                        random, cm_all, numSurrogates, pct);

                assertSameParameters(cm_all, sur_matrix);
                assertSameDimensions(cm_all, sur_matrix);

                int diff_rows = 0;
                int numSwaps = (int) Math.round(pct * cm_all.numColumns());

                for (int r = 0; r < cm_all.numRows(); ++r) {
                    int[] row_a = cm_all.getRow(r);
                    int[] row_b = sur_matrix.getRow(r);

                    assertEquals(ArrayUtils.getAverage(row_a), ArrayUtils.getAverage(row_b), 1e-8);
                    assertEquals(ArrayUtils.getMin(row_a), ArrayUtils.getMin(row_b));
                    assertEquals(ArrayUtils.getMax(row_a), ArrayUtils.getMax(row_b));

                    if (pct == 0.0) {
                        assertTrue(Arrays.equals(row_a, row_b));
                    }
                    else if (!Arrays.equals(row_a, row_b)) {
                        diff_rows++;

                        int diff_cols = 0;
                        for (int c = 0; c < cm_all.numColumns(); ++c)
                            if (sur_matrix.get(r, c) != cm_all.get(r, c))
                                diff_cols++;

                        // may fail sometimes due to randomness
                        assertTrue(diff_cols >= (numSwaps / 800));
                        assertTrue(diff_cols <= (numSwaps*2));
                    }
                }

                if (pct > 0.0)
                    assertEquals(numSurrogates, diff_rows);
                else
                    assertEquals(0, diff_rows);
            }
        }
    }


    @Test
    public void testMatrixSwapSurrogates() {
        double[] pct_values = { 0.0, 0.1, 0.3, 0.5, 0.7, 0.8, 1.0 };

        for (double pct : pct_values) {
            CountMatrix sur_matrix = DatasetTransformer.withMatrixSwap(random, cm_all, pct);

            assertSameParameters(cm_all, sur_matrix);
            assertSameDimensions(cm_all, sur_matrix);

            int num_diff = 0;
            for (int r = 0; r < sur_matrix.numRows(); ++r)
                for (int c = 0; c < sur_matrix.numColumns(); ++c)
                    if (sur_matrix.get(r, c) != cm_all.get(r, c))
                        num_diff++;

            int numSwaps = (int) Math.round(pct * (cm_all.numColumns() * cm_all.numRows()));
            assertTrue(num_diff >= (numSwaps / 7));
            assertTrue(num_diff <= (numSwaps*2));
        }
    }


    @Test
    public void testColumnSwapDistSurrogates() throws Exception {
        double[] pct_values = { 0.0, 0.2, 0.5, 0.8, 1.0 };
        double[] dist_values = { 0.0, 2.5, 5.3, 22 };

        for (double pct : pct_values) for (double dist : dist_values) {
            CountMatrix sur_matrix = DatasetTransformer.withColumnSwapDist(
                    random, cm_all, pct, dist);

            assertSameParameters(cm_all, sur_matrix);
            assertSameDimensions(cm_all, sur_matrix);

            int numRows = sur_matrix.numRows();
            int numColumns = sur_matrix.numColumns();
            int numSwaps = (int) Math.round(pct * numColumns);

            for (int r = 0; r < numRows; ++r) {
                int[] row_a = cm_all.getRow(r);
                int[] row_b = sur_matrix.getRow(r);

                if (numSwaps == 0 || dist == 0.0) {
                    assertTrue(ArrayUtils.equals(row_a, row_b));
                    continue;
                }

                assertEquals(ArrayUtils.getMin(row_a), ArrayUtils.getMin(row_b));
                assertEquals(ArrayUtils.getMax(row_a), ArrayUtils.getMax(row_b));
                assertEquals(ArrayUtils.getAverage(row_a), ArrayUtils.getAverage(row_b), 1e-8);

                int num_diff = 0;
                for (int c = 0; c < numColumns; ++c)
                    if (row_a[c] != row_b[c])
                        num_diff++;

                if (dist != 0.0) {
                    assertTrue(num_diff > numSwaps/1000);
                    assertTrue(num_diff <= numSwaps*2);
                }
            }
        }
    }


    @Test
    public void testPoissonDistSurrogates() throws Exception {
        double[] dist_values = { 0.3, 0.5, 1.0, 2.5, 5.0, 10, 15, 20 };

        for (double dist : dist_values) {
            CountMatrix sur_matrix = DatasetTransformer.withDistSurrogates(
                    random, cm_all, "poisson_d", dist);

            assertSameParameters(cm_all, sur_matrix);
            assertSameDimensions(cm_all, sur_matrix);

            int numRows = sur_matrix.numRows();
            int numColumns = sur_matrix.numColumns();

            for (int r = 0; r < numRows; ++r) {
                int[] row_a = cm_all.getRow(r);
                int[] row_b = sur_matrix.getRow(r);

                assertEquals(ArrayUtils.getAverage(row_a), ArrayUtils.getAverage(row_b), 1e-2);

                double t0 = sur_matrix.getInterval().start();
                double t1 = t0 + dist;
                int dist_bins = sur_matrix.getBinForTime(t1);

                if (dist_bins > 10) {
                    for (int st_c = 0; st_c < numColumns; st_c += dist_bins) {
                        int end_c = Math.min(st_c+dist_bins-1, numColumns-1);

                        int[] window_a = Arrays.copyOfRange(row_a, st_c, end_c+1);
                        int[] window_b = Arrays.copyOfRange(row_b, st_c, end_c+1);

                        assertEquals(
                                ArrayUtils.getAverage(window_a),
                                ArrayUtils.getAverage(window_b), 1.5);
                    }
                }
            }
        }
    }


    @Test
    public void testUniformDistSurrogates() throws Exception {
        double[] dist_values = { 0.5, 0.6, 1.0, 2.5, 5.0, 10, 15, 20 };

        for (double dist : dist_values) {
            CountMatrix sur_matrix = DatasetTransformer.withDistSurrogates(
                    random, cm_all, "uniform_d", dist);

            assertSameParameters(cm_all, sur_matrix);
            assertSameDimensions(cm_all, sur_matrix);

            int numRows = sur_matrix.numRows();
            int numColumns = sur_matrix.numColumns();

            int num_diff = 0;
            for (int r = 0; r < numRows; ++r) {
                int[] row_a = cm_all.getRow(r);
                int[] row_b = sur_matrix.getRow(r);

                if (!ArrayUtils.equals(row_a, row_b))
                    num_diff++;

                assertTrue(ArrayUtils.getMin(row_b) >= ArrayUtils.getMin(row_a));
                assertTrue(ArrayUtils.getMax(row_b) <= ArrayUtils.getMax(row_a));

                double t0 = sur_matrix.getInterval().start();
                double t1 = t0 + dist;
                int dist_bins = sur_matrix.getBinForTime(t1);

                for (int st_c = 0; st_c < numColumns; st_c += dist_bins) {
                    int end_c = Math.min(st_c+dist_bins-1, numColumns-1);

                    int[] window_a = Arrays.copyOfRange(row_a, st_c, end_c+1);
                    int[] window_b = Arrays.copyOfRange(row_b, st_c, end_c+1);

                    assertTrue(ArrayUtils.getMin(window_b) >= ArrayUtils.getMin(window_a));
                    assertTrue(ArrayUtils.getMax(window_b) <= ArrayUtils.getMax(window_a));
                }
            }

            assertTrue(num_diff >= (2*numRows)/3);
        }
    }


    @Test
    public void testSpikeJitterSurrogates() throws Exception {
        double[] dist_values = { 0.0, 0.5, 0.6, 1.0, 2.5, 5.0, 10, 15, 20, 25, 30 };

        for (double dist : dist_values) {
            TextSpikeHandler surHandler = DatasetTransformer.withSpikeJitter(
                    random, handler_all, dist);

            assertEquals(handler_all.getAnimalName(), surHandler.getAnimalName());
            assertEquals(handler_all.getFilter(), surHandler.getFilter());

            assertEquals(handler_all.getNeuronNames(), surHandler.getNeuronNames());
            assertEquals(handler_all.getNumberOfSpikeTrains(), surHandler.getNumberOfSpikeTrains());
            assertEquals(handler_all.getSourceType(), surHandler.getSourceType());

            Interval oldInt = handler_all.getGlobalSpikeInterval();
            Interval newInt = surHandler.getGlobalSpikeInterval();
            assertEquals(oldInt.start(), newInt.start(), dist);
            assertEquals(oldInt.end(), newInt.end(), dist);

            for (int i = 0; i < surHandler.getNumberOfSpikeTrains(); ++i) {
                double[] old_times = handler_all.getSpikeTrain(i).getTimes();
                double[] new_times = surHandler.getSpikeTrain(i).getTimes();

                for (int j = 0; j < old_times.length; ++j)
                    assertEquals(old_times[j], new_times[j], dist);
            }
        }
    }


    @Test
    public void testMeanDistSurrogates() throws Exception {
        double[] dist_values = { 0.5, 0.6, 1.0, 2.5, 5.0, 10, 15, 20 };

        for (double dist : dist_values) {
            CountMatrix sur_matrix = DatasetTransformer.withDistSurrogates(
                    random, cm_all, "mean_d", dist);

            assertSameParameters(cm_all, sur_matrix);
            assertSameDimensions(cm_all, sur_matrix);

            int numRows = sur_matrix.numRows();
            int numColumns = sur_matrix.numColumns();

            int num_diff = 0;
            for (int r = 0; r < numRows; ++r) {
                int[] row_a = cm_all.getRow(r);
                int[] row_b = sur_matrix.getRow(r);

                if (!ArrayUtils.equals(row_a, row_b))
                    num_diff++;

                assertEquals(ArrayUtils.getAverage(row_a), ArrayUtils.getAverage(row_b), 0.3);

                double t0 = sur_matrix.getInterval().start();
                double t1 = t0 + dist;
                int dist_bins = sur_matrix.getBinForTime(t1);

                for (int st_c = 0; st_c < numColumns; st_c += dist_bins) {
                    int end_c = Math.min(st_c+dist_bins-1, numColumns-1);

                    int[] window_a = Arrays.copyOfRange(row_a, st_c, end_c+1);
                    int[] window_b = Arrays.copyOfRange(row_b, st_c, end_c+1);

                    int mean = (int) Math.round(ArrayUtils.getAverage(window_a));
                    for (int value : window_b)
                        assertEquals(mean, value);
                }
            }

            assertTrue(num_diff >= (2*numRows)/3);
        }
    }


    @Test
    public void testContactSwapSurrogates() {
        double[] pct_values = { 0.0, 0.3, 0.5, 0.8, 1.0 };

        List<Integer> contact_inds = new ArrayList<Integer>();
        for (String label : behavior.getLabelSet()) {
            for (Interval interval : behavior.getContactIntervals(label)) {
                int c1 = cm_all.getBinForTime(interval.start());
                int c2 = cm_all.getBinForTime(interval.end());
                contact_inds.add(c1);
                contact_inds.add(c2);
            }
        }

        for (double pct : pct_values) {
            CountMatrix sur_matrix = DatasetTransformer.withContactSwap(
                    random, cm_all, behavior, pct);

            assertSameParameters(cm_all, sur_matrix);
            assertSameDimensions(cm_all, sur_matrix);

            int numContactCols = 0;
            int numDiffContactCols = 0;
            int numContactEls = 0;
            int numDiffContactEls = 0;

            for (int c = 0; c < cm_all.numColumns(); ++c) {
                boolean found = false;
                for (int i = 0; i < contact_inds.size()-1 && !found; i += 2)
                    if (c >= contact_inds.get(i) && c <= contact_inds.get(i+1))
                        found = true;

                if (!found) {
                    for (int r = 0; r < cm_all.numRows(); ++r)
                        assertEquals(cm_all.get(r, c), sur_matrix.get(r, c));
                }
                else {
                    ++numContactCols;
                    for (int r = 0; r < cm_all.numRows(); ++r) {
                        if (cm_all.get(r, c) != sur_matrix.get(r, c)) {
                            ++numDiffContactCols;
                            break;
                        }
                    }

                    numContactEls += cm_all.numRows();
                    for (int r = 0; r < cm_all.numRows(); ++r)
                        if (cm_all.get(r, c) != sur_matrix.get(r, c))
                            ++numDiffContactEls;
                }
            }

            assertTrue(numDiffContactCols <= numContactCols);
            assertTrue(numDiffContactCols >= (int)((7/8.)*pct*numContactCols));

            assertTrue(numDiffContactEls <= numContactEls);
            assertTrue(numDiffContactEls <= 2*(int)(pct*numContactEls));
            assertTrue(numDiffContactEls >= (int)((1/7.)*pct*numContactEls));
        }
    }


    @Test
    public void testContactShiftSurrogates() {
        double[] t0_values = { 0, 300, 400, 1000, 5000 };

        for (double t0 : t0_values) {
            BehaviorHandlerI shift_behavior = DatasetTransformer.withContactShift(
                    random, behavior, t0);

            assertEquals(behavior.getLabelSet(), shift_behavior.getLabelSet());

            Interval old_expo = behavior.getExpositionInterval();
            Interval new_expo = shift_behavior.getExpositionInterval();

            assertEquals(t0, new_expo.start(), 1e-8);
            assertEquals(old_expo.duration(), new_expo.duration(), 1e-8);

            double offset = new_expo.start() - old_expo.start();
            assertTrue(offset != 0);

            for (String label : behavior.getLabelSet()) {
                List<Interval> old_intervals = behavior.getContactIntervals(label);
                List<Interval> new_intervals = shift_behavior.getContactIntervals(label);

                assertEquals(old_intervals.size(), new_intervals.size());

                for (int i = 0; i < new_intervals.size(); ++i) {
                    Interval old_int = old_intervals.get(i);
                    Interval new_int = new_intervals.get(i);

                    assertEquals(old_int.start()+offset, new_int.start(), 1e-8);
                    assertEquals(old_int.end()+offset, new_int.end(), 1e-8);
                }
            }
        }
    }


    private void assertSameParameters(CountMatrix a, CountMatrix b) {
        assertEquals(a.getBinSize(), b.getBinSize(), 1e-8);
        assertEquals(a.getWindowWidth(), b.getWindowWidth());
        assertEquals(a.getCurrentColumn(), b.getCurrentColumn());
        assertEquals(a.getInterval(), b.getInterval());
        assertEquals(a.getTitle(), b.getTitle());
    }


    private void assertSameDimensions(CountMatrix a, CountMatrix b) {
        assertEquals(a.numColumns(), b.numColumns());
        assertEquals(a.numRows(), b.numRows());
        assertEquals(a.getNeuronNames(), b.getNeuronNames());
    }
}