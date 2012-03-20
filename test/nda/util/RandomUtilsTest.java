package nda.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Giuliano Vilela
 */
public class RandomUtilsTest {

    // Make the test reproducible
    private static long RANDOM_SEED = -798115166900410024L;

    // Uncomment the following block to test with a new seed
    /*static {
        RANDOM_SEED = new Random().nextLong();
        System.out.println("RANDOM_SEED = " + RANDOM_SEED);
    }*/

    private RandomData random;

    @Before
    public void setUp() throws Exception {
        RandomDataImpl impl = new RandomDataImpl();
        impl.reSeed(RANDOM_SEED);
        random = impl;
    }


    /**
     * Test method for {@link nda.util.RandomUtils#randomNSample(org.apache.commons.math.random.RandomData, int, int)}.
     */
    @Test
    public void testRandomNSample() {
        for (int n = 30; n < 2000; ++n) {
            for (int k = n-20; k < n; k += 40) {
                int[] sample = RandomUtils.randomNSample(random, n, k);

                Set<Integer> s = new HashSet<Integer>();
                for (int x : sample) {
                    assertTrue(x < n);
                    s.add(x);
                }

                assertEquals(k, sample.length);
                assertEquals(k, s.size());
            }
        }
    }


    @Test
    public void testRandomSample() {
        int N = 1000;
        for (int T = 0; T < 50; ++T) {
            double[] array = new double[N];
            for (int i = 0; i < N; ++i) array[i] = random.nextInt(-1000, 1000);

            double[] sample = RandomUtils.randomSample(random, array, T);

            assertEquals(T, sample.length);

            for (double x : sample) {
                boolean found = false;
                for (double y : array)
                    if (Double.compare(x, y) == 0) found = true;
                assertTrue(found);
            }
        }
    }
}
