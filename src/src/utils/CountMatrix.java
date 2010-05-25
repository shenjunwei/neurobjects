package utils;

import hep.aida.IHistogram1D;
import hep.aida.ref.Histogram1D;
//import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
//import java.io.*;
import java.util.*;

//import errors.InvalidArgumentException;

/** @brief Create the Matrix of spike count given a interval
 * 
 * Considering that the spike times are stored in data files, with a specific structure (one spike time per row), 
 * this class build a matrix where the correspondent spike counting is stored for that given interval. 
 * 
 * The matrix is implemented using the Colt class termed DoubleMatrix2D.
 * 
 *  @see DoubleMatrix2D
 */
public class CountMatrix implements RateMatrixI{
	
	/** Size of bins used to build the count. Using this bin size, in seconds, are defined the count intervals. Given 
	 * the time interval where the count is taken, I=[a,b], this interval is considered in sub-intervals I'_1 = [a,a+binSize]
	 * I'_2 = [a+binSize;a+2*binSize] ... I'_k = [(k-1)*binSize;k*binSize] ... I'_M=[(M-1)binSize; b]*/
	private double					binSize=0.0; 
	
	/**Path to dataset where the data files are stored. */
	//private String					datasetPath = ""; 
	
	/** First time instant of the interval where the spikes will be counted  */
	private double					first=0.0;
	
	/** Size of the histogram which stores the spike count */
	private int 					histSize = 0; 
	
		
	/** Last time instant of the interval where the spikes will be counted  */
	private double					last=0.0; 
	
	/** The last valid position of atual window on binMatrix  */
	//private int lastValidWindowPosition=-1;
	
	/** String where are stored the log msgs */
	private String 					log = "";
	
	/**  Matrix where are stored all count for the given interval. Each row stores the spike count for a neuron  */
	public int						matrix[][]; 
	
	/** Minimum number of spikes to consider a spike train as valid within a interval */
	//private int						minSpikes = 10; 
	
	/** List of string where are stored the name of neurons used to build the Count Matrix */
	private ArrayList<String> 		neuronNames = null; 
	
	/** Number of collumns */
	private int						numberOfCols = 0;
	
	/**  Number of rows  */
	private int						numberOfRows = 0; 
	
	/* Attributes used to control inner operations */
	
	/** Stores the information about the Count Matrix validity. The Count Matrix is good to be used when this value is true */
	private boolean					valid = false;
	
	/** The cursor position of window inner binMatrix. */
	private int cursor=0;
	/** Window size */
	private int windowWidth=0;
	
	private String title="";
	
	
	public CountMatrix (SpkHandlerI spikes, double binSize) {
		
		
		
		if (!this.setupInterval(binSize, spikes.beginInterval(), spikes.endInterval())) {
			this.log = "Error: Invalid time interval\n";
			return;
		}
		
		this.numberOfRows = spikes.getNumberOfNeurons();
		this.numberOfCols = (int) Math.floor((this.last-this.first)/binSize);
		this.neuronNames = spikes.getNeuronNames();
		
		 
		if (!this.createMatrix2D()) {
			this.valid = false;
			this.log = this.log + "Problems creating Count Matrix !!\n";
		}
		IHistogram1D  h1 = new Histogram1D("H",this.histSize,this.first,this.last);
		
		int numberOfNeurons = spikes.getNumberOfNeurons();
		for (int i=0; i<numberOfNeurons; i++) {
			h1.reset();
			if (this.insertNeuronSpikes(h1,spikes.getSpikeTrain(i))) {
				if (!this.fillMatrixRow2D(h1, i)) {
					return;
				}
				
			}
			if (!this.isValid()){
				return;
			}
		}
		
	}
	
	
	/* ------------------------------------------------------------------------------------------ */
	/** Creates the Count Matrix 2D.
	 * 
	 * Using internal information about the Count Matrix dimensions, creates a DenseDoubleMatrix2D 
	 * matrix where will be stored the spike trains count for all neurons.
	 * 
	 * @return TRUE operation was successful, or FALSE otherwise.
	 */
	private boolean createMatrix2D() {
		this.matrix = new int[this.numberOfRows][this.numberOfCols];
		
		if (this.matrix!=null) {
			return true;
		}
		return false;
		
	}
	/* ------------------------------------------------------------------------------------------ */
	
	/* ------------------------------------------------------------------------------------------ */
	/** Fills a Count Matrix row with values from a histogram.
	 * 
	 * The Count Matrix row and the histogram matrix must have compatible. In other words,
	 * the number of entries in the histogram shall be not smaller than number of columns in the count
	 * matrix row.
	 * 
	 * @param h1 histogram to be inserted in the Count Matrix.
	 * @param row the Count Matrix row where the count should be inserted.
	 * 
	 * @return TRUE operation was successful, or FALSE otherwise.
	 * @see IHistogram1D
	 */
	private boolean fillMatrixRow2D (IHistogram1D h1, int row) {
		
		/*if (h1.entries()<this.numberOfCols) {
			this.valid = false;
			this.log = this.log + "fillMatrixRow2D: Histogram not smaller than number of columns\n";
			return (false);
		} */
		for (int column = 0; column < this.numberOfCols; column++) {
			this.matrix[row][column]=h1.binEntries(column);
		}
		return (true);
		
	}
	/* ------------------------------------------------------------------------------------------ */
	
	/* ------------------------------------------------------------------------------------------ */
	/** Returns a index in the Count Matrix of a given time 
	 * 
	 * @param time which wants the index in the Count Matrix.
	 * @return index in the Count Matrix of the given time*/
	public int getIdx(double time) {
		if ((time<this.first) || (time>this.last)) {
			return -1;
		}
		return ((int)Math.floor((time-this.first)/this.binSize));
	}
	/* ------------------------------------------------------------------------------------------ */
	
	
	/* ------------------------------------------------------------------------------------------ */
	/** Returns the length of a time in columns in the Count Matrix 
	 * 
	 * Given a time, since zero, this method returns the number of columns in Count Matrix corresponding
	 * to this time.  
	 * 
	 * @param time value to be converted in number of columns in Count Matrix.
	 * @return number of number of columns in Count Matrix corresponding
	 * to given time.  */
	public int getLen (double time) {
		return (this.getIdx(this.first+time));
	}
	/* ------------------------------------------------------------------------------------------ */
	
	/* ------------------------------------------------------------------------------------------ */
	/** Returns the log messages
	 * 
	 * This is useful when is looking for cause of problems. 
	 * 
	 *  @return String with all log messages */
	public String getLog() {
		return (this.log);
	}
	/* ------------------------------------------------------------------------------------------ */
	
	
	/* ------------------------------------------------------------------------------------------ */
	/** Returns the neuron names. 
	 * 
	 * Returns the neuron names, in a String vector. The names are in alphabetical order. 
	 * @return a String[] vector with the neuron names */
	public String[] getNeuronsNames() {
		return ((String []) this.neuronNames.toArray() );
	}
	/* ------------------------------------------------------------------------------------------ */
	
	// TODO Doc
	/** Returns a set of pattern give a time interval */
	public ArrayList<DoubleMatrix1D> getPatterns(double t1, double t2) {
	
		ArrayList<DoubleMatrix1D> patterns = null;
		if (t1>t2) {
			this.log += "CountMatrix:getPatterns: invalid arguments";
			return (patterns);
		}
		
		if ( (!this.windowPossible(t1,this.windowWidth)) || (!this.windowPossible(t2,this.windowWidth)))  {
			this.log += "CountMatrix:getPatterns: invalid input arguments can not possible windows";
			return (patterns);
		}
		 
		
		int lastCol = this.getIdx(t2);
		patterns = new ArrayList<DoubleMatrix1D> ();
		for (int i=this.getIdx(t1); i<=lastCol; i++) {
			patterns.add(this.getPattern(i, this.windowWidth));
		}
		
		return (patterns);
	}
	
	/** Given the column position and window width, returns an array of spikes of Count Matrix.
	  *  
	 * @param firstCol : first column from which the pattern will be formed.
	 * @param windowWidth : window width to form the pattern.
	 * @return pattern: a int vector with the correspondent pattern derive from the window.
	 * 
	 * todo: check it !!
	 */
	private DoubleMatrix1D getPattern(int firstCol, int windowWidth){
		
		String errorMsg = "";
		// checks the firstCol input parameter
		if ( (firstCol<0) || (firstCol>(this.numberOfCols-windowWidth))) {
			errorMsg = "Error:BinMatrix:getPattern:invalid firstCol input>>"+firstCol;
			this.log = this.log + errorMsg;
			//System.out.println(errorMsg);
			return (null);
			
		}
		// checks the windowWidth input parameter
		if ((windowWidth<0) || (windowWidth>this.numberOfCols) ) {
			errorMsg = "Error:BinMatrix:getPattern:invalid windowWidth input>>"+windowWidth;
			this.log = this.log + errorMsg;
			//System.out.println(errorMsg);
			return (null);
		}
		
		DoubleMatrix1D pattern = new DenseDoubleMatrix1D(this.numberOfRows*windowWidth);
		int colLimit = firstCol + windowWidth;
		 
		
		for (int row=0, i=0; row<this.numberOfRows; row++) {
			for (int col=firstCol; col<colLimit; col++,i++) {		
				pattern.setQuick(i,this.matrix[row][col]);
			}
		} 
		return (pattern);
	}
	
	
	
	public DoubleMatrix1D getPattern() {
		
		DoubleMatrix1D pattern = new DenseDoubleMatrix1D(this.numberOfRows*this.windowWidth);
		
		String errorMsg = "";
		int firstCol = this.cursor;
		 
		// checks the firstCol input parameter
		if ( (firstCol<0) || (firstCol>(this.numberOfCols-windowWidth))) {
			errorMsg = "Error:BinMatrix:getPattern:invalid firstCol input>>"+firstCol;
			this.log = this.log + errorMsg;
			//System.out.println(errorMsg);
			return (null);
			
		}
		// checks the windowWidth input parameter
		if ((windowWidth<0) || (windowWidth>this.numberOfCols) ) {
			errorMsg = "Error:BinMatrix:getPattern:invalid windowWidth input>>"+windowWidth;
			this.log = this.log + errorMsg;
			//System.out.println(errorMsg);
			return (null);
		}
		
	//	int pattern[] = new int[this.numberOfRows*windowWidth];
		int colLimit = firstCol + windowWidth;
		 
		
		for (int row=0, i=0; row<this.numberOfRows; row++) {
			for (int col=firstCol; col<colLimit; col++,i++) {		
				pattern.setQuick(i, this.matrix[row][col]);
			}
		} 
		this.cursor++;
		return (pattern);
			
		
		
	}
	
	
	/* ------------------------------------------------------------------------------------------ */
	/** Inserts spike trains for a given neuron in the Count Matrix.
	 * 
	 *  Given a histogram and a spike train this method:
	 *   (1) Reads the corresponding data file where the spikes train is stored;
	 *   (2) Build the count of that spikes train in the given histogram;    
	 *   
	 *  The data file should be in the directory dataset defined when the object CountMatrix was created.
	 *  This method reads from that file and stores the count in the histogram. To prevent read from files
	 *  where there is no minimum spike number is recommended use before this method CheckNeuros, which removes
	 *  from internal list all invalid neurons in that directory.
	 *  
	 * @param h1 histogram used 
	 * @return TRUE operation was successful, or FALSE otherwise.
	 * @see IHistogram1D */
	private boolean insertNeuronSpikes(IHistogram1D h1, SpikeTrain spikes) {
		
		double spikeTime = 0;
		int numOfSpikes = -1;
		DoubleMatrix1D spike = spikes.getTimes();
		int i=0;
		spikeTime = spike.get(i++);
		int spikeTrainSize = spike.size();
		while ((spikeTime < this.last) && (i<spikeTrainSize) ) {
			if ((spikeTime >= this.first) && (spikeTime <= this.last)) {
				numOfSpikes++;
				h1.fill(spikeTime);
			}
			spikeTime = spike.get(i++);
		}
		
		if (numOfSpikes==0) {
			this.valid = false;
			this.log = this.log + "Problems reading spikes from " +  spikes.getName()+"\n";
		
		}
		else {
			this.valid = true;
		}
		return (this.valid);
	}
	/* ------------------------------------------------------------------------------------------ */
	
	/* ------------------------------------------------------------------------------------------ */
	/** Informs if the current Count Matrix is valid.
	 * 
	 * This method should be callled before the use Count Matrix. 
	 * 
	 * @return TRUE means that the count Matrix is valid e can be used, or FALSE otherwise.*/
	public boolean isValid() {
		return (this.valid);
	}
	/* ------------------------------------------------------------------------------------------ */
	
	/* ------------------------------------------------------------------------------------------ */
	/** Returns the number of neurons used to build the matrix. 
	 * 
	 * If there is no spikes for a neuron within given interval this neuron is not counted. 
	 * @return the number of neurons in the Count Matrix */
	public int numberOfNeurons() {
		return (this.neuronNames.size());
	}
	/* ------------------------------------------------------------------------------------------ */
	
	
	/* ------------------------------------------------------------------------------------------ */
	/** Setups the internal time interval.
	 * 
	 * 
	 *  @param binSize size of bin used to build the count matrix (seconds).  
	 *  @param a begin of time interval to build the count matrix (seconds).
	 *  @param b end of time interval to build the count matrix (seconds).
	 * */
	private boolean setupInterval(double binSize, double a, double b) {
		
		
		if ( (a==Double.NaN) || (b==Double.NaN) ) {
			return false;	
		}
		if (a>b) {
			double tmp = a;
			a = b;
			b = tmp;
		}
		this.first = (Math.floor(a/binSize)*binSize); //defines the biggest bin limit smaller than a;
		this.last = (Math.ceil(b/binSize)*binSize);   //defines the smallest bin limit bigger than b;
		this.histSize = (int)((last-first)/binSize);
		this.binSize = binSize;
		
		return true;
	}
	
	
	/** Shows the Count Matrix informations
	 * 
	 * Using the internal method toString shows informations about the Count Matrix 
	 * */
	public void show () {
		
	    System.out.println(this.toString());

	}
	
	/** Shows the Count Matrix 
	 * 
	 * Shows, only, the Count Matrix elements, one Count Matrix row per line */
	public void showMatrix2D () {
		String str="";
		
		for (int row=0; row < this.numberOfRows; row++) {
			str=str+this.neuronNames.get(row)+"\t";
	          for (int column=0; column < this.numberOfCols; column++) {
	        	  str=str+this.matrix[row][column]+"\t";
	          }
	          str=str+"\n";
	   }
		System.out.println(str);
		
	}
	
	/* ------------------------------------------------------------------------------------------ */
	/** Shows the neurons names.
	 * 
	 * Shows, in a line, the neuron names */
	public void showNeuronNames() {
		int numberOfNeurons=this.numberOfNeurons();
		System.out.print("Neuron names: ");
		for (int i=0; i<numberOfNeurons; i++) {
			System.out.print (this.neuronNames.get(i)+" ");
		}
		System.out.print("\n");
	}
	/* ------------------------------------------------------------------------------------------ */
	
	/** Convert in a String the Count Matrix information 
	 * 
	 * Are shown: time interval, bin size and Count Matrix elements; */
	public String toString () {
		String str="Interval(s): ["+this.first+";"+this.last+"]\tSize of bin(s):"+this.binSize+"\t\t";
		
		str=str+"Dimension: "+this.numberOfRows+"x"+this.numberOfCols+"\n";
		for (int row=0; row < this.numberOfRows; row++) {
			  str=str+this.neuronNames.get(row)+"\t";
	          for (int column=0; column < this.numberOfCols; column++) {
	        	  str=str+this.matrix[row][column]+"\t";
	          }
	          str=str+"\n";
	   }
		return str;
	}
	
	
	
	/** Informs if a given window is possible in the Count Matrix 
	 * Given time instant and a temporal width, informs if the respective window is possible in 
	 * the count matrix. In the count matrix that time window is defined by all rows and the corresponding
	 * columns since corresponding column to time until the corresponding column to time+width. 
	 * @param time time instant where start the window
	 * @param width time width of the window
	 * @return TRUE if window is possible, or FALSE otherwise.*/ 
	public boolean windowPossible(double time,double width) {
		boolean result = false;
		
		if ((this.getIdx(time)>=0) && (this.getIdx(time+width)>0)) {
			result = true;
			
		}
		return(result);
	}

	
	 //public DoubleMatrix1D rawPattern (double a, double b) {return ((DoubleMatrix1D)null);}
	public int numRows() {
		return (this.numberOfRows);
	}

	public int numCols() {
		return (this.numberOfCols);
	}

	public double binSize() {
		return (this.binSize);
	}

	public double firstTime() {
		return (this.first);
	}

	public double lastTime() {
		return (this.last);
	}

	public void setWindowWidth(int width) {
		windowWidth = width;
	}

	public int getWindowWidth() {
		return windowWidth;
	}
 
     public int                        numPatterns(int width){return (-1);}
     public int                        numPatterns(int width, double beginTime) {return (-1);}
     
     
     public boolean  incCursor(int inc) {
       int newCursor = this.cursor + inc;
       if(isValidCursor(newCursor)) {
    	   this.cursor = newCursor;
    	   return (true);
       }
       return (false);
    	 
     }
     
     
     
     public double             getCursor() {
    	 return (this.first+(this.cursor*this.binSize));
     }
     public boolean            hasNext() {return (false);}


	
     public void setTitle (String title){
    	 this.title = title;
     }
     public String getTitle() {
		// TODO Auto-generated method stub
		return this.title;
     }
     
     // TODO uses exception
     
     public double  avgRow (int idx) {
    	 
    	 if (!this.isValidRow(idx)){
    		 return (Double.NaN);
    	 }
    	 int sum = 0;
    	 for (int i=0; i<this.numberOfCols; i++) {
    		 sum+=matrix[idx][i];
    	 }
    	 return ((double)sum/this.numberOfCols);
     }
     
     public double  avgColumn (int idx) {
    	 
    	 if (!this.isValidColumn(idx)){
    		 return (Double.NaN);
    	 }
    	 int sum = 0;
    	 for (int i=0; i<this.numberOfRows; i++) {
    		 sum+=matrix[i][idx];
    	 }
    	 return ((double)sum/this.numberOfRows);
     }
     
     private boolean isValidRow(int idx) {
    	 if( (idx>=0) && (idx<this.numberOfRows) ) {
    		 return true;
    	 }
    	 return false;
     }
     
     private boolean isValidColumn(int idx) {
    	 if( (idx>=0) && (idx<this.numberOfCols) ) {
    		 return true;
    	 }
    	 return false;
     }
     private boolean isValidCursor(int value) {
    	 if ( (value>=0) &&  ((value+this.windowWidth)<=this.numberOfCols) ){
    		 return (true);
    	 }
    	 return (false);
     }

	
	
	

}
