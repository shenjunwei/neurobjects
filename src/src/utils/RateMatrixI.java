package utils;

import cern.colt.matrix.DoubleMatrix1D;

public interface RateMatrixI {
	
	public DoubleMatrix1D rawPattern (double a, double b);
	public int 			  numRows();
	public int 			  numCols();
	public double		  binSize();
	public double		  firstTime();
	public double		  lastTime();
	public void			  setWindowWidth(int width);
	public void			  getWindowWidth();
	public void			  setTitle (String title);
	public void			  getTitle (String title);
	public int			  numPatterns(int width);
	public int			  numPatterns(int width, double beginTime);
	public boolean	      setCursor(double time);
	public double		  getCursor();
	public boolean		  hasNext();

}
