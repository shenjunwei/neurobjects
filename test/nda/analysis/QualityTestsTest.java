package nda.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nda.data.CountMatrix;
import nda.data.SpikeHandlerI;
import nda.data.text.TextSpikeHandler;


/**
 * @author Giuliano Vilela
 */
public class QualityTestsTest {
    // Make the test reproducible
    private static long RANDOM_SEED = 8146340101830722149L;

    // Uncomment the following block to test with a new seed
    /*static {
        RANDOM_SEED = new Random().nextLong();
        System.out.println("RANDOM_SEED = " + RANDOM_SEED);
    }*/


    private static RandomData random;
    private static final String spikeDir = "data/test/spikes/";
    private static SpikeHandlerI handler_all;

    private CountMatrix cm_all;


    @BeforeClass
    public static void setUpClass() throws Exception {
        handler_all = new TextSpikeHandler(spikeDir, "*");
        random = new RandomDataImpl();
        ((RandomDataImpl)random).reSeed(RANDOM_SEED);
    }


    @Before
    public void setUp() throws Exception {
        cm_all = new CountMatrix(handler_all, 0.250);
    }


    /**
     * Test method for {@link nda.analysis.QualityTests#withNeuronDrop(org.apache.commons.math.random.RandomData, nda.data.CountMatrix, int)}.
     */
    @Test
    public void testWithNeuronDrop() {
        assertEquals(10, cm_all.numRows());

        for (int k = 1; k < 10; ++k) {
            CountMatrix dropped = QualityTests.withNeuronDrop(random, cm_all, k);

            assertEquals(cm_all.numColumns(), dropped.numColumns());
            assertEquals(cm_all.getBinSize(), dropped.getBinSize(), 1e-8);
            assertEquals(cm_all.getWindowWidth(), dropped.getWindowWidth());
            assertEquals(cm_all.getCurrentColumn(), dropped.getCurrentColumn());
            assertEquals(cm_all.getInterval(), dropped.getInterval());
            assertEquals(cm_all.getTitle(), dropped.getTitle());
            assertEquals(cm_all.numRows()-k, dropped.numRows());

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
}
