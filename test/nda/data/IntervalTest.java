package nda.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * \defgroup UnitTests Unit Tests
 */

/**
 * Test the Interval class.
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class IntervalTest {
    final Interval norm_i = Interval.make(0, 10);
    final Interval point_i = Interval.make(5, 5);
    final Interval neg_i = Interval.make(-5, 15);
    final Interval out_i = Interval.make(20, 30);
    final double EPS = 1e-8;

    /**
     * Test method for {@link nda.data.Interval#make(double, double)}.
     */
    @Test
    public void testMake() {
        assertEquals(0, norm_i.start(), EPS);
        assertEquals(10, norm_i.end(), EPS);
    }

    /**
     * Test method for {@link nda.data.Interval#duration()}.
     */
    @Test
    public void testDuration() {
        assertEquals(10, norm_i.duration(), EPS);
        assertEquals(0, point_i.duration(), EPS);
    }

    /**
     * Test method for {@link nda.data.Interval#contains(double)}.
     */
    @Test
    public void testContainsDouble() {
        assertTrue(norm_i.contains(5));
        assertTrue(norm_i.contains(0));
        assertTrue(norm_i.contains(10));
        assertFalse(norm_i.contains(-5));
        assertTrue(point_i.contains(5));
    }

    /**
     * Test method for {@link nda.data.Interval#contains(nda.data.Interval)}.
     */
    @Test
    public void testContainsInterval() {
        assertTrue(norm_i.contains(point_i));
        assertTrue(norm_i.contains(norm_i));
        assertTrue(neg_i.contains(norm_i));
        assertFalse(point_i.contains(norm_i));
    }

    /**
     * Test method for {@link nda.data.Interval#isValid()}.
     */
    @Test
    public void testIsValid() {
        assertTrue(neg_i.isValid());
        assertTrue(point_i.isValid());
        assertFalse(norm_i.intersection(out_i).isValid());
    }

    /**
     * Test method for {@link nda.data.Interval#intersection(nda.data.Interval)}.
     */
    @Test
    public void testIntersection() {
        assertTrue(neg_i.intersection(norm_i).isValid());
        assertEquals(0, neg_i.intersection(norm_i).start(), EPS);
        assertEquals(10, neg_i.intersection(norm_i).end(), EPS);
        assertFalse(norm_i.intersection(out_i).isValid());
        assertEquals(0, norm_i.intersection(point_i).duration(), EPS);
    }

    /**
     * Test method for {@link nda.data.Interval#isEmpty()}.
     */
    @Test
    public void testIsEmpty() {
        assertFalse(norm_i.isEmpty());
        assertFalse(neg_i.isEmpty());
        assertTrue(norm_i.intersection(out_i).isEmpty());
    }

    /**
     * Test pre-defined intervals
     */
    @Test
    public void testPreDefined() {
        assertTrue(Interval.EMPTY.isEmpty());
        assertFalse(Interval.EMPTY.isValid());
        assertFalse(Interval.INF.isEmpty());
        assertTrue(Interval.INF.isValid());
        assertTrue(Interval.INF.contains(neg_i));
        assertFalse(norm_i.contains(Interval.INF));
    }

    @Test
    public void testEnclose() {
        assertEquals(Interval.INF, norm_i.enclose(Interval.INF));
        assertEquals(neg_i, norm_i.enclose(neg_i));
        assertEquals(neg_i, neg_i.enclose(norm_i));
    }
}
