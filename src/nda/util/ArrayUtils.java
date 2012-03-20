package nda.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nda.data.Interval;


/**
 * Utiliy functions for Java arrays.
 * 
 * @author Giuliano Vilela
 */
public final class ArrayUtils {
    public static Interval minAndMax(double[] mat) {
        assert mat.length != 0;

        double min = mat[0];
        double max = mat[0];

        for (int i = 1; i < mat.length; ++i) {
            min = Math.min(min, mat[i]);
            max = Math.max(max, mat[i]);
        }

        return Interval.make(min, max);
    }

    public static Interval minAndMax(int[] mat) {
        assert mat.length != 0;

        int min = mat[0];
        int max = mat[0];

        for (int i = 1; i < mat.length; ++i) {
            min = Math.min(min, mat[i]);
            max = Math.max(max, mat[i]);
        }

        return Interval.make(min, max);
    }

    public static double min(double[] mat) {
        assert mat.length != 0;

        double min = mat[0];
        for (int i = 1; i < mat.length; ++i)
            min = Math.min(min, mat[i]);

        return min;
    }

    public static int min(int[] mat) {
        assert mat.length != 0;

        int min = mat[0];
        for (int i = 1; i < mat.length; ++i)
            min = Math.min(min, mat[i]);

        return min;
    }

    public static double max(double[] mat) {
        assert mat.length != 0;

        double max = mat[0];
        for (int i = 1; i < mat.length; ++i)
            max = Math.max(max, mat[i]);

        return max;
    }

    public static int max(int[] mat) {
        assert mat.length != 0;

        int max = mat[0];
        for (int i = 1; i < mat.length; ++i)
            max = Math.max(max, mat[i]);

        return max;
    }

    public static double average(double[] mat) {
        assert mat.length != 0;

        double sum = 0;
        for (double x : mat) sum += x;

        return sum / mat.length;
    }

    public static double average(int[] mat) {
        assert mat.length != 0;

        int sum = 0;
        for (int x : mat) sum += x;

        return ((double) sum) / mat.length;
    }

    public static double euclideanDistance(double[] a, double[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("vectors have different dimensions");

        double sum = 0;
        for (int i = 0; i < a.length; ++i)
            sum += (a[i]-b[i]) * (a[i]-b[i]);

        return Math.sqrt(sum);
    }

    public static boolean isSorted(List<Double> list) {
        for (int i = 1; i < list.size(); ++i)
            if (list.get(i).compareTo(list.get(i-1)) < 0)
                return false;

        return true;
    }

    public static double[] extractInterval(double[] array, double a, double b) {
        int i = Arrays.binarySearch(array, a);
        if (i < 0) i = -i - 1; // see binarySearch docs

        int j = Arrays.binarySearch(array, i, array.length, b);
        if (j < 0) j = -j - 1;
        else j = j + 1;

        return Arrays.copyOfRange(array, i, j);
    }

    public static <T> String toString(T[][] m) {
        String str = "[";

        for (int i = 0; i < m.length; ++i) {
            if (i != 0) str += ",\n";
            str += Arrays.toString(m[i]);
        }

        return str + "]";
    }

    public static boolean equals(double[] a, double[] b) {
        if (a.length != b.length)
            return false;

        for (int i = 0; i < a.length; ++i)
            if (Double.compare(a[i], b[i]) != 0)
                return false;

        return true;
    }


    public static boolean equals(byte[] a, byte[] b) {
        if (a.length != b.length)
            return false;

        for (int i = 0; i < a.length; ++i)
            if (a[i] != b[i])
                return false;

        return true;
    }


    public static boolean equals(int[] a, int[] b) {
        if (a.length != b.length)
            return false;

        for (int i = 0; i < a.length; ++i)
            if (a[i] != b[i])
                return false;

        return true;
    }


    public static double[] toPrimitiveArray(List<Double> list) {
        double[] array = new double[list.size()];

        for (int i = 0; i < list.size(); ++i)
            array[i] = list.get(i);

        return array;
    }


    public static List<Double> toObjectList(double[] array) {
        List<Double> list = new ArrayList<Double>(array.length);

        for (double val : array)
            list.add(val);

        return list;
    }
}
