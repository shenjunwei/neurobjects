package nda.data;

import java.util.Iterator;
import java.util.List;


/**
 * Re-implementation of RateMatrixI
 * 
 * @todo documentation, tests
 * @author Nivaldo Vasconcelos
 * @author Giuliano Vilela
 */
public interface SpikeRateMatrixI extends Iterable<double[]> {
    /**
     * Returns the number of rows in the matrix
     * 
     * @return the number of rows in the matrix. If there is no valid content in
     *         matrix return a \code null value.
     */
    public int numRows();


    /**
     * Returns the number of columns in the matrix
     * 
     * @return the number of columns in the matrix. If there is no valid content in
     *         matrix return a \code null value.
     */
    public int numColumns();


    /**
     * Returns the size of bin used to build the matrix
     * 
     * it did not use any bin returns null.
     * @return size of bin used to build the matrix. it did not use any bin returns \code null.
     */
    public double getBinSize();


    public List<String> getNeuronNames();



    /**
     * Returns a double vector which is the activity population pattern in a
     * given interval. The pattern is built concatenating piece of rows (from the collumn
     * correpondent to 'a' until the collumn correspondent to 'b'.
     * 
     * Ex: In following matrix 'a' corresponds to collumn 3 and 'b' corresponds to collumn 5.
     * 
     * M = [
     * 1 1 1 1 1 1 1 1 1 1 1 1 1 1
     * 2 2 2 2 2 2 2 2 2 2 2 2 2 2
     * 3 3 3 3 3 3 3 3 3 3 3 3 3 3
     * 4 4 4 4 4 4 4 4 4 4 4 4 4 4]
     * 
     * The result will be:
     * p = [1 1 1 2 2 2 3 3 3 4 4 4];
     */
    public double[] getPattern(Interval interval);


    /**
     * Returns a double vector which is the activity population pattern using
     * the current cursor position as begin and the current window width.
     * 
     * Ex: In following matrix the cursor is 2 and window width is 3.
     * 
     * M = [ 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 2 2 2 2 3 3 3 3 3 3
     * 3 3 3 3 3 3 3 3 4 4 4 4 4 4 4 4 4 4 4 4 4 4]
     * 
     * The result will be: p = [1 1 1 2 2 2 3 3 3 4 4 4];
     * 
     * Every time that that this method is the cursor position is incremeted by
     * unity.
     * 
     * @param width window width used to build the pattern
     */
    public double[] getPattern(int width);

    public double[] getPattern(double startTime, int width);

    public double[] getPattern(int column, int width);

    public List<double[]> getPatterns(Interval interval);


    /**
     * Returns the first time used to build the matrix
     * 
     * 
     * Any rate matrix is build from the observation of spike trains into a
     * given time interval I=[i,j]. This methods would return 'i' from that I
     * time interval.
     * 
     * @return first time used to build the matrix \b or \code null
     * */
    public Interval getInterval();


    /**
     * Defines the window width to the pattern
     * 
     * Define the window width to the pattern in the next get patterns
     * operations
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

    public void setStep(int _step);

    public int getStep();


    public int getCurrentColumn();

    public double getCurrentTime();


    /** Sets the title of the rate matrix
     * 
     * Some times this is useful to matrix visualization.
     * @param title a string used as title */
    public void setTitle(String title);


    /** Returns the current title of the rate matrix
     * 
     * Some times this is useful to matrix visualization.
     * @return the rate matrix title */
    public String getTitle();


    /**
     * Returns the number of patterns using a given window width
     * 
     * This number is calculated considering a slide window operation with that
     * window width.
     * 
     * @param width window to be used in the calculation.
     * */
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


    /** Increments the time cursor in the rate matrix
     * 
     * 
     * The default value is equal to first time.
     * @param time value to time cursor;
     * @exception invalid argument when time is invalid to rate matrix time interval.
     */
    public boolean setCurrentTime(double startTime);


    /** Increments the time cursor in the rate matrix
     * 
     * 
     * The default value is equal to first time.
     * @param time value to time cursor;
     * @exception invalid argument when time is invalid to rate matrix time interval.
     */
    public boolean setCurrentColumn(int column);


    /** \brief Tells if a given interval is possible within the matrix
     * 
     * */
    public boolean containsWindow(Interval interval);


    /** Informs if a given window is possible in the Count Matrix
     * Given time instant and a temporal width, informs if the respective window is possible in
     * the count matrix. In the count matrix that time window is defined by all rows and the corresponding
     * columns since corresponding column to time until the corresponding column to time+width.
     * @param time time instant where start the window
     * @param width time width of the window
     * @return TRUE if window is possible, or FALSE otherwise.
     * 
     * \todo this explanation is confusing (the documentation of this method should explain everything in other words)
     * 
     * */
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
