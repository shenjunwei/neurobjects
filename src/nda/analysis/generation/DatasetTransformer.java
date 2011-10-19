package nda.analysis.generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;

import nda.data.BehaviorHandlerI;
import nda.data.CountMatrix;
import nda.data.Interval;
import nda.data.SpikeTrain;
import nda.data.text.TextBehaviorHandler;
import nda.data.text.TextSpikeHandler;
import nda.util.RandomUtils;


/**
 * @author Giuliano Vilela
 */
public class DatasetTransformer {

    public static boolean needsSpikeTrainTransform(GeneratorSetup.Dataset dataset) {
        return dataset.getParameter("surrogate") != null &&
                dataset.getParameter("surrogate_type").equals("spike_jitter");
    }

    // Victor
    public static boolean needsVarianceContactsTransform(GeneratorSetup.Dataset dataset) {
        return dataset.getParameter("surrogate") != null &&
                dataset.getParameter("surrogate_type").equals("var_contacts");
    }

    public static boolean needsRateMatrixTransform(GeneratorSetup.Dataset dataset) {
        return (dataset.getParameter("neuron_drop") != null ||
                dataset.getParameter("surrogate") != null &&
                !dataset.getParameter("surrogate_type").equals("spike_jitter") &&
                !dataset.getParameter("surrogate_type").equals("contact_shift") &&
                !dataset.getParameter("surrogate_type").equals("var_contacts"));
    }


    public static boolean needsBehaviorHandlerTransform(GeneratorSetup.Dataset dataset) {
        return (dataset.getParameter("surrogate") != null &&
                dataset.getParameter("surrogate_type").equals("contact_shift"));
    }


    public static TextSpikeHandler applySpikeTrainTransform(
            RandomData random, TextSpikeHandler spikeHandler,
            GeneratorSetup.Dataset dataset) {

        if (!needsSpikeTrainTransform(dataset))
            throw new IllegalArgumentException("Dataset doesn't need a transform");

        if (dataset.getParameter("surrogate_type").equals("spike_jitter")) {
            double dist = (Double) dataset.getParameter("dist_surrogate");
            return withSpikeJitter(random, spikeHandler, dist);
        }
        else {
            throw new IllegalArgumentException("Illegal surrogate_type value");
        }
    }

    public static CountMatrix applyRateMatrixTransform(
            RandomData random, CountMatrix rateMatrix,
            GeneratorSetup.Dataset dataset, BehaviorHandlerI behavior) {

        // neuron dropping
        if (dataset.getParameter("num_drop") != null) {
            int numDrop = (Integer) dataset.getParameter("num_drop");
            return withNeuronDrop(random, rateMatrix, numDrop);
        }
        // per neuron surrogate (uniform, poisson or neuron_swap)
        else if (dataset.getParameter("num_surrogate") != null) {
            int numSurrogates = (Integer) dataset.getParameter("num_surrogate");
            String surrogateType = (String) dataset.getParameter("surrogate_type");

            if (surrogateType.equals("neuron_swap")) {
                double pct = (Double) dataset.getParameter("pct_surrogate");
                return withNeuronSwap(random, rateMatrix, numSurrogates, pct);
            }
            else {
                return withRandomSurrogates(
                        random, rateMatrix, numSurrogates, surrogateType);
            }
        }
        // full matrix surrogate
        else if (dataset.getParameter("pct_surrogate") != null) {
            double pctSurrogates = (Double) dataset.getParameter("pct_surrogate");

            if (dataset.getParameter("surrogate_type").equals("col_swap")) {
                return withColumnSwap(random, rateMatrix, pctSurrogates);
            }
            else if (dataset.getParameter("surrogate_type").equals("col_swap_d")) {
                double distSurrogates = (Double) dataset.getParameter("dist_surrogate");
                return withColumnSwapDist(random, rateMatrix, pctSurrogates, distSurrogates);
            }
            else if (dataset.getParameter("surrogate_type").equals("contact_swap")) {
                return withContactSwap(random, rateMatrix, behavior, pctSurrogates);
            }
            else {
                return withMatrixSwap(random, rateMatrix, pctSurrogates);
            }
        }
        // poisson_d, uniform_d, average_d
        else if (dataset.getParameter("dist_surrogate") != null) {
            String sur_type = (String) dataset.getParameter("surrogate_type");
            double distSurrogates = (Double) dataset.getParameter("dist_surrogate");

            return withDistSurrogates(random, rateMatrix, sur_type, distSurrogates);
        }
        else {
            throw new IllegalArgumentException("Dataset doesn't need a transform");
        }
    }


    public static BehaviorHandlerI applyBehaviorHandlerTransform(
            RandomData random, GeneratorSetup.Dataset dataset,
            BehaviorHandlerI behavior) {

        String sur_type = (String) dataset.getParameter("surrogate_type");

        if (sur_type.equals("contact_shift")) {
            double t0 = (Double) dataset.getParameter("dist_surrogate");
            return withContactShift(random, behavior, t0);
        }
        else {
            throw new IllegalArgumentException("Dataset doesn't need a transform");
        }
    }

    // Victor
    public static BehaviorHandlerI applyVarianceContactsTransform(
            RandomData random, GeneratorSetup.Dataset dataset,
            BehaviorHandlerI behavior) {

        String sur_type = (String) dataset.getParameter("surrogate_type");

        if (sur_type.equals("var_contacts")) {
            double t0 = (Double) dataset.getParameter("val_surrogate");
            String var_type = (String) dataset.getParameter("method_surrogate");
            return withVarianceContacts(random, behavior, t0, var_type);
        }
        else {
            throw new IllegalArgumentException("Dataset doesn't need a transform");
        }
    }



    /**
     * neuron_drop(K)
     */
    protected static CountMatrix withNeuronDrop(
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


    /**
     * uniform(K) or poisson(K)
     */
    protected static CountMatrix withRandomSurrogates(
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


    /**
     * col_swap(P)
     */
    protected static CountMatrix withColumnSwap(
            RandomData random, CountMatrix originalMatrix, double pct) {

        CountMatrix newMatrix = new CountMatrix(originalMatrix);

        int[][] old_values = originalMatrix.getMatrix();
        int[][] new_values = matrixColumnsSwap(random, old_values, pct);

        newMatrix.setMatrixValues(new_values);
        return newMatrix;
    }


    /**
     * neuron_swap(K,P)
     */
    protected static CountMatrix withNeuronSwap(
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


    /**
     * matrix_swap(P)
     */
    protected static CountMatrix withMatrixSwap(
            RandomData random, CountMatrix originalMatrix, double pct) {

        CountMatrix newMatrix = new CountMatrix(originalMatrix);

        int[][] old_values = originalMatrix.getMatrix();
        int[][] new_values = matrixElementsSwap(random, old_values, pct);

        newMatrix.setMatrixValues(new_values);
        return newMatrix;
    }


    /**
     * neuron_swap(K,P)
     */
    protected static CountMatrix withColumnSwapDist(
            RandomData random, CountMatrix originalMatrix,
            double pct, double dist) {

        double t0 = originalMatrix.getInterval().start();
        double t1 = t0 + dist;
        int dist_bins = originalMatrix.getBinForTime(t1); // dist in bin units

        // shuffle matrix
        CountMatrix newMatrix = new CountMatrix(originalMatrix);

        int[][] old_values = originalMatrix.getMatrix();
        int[][] new_values = matrixSwapDist(random, old_values, pct, dist_bins);

        newMatrix.setMatrixValues(new_values);
        return newMatrix;
    }


    /**
     * poisson_d(D)
     */
    protected static CountMatrix withDistSurrogates(
            RandomData random, CountMatrix originalMatrix, String type, double dist) {

        double t0 = originalMatrix.getInterval().start();
        double t1 = t0 + dist;
        int dist_bins = originalMatrix.getBinForTime(t1); // dist in bin units

        if (dist_bins <= 0)
            throw new IllegalArgumentException("Invalid value for dist: " + dist);

        // shuffle matrix
        CountMatrix newMatrix = new CountMatrix(originalMatrix);

        int[][] old_values = originalMatrix.getMatrix();
        int[][] new_values = matrixRandomDist(random, old_values, type, dist_bins);

        newMatrix.setMatrixValues(new_values);
        return newMatrix;
    }


    /**
     * spike_jitter(D)
     */
    protected static TextSpikeHandler withSpikeJitter(
            RandomData random, TextSpikeHandler spikeHandler, double dist) {

        TextSpikeHandler newHandler = new TextSpikeHandler(spikeHandler);

        for (SpikeTrain spikeTrain : newHandler.getAllSpikeTrains())
            applyJitter(random, spikeTrain.getTimes(), dist);

        return newHandler;
    }


    /**
     * contact_swap(P)
     */
    protected static CountMatrix withContactSwap(
            RandomData random, CountMatrix originalMatrix,
            BehaviorHandlerI behavior, double pct) {

        // Get contact indexes
        List<Integer> contact_inds = new ArrayList<Integer>();

        for (String label : behavior.getLabelSet()) {
            for (Interval interval : behavior.getContactIntervals(label)) {
                interval = interval.intersection(originalMatrix.getInterval());
                if (!interval.isEmpty()) {
                    int i1 = originalMatrix.getBinForTime(interval.start());
                    int i2 = originalMatrix.getBinForTime(interval.end());
                    contact_inds.add(i1);
                    contact_inds.add(i2);
                }
            }
        }

        CountMatrix newMatrix = new CountMatrix(originalMatrix);

        int[][] old_values = originalMatrix.getMatrix();
        int[][] new_values = matrixIntervalsSwap(random, old_values, contact_inds, pct);

        newMatrix.setMatrixValues(new_values);
        return newMatrix;
    }


    /**
     * contact_shift(T0)
     */
    protected static BehaviorHandlerI withContactShift(
            RandomData random, BehaviorHandlerI old_behavior, double new_t0) {

        BehaviorHandlerI behavior = new TextBehaviorHandler(old_behavior);

        double old_t0 = behavior.getExpositionInterval().start();
        double offset = new_t0 - old_t0;

        for (String label : behavior.getLabelSet()) {
            List<Interval> intervals = behavior.getContactIntervals(label);
            List<Interval> new_intervals = new ArrayList<Interval>(intervals.size());

            for (Interval interval : intervals) {
                double a = interval.start();
                double b = interval.end();

                Interval shift = Interval.make(a+offset, b+offset);
                new_intervals.add(shift);
            }

            behavior.setContactIntervals(label, new_intervals);
        }

        return behavior;
    }

    // Victor
    protected static BehaviorHandlerI withVarianceContacts(
            RandomData random, BehaviorHandlerI old_behavior, double new_t0, String var_type) {

        BehaviorHandlerI behavior = new TextBehaviorHandler(old_behavior);

        RandomData rand = new RandomDataImpl();

        double offset1, offset2;

        for (String label : behavior.getLabelSet()) {
            List<Interval> intervals = behavior.getContactIntervals(label);
            List<Interval> new_intervals = new ArrayList<Interval>(intervals.size());

            for (Interval interval : intervals) {
                double a = interval.start();
                double b = interval.end();

                Interval shift;

                try{
                    offset1 = rand.nextGaussian(0, new_t0); // Distribuiçao Normal, efeito aditivo
                    offset2 = rand.nextGaussian(0, new_t0); // Distribuiçao Normal, efeito aditivo
                } catch(Exception e){
                    offset1 = 0.0;
                    offset2 = 0.0;
                }

                if(var_type.equals("ab")){
                    if(a+offset1 >= b+offset2) continue; // Elimina intervalo.
                    shift = Interval.make(a+offset1, b+offset2);
                }
                else if(var_type.equals("a")){
                    if(a+offset1 >= b) continue; // Elimina intervalo.
                    shift = Interval.make(a+offset1, b);
                }
                else{
                    if(a >= b+offset2) continue; // Elimina intervalo.
                    shift = Interval.make(a, b+offset2);
                }

                System.out.printf("%f | (%f,%f) | (%f,%f)", new_t0, a, b, shift.start(), shift.end());
                System.out.println();
                new_intervals.add(shift);
            }

            behavior.setContactIntervals(label, new_intervals);
        }

        return behavior;
    }

    /*
     * Helper methods for the transforms
     */

    private static int[] uniformSurrogate(RandomData random, int[] array) {
        int min = nda.util.ArrayUtils.getMin(array);
        int max = nda.util.ArrayUtils.getMax(array);

        int[] surrogate = new int[array.length];
        for (int i = 0; i < array.length; ++i)
            surrogate[i] = RandomUtils.nextInt(random, min, max);

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
            int i = RandomUtils.nextInt(random, 0, numColumns-1);
            int j = RandomUtils.nextInt(random, 0, numColumns-1);

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
            int i = RandomUtils.nextInt(random, 0, numColumns-1);
            int j = RandomUtils.nextInt(random, 0, numColumns-1);

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

            int a = RandomUtils.nextInt(random, 0, numRows-1);
            int b = RandomUtils.nextInt(random, 0, numColumns-1);
            int c = RandomUtils.nextInt(random, 0, numRows-1);
            int d = RandomUtils.nextInt(random, 0, numColumns-1);

            int tmp = surrogate[a][b];
            surrogate[a][b] = surrogate[c][d];
            surrogate[c][d] = tmp;
        }

        return surrogate;
    }


    private static int[][] matrixSwapDist(
            RandomData random, int[][] matrix, double pct, int dist) {

        if (pct < 0 || pct > 1)
            throw new IllegalArgumentException("Invalid pct value: " + pct);

        int numRows = matrix.length;
        int numColumns = matrix[0].length;
        int numSwaps = (int) Math.round(numColumns * pct);

        int[][] surrogate = new int[numRows][];
        for (int r = 0; r < numRows; ++r)
            surrogate[r] = matrix[r].clone();

        for (int r = 0; r < numRows; ++r) {
            int[] swap_inds = RandomUtils.randomNSample(random, numColumns, numSwaps);

            for (int i : swap_inds) {
                int a = Math.max(0, i-dist);
                int b = Math.min(i+dist, numColumns-1);
                int j = RandomUtils.nextInt(random, a, b);

                int tmp = surrogate[r][i];
                surrogate[r][i] = surrogate[r][j];
                surrogate[r][j] = tmp;
            }
        }

        return surrogate;
    }


    private static int[][] matrixRandomDist(
            RandomData random, int[][] matrix, String type, int dist) {

        int numRows = matrix.length;
        int numColumns = matrix[0].length;

        int[][] surrogate = new int[numRows][];
        for (int r = 0; r < numRows; ++r)
            surrogate[r] = new int[numColumns];

        for (int r = 0; r < numRows; ++r) {
            for (int st_c = 0; st_c < numColumns; st_c += dist) {
                int end_c = Math.min(st_c+dist-1, numColumns-1);

                if (type.equals("uniform_d")) {
                    int window_min = Integer.MAX_VALUE, window_max = Integer.MIN_VALUE;
                    for (int c = st_c; c <= end_c; ++c) {
                        window_min = Math.min(window_min, matrix[r][c]);
                        window_max = Math.max(window_max, matrix[r][c]);
                    }

                    for (int c = st_c; c <= end_c; ++c)
                        if (window_min != window_max)
                            surrogate[r][c] = random.nextInt(window_min, window_max);
                        else
                            surrogate[r][c] = window_min;
                }
                else if (type.equals("poisson_d") || type.equals("mean_d")) {
                    long sum = 0;
                    for (int c = st_c; c <= end_c; ++c)
                        sum += matrix[r][c];

                    double window_avg = sum / (double)(end_c-st_c+1);
                    for (int c = st_c; c <= end_c; ++c) {
                        if (type.equals("poisson_d")) {
                            if (window_avg > 0)
                                surrogate[r][c] = (int) random.nextPoisson(window_avg);
                            else
                                surrogate[r][c] = 0;
                        }
                        else {
                            surrogate[r][c] = (int) Math.round(window_avg);
                        }
                    }
                }
                else {
                    throw new IllegalArgumentException("Illegal type value");
                }
            }
        }

        return surrogate;
    }


    private static void applyJitter(RandomData random, double[] array, double eps) {
        for (int i = 0; i < array.length; ++i) {
            double value = array[i];
            double min = Math.max(value-eps, 0);
            double max = value+eps;

            if (min != max)
                array[i] = random.nextUniform(min, max);
        }

        Arrays.sort(array);
    }


    private static int[][] matrixIntervalsSwap(
            RandomData random, int[][] matrix,
            List<Integer> intervals, double pct) {

        if (pct < 0 || pct > 1)
            throw new IllegalArgumentException("Invalid pct value: " + pct);

        int numRows = matrix.length;

        int[][] surrogate = new int[numRows][];
        for (int r = 0; r < numRows; ++r)
            surrogate[r] = matrix[r].clone();

        for (int i = 0; i < intervals.size()-1; i += 2) {
            int c1 = intervals.get(i);
            int c2 = intervals.get(i+1);
            int numSwaps = (int)((c2-c1+1)*numRows*pct);

            for (int k = 0; k < numSwaps; ++k) {
                int rA = RandomUtils.nextInt(random, 0, numRows-1);
                int rB = RandomUtils.nextInt(random, 0, numRows-1);
                int cA = RandomUtils.nextInt(random, c1, c2);
                int cB = RandomUtils.nextInt(random, c1, c2);

                int tmp = surrogate[rA][cA];
                surrogate[rA][cA] = surrogate[rB][cB];
                surrogate[rB][cB] = tmp;
            }
        }

        return surrogate;
    }
}
