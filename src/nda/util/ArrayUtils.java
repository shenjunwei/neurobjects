/**
 * 
 */
package nda.util;

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
    
    public static double getMin(double[] mat) {
        assert mat.length != 0;
        
        double min = mat[0];
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
    
    public static double getAverage(double[] mat) {
        assert mat.length != 0;
        
        double sum = 0;
        for (double x : mat) sum += x;
        
        return sum / mat.length;
    }
    
    public static boolean isSorted(List<Double> list) {
        for (int i = 1; i < list.size(); ++i)
            if (list.get(i).compareTo(list.get(i-1)) < 0)
                return false;
        
        return true;
    }
}
