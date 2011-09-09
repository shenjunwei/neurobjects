/**
 * 
 */
package nda.util;

import java.util.Arrays;
import java.util.List;

import nda.data.Interval;

/**
 * @author giulianoxt
 *
 */
public final class ArrayUtils {
    public static Interval getMinMax(double[] mat) {
        assert mat.length != 0;

        double min = mat[0];
        double max = mat[0];

        for (int i = 1; i < mat.length; ++i) {
            min = Math.min(min, mat[i]);
            max = Math.max(max, mat[i]);
        }

        return Interval.make(min, max);
    }

    public static Interval getMinMax(int[] mat) {
        assert mat.length != 0;

        int min = mat[0];
        int max = mat[0];

        for (int i = 1; i < mat.length; ++i) {
            min = Math.min(min, mat[i]);
            max = Math.max(max, mat[i]);
        }

        return Interval.make(min, max);
    }

    public static double getMin(double[] mat) {
        assert mat.length != 0;

        double min = mat[0];
        for (int i = 1; i < mat.length; ++i)
            min = Math.min(min, mat[i]);

        return min;
    }

    public static int getMin(int[] mat) {
        assert mat.length != 0;

        int min = mat[0];
        for (int i = 1; i < mat.length; ++i)
            min = Math.min(min, mat[i]);

        return min;
    }

    public static double getMax(double[] mat) {
        assert mat.length != 0;

        double max = mat[0];
        for (int i = 1; i < mat.length; ++i)
            max = Math.max(max, mat[i]);

        return max;
    }

    public static int getMax(int[] mat) {
        assert mat.length != 0;

        int max = mat[0];
        for (int i = 1; i < mat.length; ++i)
            max = Math.max(max, mat[i]);

        return max;
    }

    public static double getAverage(double[] mat) {
        assert mat.length != 0;

        double sum = 0;
        for (double x : mat) sum += x;

        return sum / mat.length;
    }

    public static double getAverage(int[] mat) {
        assert mat.length != 0;

        int sum = 0;
        for (int x : mat) sum += x;

        return ((double) sum) / mat.length;
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
}
