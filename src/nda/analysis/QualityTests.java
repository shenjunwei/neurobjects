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
    public static CountMatrix withNeuronDrop(RandomData random, CountMatrix matrix, int numDrop) {
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
}
