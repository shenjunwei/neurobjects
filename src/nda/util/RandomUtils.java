package nda.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math.random.RandomData;


/**
 * @author Giuliano Vilela
 */
public class RandomUtils {
    public static int nextInt(RandomData random, int lower, int upper) {
        if (lower == upper)
            return lower;
        else
            return random.nextInt(lower, upper);
    }


    public static int[] randomNSample(RandomData random, int n, int k) {
        List<Integer> all = new ArrayList<Integer>();
        for (int i = 0; i < n; ++i) all.add(i);

        Object[] obj_sample = randomSample(random, all, k);

        int[] sample = new int[obj_sample.length];
        for (int i = 0; i < obj_sample.length; ++i)
            sample[i] = (Integer) obj_sample[i];

        return sample;
    }


    public static Object[] randomSample(
            RandomData random,
            Collection<? extends Object> objects, int k) {

        if (k == 0)
            return new Object[0];
        else
            return random.nextSample(objects, k);
    }
}
