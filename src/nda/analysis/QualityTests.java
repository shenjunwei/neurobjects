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
        int[][] new_values = new int[new_sz][];

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
        int[] surrogate_inds = RandomUtils.randomNSample(random, numNeurons, numSurrogates);

        CountMatrix newMatrix = new CountMatrix(originalMatrix);

        // new_values shares non-modified rows with old_values
        int [][] old_values = originalMatrix.getMatrix();
        int[][] new_values = old_values.clone();

        for (int i : surrogate_inds) {
            if (type.equals("uniform"))
                new_values[i] = uniformSurrogate(random, old_values[i]);
            else if (type.equals("poisson"))
                new_values[i] = poissonSurrogate(random, old_values[i]);
            else
                throw new IllegalArgumentException("Unknown surrogate type: " + type);
        }

        newMatrix.setMatrixValues(new_values);
        return newMatrix;
    }


    public static CountMatrix withColumnSwap(
            RandomData random, CountMatrix originalMatrix, double pct) {

        CountMatrix newMatrix = new CountMatrix(originalMatrix);

        int[][] old_values = originalMatrix.getMatrix();
        int[][] new_values = matrixColumnsSwap(random, old_values, pct);

        newMatrix.setMatrixValues(new_values);
        return newMatrix;
    }


    public static CountMatrix withNeuronSwap(
            RandomData random, CountMatrix originalMatrix, int numSurrogates, double pct) {

        int numNeurons = originalMatrix.numRows();
        int[] surrogate_inds = RandomUtils.randomNSample(random, numNeurons, numSurrogates);

        CountMatrix newMatrix = new CountMatrix(originalMatrix);

        // new_values shares non-modified rows with old_values
        int [][] old_values = originalMatrix.getMatrix();
        int[][] new_values = old_values.clone();

        for (int i : surrogate_inds) {
            new_values[i] = lineColumnsSwap(random, old_values[i], pct);
        }

        newMatrix.setMatrixValues(new_values);
        return newMatrix;
    }


    public static CountMatrix withMatrixSwap(
            RandomData random, CountMatrix originalMatrix, double pct) {

        CountMatrix newMatrix = new CountMatrix(originalMatrix);

        int[][] old_values = originalMatrix.getMatrix();
        int[][] new_values = matrixElementsSwap(random, old_values, pct);

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


    private static int[][] matrixColumnsSwap(RandomData random, int[][] matrix, double pct) {
        if (pct < 0 || pct > 1)
            throw new IllegalArgumentException("Invalid pct value: " + pct);

        int numRows = matrix.length;
        int numColumns = matrix[0].length;
        int numSwaps = (int) Math.round(numColumns * pct);

        int[][] surrogate = new int[numRows][];
        for (int r = 0; r < numRows; ++r)
            surrogate[r] = matrix[r].clone();

        for (int k = 0; k < numSwaps; ++k) {
            int i = random.nextInt(0, numColumns-1);
            int j = random.nextInt(0, numColumns-1);

            for (int r = 0; r < numRows; ++r) {
                int tmp = surrogate[r][i];
                surrogate[r][i] = surrogate[r][j];
                surrogate[r][j] = tmp;
            }
        }

        return surrogate;
    }


    private static int[] lineColumnsSwap(RandomData random, int[] array, double pct) {
        if (pct < 0 || pct > 1)
            throw new IllegalArgumentException("Invalid pct value: " + pct);

        int numColumns = array.length;
        int numSwaps = (int) Math.round(numColumns * pct);

        int[] surrogate = array.clone();

        for (int k = 0; k < numSwaps; ++k) {
            int i = random.nextInt(0, numColumns-1);
            int j = random.nextInt(0, numColumns-1);

            int tmp = surrogate[i];
            surrogate[i] = surrogate[j];
            surrogate[j] = tmp;
        }

        return surrogate;
    }


    private static int[][] matrixElementsSwap(RandomData random, int[][] matrix, double pct) {
        if (pct < 0 || pct > 1)
            throw new IllegalArgumentException("Invalid pct value: " + pct);

        int numRows = matrix.length;
        int numColumns = matrix[0].length;
        int numSwaps = (int) Math.round((numRows * numColumns) * pct);

        int[][] surrogate = new int[numRows][];
        for (int r = 0; r < numRows; ++r)
            surrogate[r] = matrix[r].clone();

        for (int k = 0; k < numSwaps; ++k) {
            int a = random.nextInt(0, numRows-1);
            int b = random.nextInt(0, numColumns-1);
            int c = random.nextInt(0, numRows-1);
            int d = random.nextInt(0, numColumns-1);

            int tmp = surrogate[a][b];
            surrogate[a][b] = surrogate[c][d];
            surrogate[c][d] = tmp;
        }

        return surrogate;
    }
}
