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
            RandomData random, CountMatrix originalMatrix, int numDrop) {

        int numNeurons = originalMatrix.numRows();
        int[] dropped_inds = RandomUtils.randomNSample(random, numNeurons, numDrop);

        CountMatrix newMatrix = new CountMatrix(originalMatrix);

        int new_sz = numNeurons - numDrop;
        int[][] new_values = new int[new_sz][originalMatrix.numColumns()];

        List<String> newNames = new ArrayList<String>();

        int matrix_p = 0;
        for (int i = 0; i < numNeurons; ++i) {
            if (ArrayUtils.contains(dropped_inds, i))
                continue;

            new_values[matrix_p++] = originalMatrix.getRow(i);
            newNames.add(originalMatrix.getNeuronNames().get(i));
        }

        newMatrix.setMatrixValues(new_values);
        newMatrix.setNeuronNames(newNames);
        return newMatrix;
    }


    public static CountMatrix withRandomSurrogates(
            RandomData random, CountMatrix originalMatrix, int numSurrogates, String type) {

        int numNeurons = originalMatrix.numRows();
        int numCols = originalMatrix.numColumns();

        int[] surrogate_inds = RandomUtils.randomNSample(random, numNeurons, numSurrogates);

        CountMatrix newMatrix = new CountMatrix(originalMatrix);

        int [][] old_values = originalMatrix.getMatrix();
        int[][] new_values = new int[numNeurons][numCols];

        for (int i = 0; i < numNeurons; ++i) {
            if (ArrayUtils.contains(surrogate_inds, i)) {
                if (type.equals("uniform"))
                    new_values[i] = uniformSurrogate(random, old_values[i]);
                else if (type.equals("poisson"))
                    new_values[i] = poissonSurrogate(random, old_values[i]);
                else
                    throw new IllegalArgumentException("Unknown surrogate type: " + type);
            }
            else {
                new_values[i] = old_values[i].clone();
            }
        }

        newMatrix.setMatrixValues(new_values);
        return newMatrix;
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
            surrogate[i] = (int) random.nextPoisson(mean);

        return surrogate;
    }
}
