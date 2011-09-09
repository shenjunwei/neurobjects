package nda.analysis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.random.RandomData;

import nda.data.CountMatrix;
import nda.util.RandomUtils;


/**
 * @author Giuliano Vilela
 */
public class QualityTests {
    public static CountMatrix withNeuronDrop(
            RandomData random, CountMatrix matrix, int numDrop) {

        int numNeurons = matrix.numRows();
        int[] dropped_inds = RandomUtils.randomNSample(random, numNeurons, numDrop);

        CountMatrix cm = new CountMatrix(matrix);

        int new_sz = numNeurons - numDrop;
        int[][] new_matrix = new int[new_sz][matrix.numColumns()];

        List<String> newNames = new ArrayList<String>();

        int matrix_p = 0;
        for (int i = 0; i < numNeurons; ++i) {
            if (ArrayUtils.contains(dropped_inds, i))
                continue;

            new_matrix[matrix_p++] = matrix.getRow(i);
            newNames.add(matrix.getNeuronNames().get(i));
        }

        cm.setMatrix(new_matrix);
        cm.setNeuronNames(newNames);
        return cm;
    }


    public static CountMatrix withRandomSurrogates(
            RandomData random, CountMatrix matrix, int numSurrogates, String type) {

        int numNeurons = matrix.numRows();
        int numCols = matrix.numColumns();

        int[] surrogates = RandomUtils.randomNSample(random, numNeurons, numSurrogates);

        CountMatrix cm = new CountMatrix(matrix);

        int [][] old_matrix = matrix.getMatrix();
        int[][] new_matrix = new int[numNeurons][numCols];

        for (int i = 0; i < numNeurons; ++i) {
            if (ArrayUtils.contains(surrogates, i)) {
                if (type.equals("uniform"))
                    new_matrix[i] = uniformSurrogate(random, old_matrix[i]);
                else if (type.equals("poisson"))
                    new_matrix[i] = poissonSurrogate(random, old_matrix[i]);
                else
                    throw new IllegalArgumentException("Unknown surrogate type: " + type);
            }
            else {
                new_matrix[i] = old_matrix[i].clone();
            }
        }

        cm.setMatrix(new_matrix);
        cm.setNeuronNames(matrix.getNeuronNames());
        return cm;
    }


    private static int[] uniformSurrogate(RandomData random, int[] array) {
        int min = nda.util.ArrayUtils.getMin(array);
        int max = nda.util.ArrayUtils.getMax(array);

        int[] surrogate = new int[array.length];
        for (int i = 0; i < array.length; ++i)
            surrogate[i] = random.nextInt(min, max);

        return surrogate;
    }


    private static int[] poissonSurrogate(RandomData random, int[] array) {
        double mean = nda.util.ArrayUtils.getAverage(array);

        int[] surrogate = new int[array.length];
        for (int i = 0; i < array.length; ++i)
            surrogate[i] = Math.round(random.nextPoisson(mean));

        return surrogate;
    }
}
