package nda.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import nda.data.Interval;

import static org.junit.Assert.*;

import static nda.util.ArrayUtils.*;


/**
 * Tests for the ArrayUtils class.
 * 
 * @author giulianoxt
 */
public class ArrayUtilsTest {
    private double[] v1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private double[] v2 = { 3, 1, 2 , 0, -5, 10 };
    private double[] v3 = { };

    /**
     * Test method for {@link nda.util.ArrayUtils#getMinMax(double[])}.
     */
    @Test
    public void testGetMinMax() {
        Interval i = getMinMax(v1);
        assertEquals(0, i.start(), 1e-8);
        assertEquals(9, i.end(), 1e-8);
    }

    /**
     * Test method for {@link nda.util.ArrayUtils#getMin(double[])}.
     */
    @Test
    public void testGetMin() {
        assertEquals(-5, getMin(v2), 1e-8);
    }

    /**
     * Test method for {@link nda.util.ArrayUtils#getMax(double[])}.
     */
    @Test
    public void testGetMax() {
        assertEquals(10, getMax(v2), 1e-8);
    }

    /**
     * Test method for {@link nda.util.ArrayUtils#getAverage(double[])}.
     */
    @Test
    public void testGetAverage() {
        assertEquals(4.5, getAverage(v1), 1e-8);
        assertEquals(1.83333333, getAverage(v2), 1e-8);
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
