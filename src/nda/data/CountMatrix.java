package nda.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * @brief Create the Matrix of spike count given a interval
 * 
 * Considering that the spike times are stored in data files, with a specific structure
 * (one spike time per row), this class build a matrix where the correspondent spike
 * counting is stored for that given interval.
 * 
 * @author Nivaldo Vasconcelos
 * @author Giuliano Vilela
 */
public class CountMatrix implements SpikeRateMatrixI {
    private Histogram histogram;
    private int[][] matrix;

    private int cursor_pos;
    private int cursor_step;
    private int cursor_width;

    private String title;
    private List<String> neuronNames;


    public CountMatrix(SpikeHandlerI spikeHandler, int binCount) {
        Interval interval = spikeHandler.getGlobalSpikeInterval();
        histogram = new Histogram(interval, binCount);

        load(spikeHandler);
    }


    public CountMatrix(SpikeHandlerI spikeHandler, double binSize) {
        Interval interval = spikeHandler.getGlobalSpikeInterval();
        histogram = new Histogram(interval, binSize);

        load(spikeHandler);
    }


    protected void load(SpikeHandlerI spikeHandler) {
        int n_rows = spikeHandler.getNumberOfSpikeTrains();
        int n_cols = histogram.getNumberBins();
        matrix = new int[n_rows][n_cols];

        for (int r = 0; r < n_rows; ++r) {
            histogram.load(spikeHandler.getSpikeTrain(r));
            histogram.saveBinCounts(matrix[r]);
        }

        cursor_pos = 0;
        cursor_step = 1;
        cursor_width = 1;

        title = "CountMatrix";
        neuronNames = spikeHandler.getNeuronNames();
    }


    @Override
    public int numRows() {
        return matrix.length;
    }


    @Override
    public int numColumns() {
        return histogram.getNumberBins();
    }


    @Override
    public double getBinSize() {
        return histogram.getBinSize();
    }


    public int get(int row, int column) {
        return matrix[row][column];
    }


    public int[] getRow(int row) {
        return matrix[row];
    }


    public int[] getColumn(int column) {
        int[] col = new int[numRows()];

        for (int i = 0; i < numRows(); ++i)
            col[i] = matrix[i][column];

        return col;
    }


    public int[][] getMatrix() {
        return matrix;
    }


    @Override
    public List<String> getNeuronNames() {
        return neuronNames;
    }


    @Override
    public String toString() {
        return title +
        " cursor:" + cursor_pos +
        " width:" + cursor_width +
        " matrix:" + Arrays.toString(matrix);
    }


    @Override
    public double[] getPattern(Interval interval) {
        if (!containsWindow(interval)) {
            throw new IllegalArgumentException(
            "interval lies outside the spike activity boundary");
        }

        int startBin = histogram.getBinFor(interval.start());
        int endBin = histogram.getBinFor(interval.end());
        return getRawPattern(startBin, endBin);
    }


    @Override
    public double[] getPattern(int width) {
        if (!containsWindow(cursor_pos, width)) {
            throw new IllegalArgumentException(
            "there is no pattern starting from cursor with the desired width");
        }

        int startBin = cursor_pos;
        cursor_pos += cursor_step;

        return getRawPattern(startBin, startBin+width-1);
    }


    @Override
    public double[] getPattern(double startTime, int width) {
        if (!containsWindow(startTime, width)) {
            throw new IllegalArgumentException(
            "there is no pattern starting from startTime with the desired width");
        }

        int startBin = histogram.getBinFor(startTime);
        return getRawPattern(startBin, startBin+width-1);
    }


    @Override
    public double[] getPattern(int column, int width) {
        if (!containsWindow(column, width)) {
            throw new IllegalArgumentException(
            "there is no pattern starting from column with the desired width");
        }

        return getRawPattern(column, column+width-1);
    }


    @Override
    public List<double[]> getPatterns(Interval interval) {
        setCurrentTime(interval.start());
        int estimate = numPatterns(cursor_width);

        List<double[]> patterns = new ArrayList<double[]>(estimate);

        for (double[] pattern : this) {
            if (getCurrentTime() > interval.end()) break;
            patterns.add(pattern);
        }

        return patterns;
    }


    protected double[] getRawPattern(int startBin, int endBin) {
        int length = endBin - startBin + 1;
        int pattern_sz = length * numRows();
        double[] pattern = new double[pattern_sz];

        for (int r = 0, p = 0; r < numRows(); ++r)
            for (int c = startBin; c <= endBin; ++c)
                pattern[p++] = matrix[r][c];

        return pattern;
    }


    @Override
    public Iterator<double[]> iterator() {
        return new PatternIterator(this);
    }


    @Override
    public Interval getInterval() {
        return histogram.getInterval();
    }


    @Override
    public void setWindowWidth(int width) {
        if (width >= numColumns())
            throw new IllegalArgumentException("width exceeds the matrix dimensions");

        cursor_width = width;
    }


    @Override
    public int getWindowWidth() {
        return cursor_width;
    }


    @Override
    public int getCurrentColumn() {
        if (cursor_pos >= numColumns())
            return -1;
        else
            return cursor_pos;
    }


    @Override
    public double getCurrentTime() {
        return histogram.getTimeForBin(cursor_pos);
    }


    @Override
    public void setTitle(String _title) {
        title = _title;
    }


    @Override
    public String getTitle() {
        return title;
    }


    @Override
    public int numPatterns(int width) {
        if (cursor_pos + width <= numColumns())
            return numColumns()-cursor_pos-width+1;
        else
            return 0;
    }


    @Override
    public int numPatterns(double startTime, int width) {
        if (!getInterval().contains(startTime))
            return 0;

        int p = histogram.getBinFor(startTime);
        return Math.max(0, numColumns()-p-width);
    }


    @Override
    public boolean setCurrentTime(double startTime) {
        try {
            cursor_pos = histogram.getBinFor(startTime);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    @Override
    public boolean setCurrentColumn(int column) {
        if (0 <= column && column < numColumns()) {
            cursor_pos = column;
            return true;
        }
        else {
            return false;
        }
    }


    @Override
    public boolean containsWindow(Interval interval) {
        return getInterval().contains(interval);
    }


    @Override
    public boolean containsWindow(double startTime, double width) {
        return containsWindow(Interval.make(startTime, startTime + width));
    }


    protected boolean containsWindow(int column, int width) {
        return column + width <= numColumns();
    }


    @Override
    public void setStep(int step) {
        if (step <= 0)
            throw new IllegalArgumentException("step must be positive");

        cursor_step = step;
    }


    @Override
    public int getStep() {
        return cursor_step;
    }
}
