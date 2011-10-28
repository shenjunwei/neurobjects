package nda.data;

import java.util.Iterator;
import java.util.List;


/**
 * The SpikeRateMatrixI component is an abstract representation of a model for estimating
 * the spike rate function.
 * 
 * Besides CountMatrix, other models can be implemented that adhere to the interface,
 * such as a Gaussian model.
 * 
 * Every SpikeRateMatrixI has a notion of a pattern. A pattern is a view of the matrix
 * that was extracted from it in a specific position, using a specific length.
 * 
 * Consider a CountMatrix M corresponding to r spike trains, having c time bins for each.
 * A pattern P on M, extracted from position i with width w (where <tt>i + w <= c</tt>)
 * is a 1D vector defined as:
 * 
 * \code
 * P = concat(M[0][i:i+w-1], M[1][i:i+w-1], ..., M[r-1][i:i+w-1])
 * \endcode
 * 
 * Where concat represents the horizontal concatenation of its arguments and <tt>M[i][j:k]</tt>
 * is an 1D vector corresponding to row i and columns j through k, inclusive, of M.
 * 
 * Ex: In following matrix, <tt>i = 0, w =  3</tt>.
 *
 * \code
 * 1 1 1 1 1 1 1 1 1 1 1 1 1 1
 * 2 2 2 2 2 2 2 2 2 2 2 2 2 2
 * 3 3 3 3 3 3 3 3 3 3 3 3 3 3
 * 4 4 4 4 4 4 4 4 4 4 4 4 4 4
 * \endcode
 * 
 * The result will be: <tt>p = [1 1 1 2 2 2 3 3 3 4 4 4];</tt>
 * 
 * @author Nivaldo Vasconcelos
 * @author Giuliano Vilela
 */
public interface SpikeRateMatrixI extends Iterable<double[]> {

    /**
     * @return Number of rows in the matrix
     */
    public int numRows();


    /**
     * @return Number of columns in the matrix
     */
    public int numColumns();


    /**
     * @return The bin size used to build the matrix
     */
    public double getBinSize();


    /**
     * @return The list of names of the neurons represented by this SpikeRateMatrixI
     */
    public List<String> getNeuronNames();


    /**
     * @return Double vector which is the activity population pattern in a
     * given interval.
     */
    public double[] getPattern(Interval interval);


    /**
     * @return A pattern beginning on the current time and with the specified width
     */
    public double[] getPattern(int width);


    /**
     * @return A pattern beginning on time startTime and with the specified width
     */
    public double[] getPattern(double startTime, int width);


    /**
     * @return A pattern beginning on the specified column of the matrix and with
     * the given width
     */
    public double[] getPattern(int column, int width);


    /**
     * @return A list with all the extracted patterns in the given interval, respecting
     * the current windowWidth.
     */
    public List<double[]> getPatterns(Interval interval);


    /**
     * @return The interval in which this matrix is estimating the spike rate function
     */
    public Interval getInterval();


    /**
     * Defines the window width to the pattern
     * 
     * Define the window width to the pattern in the next get pattern operations
     * 
     * @param width window to the next patterns.
     * @throws IllegalArgumentException invalid argument when width is bigger than
     *         the number of columns.
     */
    public void setWindowWidth(int width);


    /**
     * Returns the window width to the pattern
     * 
     * Returns the window width to the pattern in the next get patterns
     * operations
     * 
     * @return the width window to the next patterns.
     */
    public int getWindowWidth();


    /**
     * @return the start column for the next get pattern operation
     */
    public int getCurrentColumn();


    /**
     * @return the start time for the next get pattern operation
     */
    public double getCurrentTime();


    /**
     * Sets the title of the rate matrix
     */
    public void setTitle(String title);


    /**
     * @return the current title of the rate matrix
     */
    public String getTitle();


    /**
     * Returns the number of patterns using a given window width
     * 
     * This number is calculated considering a slide window operation with that
     * window width.
     * 
     * @param width window to be used in the calculation.
     */
    public int numPatterns(int width);


    /**
     * Returns the number of patterns using a given window width and a initial time.
     * 
     * This number is calculated considering a slide window operation with that
     * window width since the given initial time.
     * 
     * @param width window to be used in the calculation;
     * @param startTime initial time;
     */
    public int numPatterns(double startTime, int width);


    /**
     * @return The number of patterns that can be extracted from the given interval,
     * respecting the current width
     */
    public int numPatterns(Interval interval);


    /**
     * Set the start time of the next pattern to be returned
     */
    public boolean setCurrentTime(double startTime);


    /**
     * Set the start column of the next pattern to be returned
     */
    public boolean setCurrentColumn(int column);


    /**
     * Tells if a given interval is possible within the matrix
     */
    public boolean containsWindow(Interval interval);


    /**
     * Informs if a given window is possible in the Count Matrix
     * 
     * Given time instant and a temporal width, informs if the respective window is possible in
     * the count matrix. In the count matrix that time window is defined by all rows and the corresponding
     * columns since corresponding column to time until the corresponding column to time+width.
     * 
     * @param time time instant where start the window
     * @param width time width of the window
     * @return TRUE if window is possible, or FALSE otherwise.
     * 
     * \todo this explanation is confusing (the documentation of this method should explain everything in other words)
     */
    public boolean containsWindow(double startTime, double width);


    public class PatternIterator implements Iterator<double[]> {
        private SpikeRateMatrixI rate_matrix;

        public PatternIterator(SpikeRateMatrixI m) {
            rate_matrix = m;
        }

        @Override
        public boolean hasNext() {
            int width = rate_matrix.getWindowWidth();
            return rate_matrix.numPatterns(width) > 0;
        }

        @Override
        public double[] next() {
            int width = rate_matrix.getWindowWidth();
            return rate_matrix.getPattern(width);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("can't remove a pattern");
        }
    }
}
