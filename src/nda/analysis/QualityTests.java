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


    public static CountMatrix withUniformSurrogates(
            RandomData random, CountMatrix matrix, int numSurrogates) {

        int numNeurons = matrix.numRows();
        int numCols = matrix.numColumns();

        int[] surrogates = RandomUtils.randomNSample(random, numNeurons, numSurrogates);

        CountMatrix cm = new CountMatrix(matrix);

        int [][] old_matrix = matrix.getMatrix();
        int[][] new_matrix = new int[numNeurons][numCols];

        for (int i = 0; i < numNeurons; ++i) {
            if (ArrayUtils.contains(surrogates, i)) {
                int min = nda.util.ArrayUtils.getMin(old_matrix[i]);
                int max = nda.util.ArrayUtils.getMax(old_matrix[i]);

                new_matrix[i] = new int[numCols];
                for (int j = 0; j < numCols; ++j)
                    new_matrix[i][j] = random.nextInt(min, max);
            }
            else {
                new_matrix[i] = cm.getMatrix()[i].clone();
            }
        }

        cm.setMatrix(new_matrix);
        cm.setNeuronNames(matrix.getNeuronNames());
        return cm;
    }
}
