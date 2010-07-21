package data;

import java.util.ArrayList;

import cern.colt.matrix.DoubleMatrix1D;

/** \page RateMatrixCursor Rate Matrix Cursor 
 * 
 * To help on repeated operation of reading from a Rate Matrix was though the cursor concept
 * Its default value is equal to first time of Rate Matrix and every reading operation using the
 * getNextPattern the cursor value is incremented to the next column. */

/**
 * \brief Defines a rate matrix
 * 
 * A rate matrix stores information about rates from a set of spike trains, one
 * row per spike train.
 */
public interface RateMatrixI {
	
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
	//public DoubleMatrix1D rawPattern (double a, double b);
	
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
	//public DoubleMatrix1D rawPattern (int width);
	
	/**
	 * Informs if there is a next pattern.
	 * 
	 * Based on current cursor position and window width, informs if there is a
	 * pattern available for reading from the Rate Matrix.
	 * 
	 * @return \code true if there is a pattern available for reading from the
	 *         Rate Matrix, \b or \code false otherwise;
	 * 
	 */
	public boolean	  hasNext();
	
	
	/**
	 * \brief Returns the pattern in current cursor.
	 * 
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
	 * @return a \code double vector with the pattern \b or a \code null value
	 *         if the matrix content is not valid.
	 */
	/*public DoubleMatrix1D getPattern(); */

	
	/**
	 * Returns the number of rows in the matrix
	 * 
	 * @return the number of rows in the matrix. If there is no valid content in
	 *         matrix return a \code null value.
	 */
	public int 			  numRows();
	
	/**
	 * Returns the number of columns in the matrix
	 * 
	 * @return the number of columns in the matrix. If there is no valid content in
	 *         matrix return a \code null value.
	 */
	public int 			  numCols();
	
	/**
	 * Returns the size of bin used to build the matrix
	 * 
	 * it did not use any bin returns null.
	 * @return size of bin used to build the matrix. it did not use any bin returns \code null.        
	 */
	public double		  binSize();
	
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
	public double		  firstTime();
	
	/**
	 * Returns the last time used to build the matrix
	 * 
	 * 
	 * Any rate matrix is build from the observation of spike trains into a
	 * given time interval I=[i,j]. This methods would return 'j' from that I
	 * time interval.
	 * 
	 * @return last time used to build the matrix \b or \code null 
	 * */
	public double		  lastTime();
	
	/**
	 * Defines the window width to the pattern
	 * 
	 * 
	 * Define the window width to the pattern in the next get patterns
	 * operations
	 * 
	 * @param width window to the next patterns.
	 * @exception invalid argument when width is bigger than the number of columns.
	 *            
	 * */
	public void		  setWindowWidth(int width) throws Exception;
	
	/**
	 * Returns the window width to the pattern
	 * 
	 * 
	 * Returns the window width to the pattern in the next get patterns
	 * operations
	 * 
	 * @param the width window to the next patterns.
	 *            
	 * */
	public int		  getWindowWidth();
	
	/** Sets the title of the rate matrix
	 * 
	 * Some times this is useful to matrix visualization. 
	 * @param title a string used as title */
	public void			  setTitle (String title);
	
	/** Returns the current title of the rate matrix
	 * 
	 * Some times this is useful to matrix visualization. 
	 * @return the rate matrix title */
	public String			  getTitle ();
	
	/**
	 * Returns the number of patterns using a given window width
	 * 
	 * This number is calculated considering a slide window operation with that
	 * window width.
	 * 
	 * @param width window to be used in the calculation.
	 * */
	public int			  numPatterns(int width);
	
	/**
	 * Returns the number of patterns using a given window width and a initial time.
	 * 
	 * This number is calculated considering a slide window operation with that
	 * window width since the given initial time. 
	 * 
	 * @param width window to be used in the calculation;
	 * @param beginTime initial time;
	 * */
	public int			  numPatterns(int width, double beginTime);
	
	/** Increments the time cursor in the rate matrix
	 * 
	 *  
	 * The default value is equal to first time.
	 * @param time value to time cursor; 
	 * @exception invalid argument when time is invalid to rate matrix time interval.
	 */
	public boolean  incCursor(int inc);
	
	/** \brief Return the average of a row 
	 * 
	 * @param row index of the row; 
	 * @return the value of average of given row \b or NaN if the matrix content is not valid */
	public double   avgRow (int idx);
	
	/** \brief Return the average of a column 
	 * 
	 * @param neuron index of the column; 
	 * @return the value of average of given column \b or NaN if the matrix content is not valid; */
	public double   avgColumn (int idx);
	
	//TODO Doc
	/** Returns a set of pattern give a time interval */
	public ArrayList<DoubleMatrix1D> getPatterns(double t1, double t2);
	
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
	public boolean windowPossible(double time,double width);
	
	
	public boolean possibleInterval (double t1, double t2);
	

}
