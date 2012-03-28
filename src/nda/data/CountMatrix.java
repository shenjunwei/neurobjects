package nda.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * SpikeRateMatrixI that calculates the estimate by using the spike count on various
 * time bins.
 * 
 * In order to create a CountMatrix, we need to specify all its parameters. They are:
 * 
 *   Spike trains: a CountMatrix is associated with a SpikeHandler in order to read the
 *   activation times
 * 
 *   Bin size: size of the time bin used to count the spikes, measured in seconds.
 * 
 *   Start time: the starting point from which the pattern extraction will begin.
 * 
 *   Window width: length of a single part of a pattern.
 * 
 *   Cursor step: how many columns will the internal cursor advance each time we create a
 *   pattern based on the cursor position.
 * 
 * @author Nivaldo Vasconcelos
 * @author Giuliano Vilela
 */
public class CountMatrix implements SpikeRateMatrixI {
    private Histogram histogram;
    private int[][] matrix;

    private int cursor_pos;
    private int cursor_width;
    private double binSize;
    private Interval interval;


    private String title;
    private List<String> neuronNames;


    /**
     * Copy constructor.
     */
    public CountMatrix(CountMatrix copy) {
        histogram = copy.histogram;
        matrix = copy.matrix;
        cursor_pos = copy.cursor_pos;
        cursor_width = copy.cursor_width;
        title = copy.title;
        neuronNames = copy.neuronNames;
        binSize = this.histogram.getBinSize();
        interval = this.histogram.getInterval();
    }


    /**
     * Create a CountMatrix with a pre defined number of bins.
     */
    public CountMatrix(SpikeHandlerI spikeHandler, int binCount) {
        Interval interval = spikeHandler.getRecordingInterval();
        histogram = new Histogram(interval, binCount);
        binSize = this.histogram.getBinSize();
        interval = this.histogram.getInterval();

        load(spikeHandler);
    }


    /**
     * Create a CountMatrix with a pre defined bin size.
     */
    public CountMatrix(SpikeHandlerI spikeHandler, double binSize) {
        Interval interval = spikeHandler.getRecordingInterval();
        histogram = new Histogram(interval, binSize);
        binSize = this.histogram.getBinSize();
        interval = this.histogram.getInterval();

        load(spikeHandler);
    }

    /**
     * Create a CountMatrix based on a file, where can be found the matrix content
     */

    public CountMatrix(String fileName, double binSize) {

        this.binSize = binSize;

        ArrayList<String> data = this.readFile(fileName);
        if (!this.buildMatrixFromFile(data)) {
            System.err.println ("Problems reading matrix from file");
            return;
        }
        this.interval = new Interval (0,matrix[0].length*binSize);
        this.histogram = new Histogram(interval,binSize);
    }

    private ArrayList<String> readFile(String fileName) {
        String line = "";
        ArrayList<String> data = new ArrayList<String>();//consider using ArrayList<int>
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);//Can also use a Scanner to read the file
            while((line = br.readLine()) != null) {

                data.add(line);
            }
        }
        catch(FileNotFoundException fN) {
            fN.printStackTrace();
        }
        catch(IOException e) {
            System.out.println(e);
        }
        return data;
    }

    private boolean buildMatrixFromFile (ArrayList<String> data) {
        int numRows = data.size();
        if (numRows==0) {
            System.err.println ("Empty matrix !!");
            return false;
        }
        String tmpRow = data.get(0);
        String numbers[] = tmpRow.split("\\s+");

        int numCols = numbers.length;
        if (numCols==0) {
            System.err.println ("Empty matrix !!");
            return false;
        }
        // Allocates the matrix
        this.matrix = new int[numRows][numCols];


        for (int row=0; row<numRows; row++) {
            tmpRow = data.get(row);
            numbers = tmpRow.split("\\s+");
            for (int col=0; col<numCols; col++) {
                matrix[row][col] = Integer.parseInt(numbers [col]);
            }
        }
        return true;
    }


    protected void load(SpikeHandlerI spikeHandler) {
        int n_rows = spikeHandler.size();
        int n_cols = histogram.getNumberBins();
        matrix = new int[n_rows][n_cols];

        for (int r = 0; r < n_rows; ++r) {
            histogram.load(spikeHandler.get(r));
            histogram.saveBinCounts(matrix[r]);
        }

        cursor_pos = 0;
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
        return this.matrix[0].length;
    }


    @Override
    public double getBinSize() {
        return binSize;
        //return histogram.getBinSize();
    }


    /**
     * @return the value in the specified position of the matrix
     */
    public int get(int row, int column) {
        return matrix[row][column];
    }


    /**
     * @return A copy of the specified row of the matrix
     */
    public int[] getRow(int row) {
        return matrix[row];
    }


    public int[] getRow(String neuron) {
        return getRow(neuronNames.indexOf(neuron));
    }


    /**
     * @return A copy of the specified column of the matrix
     */
    public int[] getColumn(int column) {
        int[] col = new int[numRows()];

        for (int i = 0; i < numRows(); ++i)
            col[i] = matrix[i][column];

        return col;
    }


    /**
     * @return A shallow copy of this matrix
     */
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
        " binSize: " + this.getBinSize() +
        " numColumns: " + numColumns() +
        " neurons: " + getNeuronNames();
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

        int startBin = cursor_pos++;
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
        interval = interval.intersection(getInterval());
        int numPatterns = numPatterns(interval);

        if (numPatterns == 0) {
            return new ArrayList<double[]>();
        }

        List<double[]> patterns = new ArrayList<double[]>(numPatterns);

        setCurrentTime(interval.start());
        for (double[] pattern : this) {
            if (patterns.size() == numPatterns)
                break;
            else
                patterns.add(pattern);
        }

        return patterns;
    }


    @Override
    public int numPatterns(Interval interval) {
        interval = interval.intersection(getInterval());
        if (interval.isEmpty())
            return 0;

        try {
            int st_bin = histogram.getBinFor(interval.start());
            int end_bin = histogram.getBinFor(interval.end());

            return Math.max(0, end_bin - st_bin - cursor_width + 2);
        } catch (IllegalArgumentException e) {
            return 0;
        }
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
        return interval;
    }


    @Override
    public void setWindowWidth(int width) {
        if (width > numColumns())
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


    /**
     * @return Return the correspondent time represented by this bin
     */
    public double getTimeForBin(int bin) {
        return histogram.getTimeForBin(bin);
    }


    /**
     * @return Return the correspondent bin represented by the given instant
     */
    public int getBinForTime(double time) {
        return histogram.getBinFor(time);
    }


    @Override
    public boolean containsWindow(Interval interval) {
        return getInterval().contains(interval);
    }


    @Override
    public boolean containsWindow(double startTime, double width) {
        return containsWindow(Interval.make(startTime, startTime + width));
    }


    public void setMatrixValues(int[][] matrix) {
        this.matrix = matrix;
    }


    public void setNeuronNames(List<String> neuronNames) {
        this.neuronNames = neuronNames;
    }


    protected boolean containsWindow(int column, int width) {
        return column + width <= numColumns();
    }
}
