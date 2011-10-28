package nda.util;

import static nda.util.ArrayUtils.average;
import static nda.util.ArrayUtils.max;
import static nda.util.ArrayUtils.min;
import static nda.util.ArrayUtils.minAndMax;
import static nda.util.ArrayUtils.isSorted;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import nda.data.Interval;


/**
 * Tests for the ArrayUtils class.
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class ArrayUtilsTest {
    private double[] v1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private double[] v2 = { 3, 1, 2 , 0, -5, 10 };

    /**
     * Test method for {@link nda.util.ArrayUtils#minAndMax(double[])}.
     */
    @Test
    public void testGetMinMax() {
        Interval i = minAndMax(v1);
        assertEquals(0, i.start(), 1e-8);
        assertEquals(9, i.end(), 1e-8);
    }

    /**
     * Test method for {@link nda.util.ArrayUtils#min(double[])}.
     */
    @Test
    public void testGetMin() {
        assertEquals(-5, min(v2), 1e-8);
    }

    /**
     * Test method for {@link nda.util.ArrayUtils#max(double[])}.
     */
    @Test
    public void testGetMax() {
        assertEquals(10, max(v2), 1e-8);
    }

    /**
     * Test method for {@link nda.util.ArrayUtils#average(double[])}.
     */
    @Test
    public void testGetAverage() {
        assertEquals(4.5, average(v1), 1e-8);
        assertEquals(1.83333333, average(v2), 1e-8);
    }

    /**
     * Test method for {@link nda.util.ArrayUtils#isSorted(java.util.List)}.
     */
    @Test
    public void testIsSorted() {
        List<Double> l1 = new ArrayList<Double>(v1.length);
        List<Double> l2 = new ArrayList<Double>(v2.length);

        for (double n : v1) l1.add(n);
        for (double n : v2) l2.add(n);

        assertTrue(isSorted(l1));
        assertFalse(isSorted(l2));
    }

}
