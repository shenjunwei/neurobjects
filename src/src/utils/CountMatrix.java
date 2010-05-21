package utils;

import hep.aida.IHistogram1D;
import hep.aida.ref.Histogram1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix2D;
import java.io.*;
import java.util.*;

import errors.InvalidArgumentException;

/** @brief Create the Matrix of spike count given a interval
 * 
 * Considering that the spike times are stored in data files, with a specific structure (one spike time per row), 
 * this class build a matrix where the correspondent spike counting is stored for that given interval. 
 * 
 * The matrix is implemented using the Colt class termed DoubleMatrix2D.
 * 
 *  @see DoubleMatrix2D
 */
public class CountMatrix {
	
	/** Size of bins used to build the count. Using this bin size, in seconds, are defined the count intervals. Given 
	 * the time interval where the count is taken, I=[a,b], this interval is considered in sub-intervals I'_1 = [a,a+binSize]
	 * I'_2 = [a+binSize;a+2*binSize] ... I'_k = [(k-1)*binSize;k*binSize] ... I'_M=[(M-1)binSize; b]*/
	private double					binSize=0.0; 
	
	/**Path to dataset where the data files are stored. */
	private String					datasetPath = ""; 
	
	/** First time instant of the interval where the spikes will be counted  */
	private double					first=0.0;
	
	/** Size of the histogram which stores the spike count */
	private int 					histSize = 0; 
	
		
	/** Last time instant of the interval where the spikes will be counted  */
	private double					last=0.0; 
	
	/** The last valid position of atual window on binMatrix  */
	private int lastValidWindowPosition=-1;
	
	/** String where are stored the log msgs */
	private String 					log = "";
	
	/**  Matrix where are stored all count for the given interval. Each row stores the spike count for a neuron  */
	public DoubleMatrix2D			matrix; 
	
	/** Minimum number of spikes to consider a spike train as valid within a interval */
	private int						minSpikes = 10; 
	
	/** List of string where are stored the name of neurons used to build the Count Matrix */
	private ArrayList<String> 		neuronNames = null; 
	
	/** Number of collumns */
	private int						numberOfCols = 0;
	
	/**  Number of rows  */
	private short					numberOfRows = 0; 
	
	/* Attributes used to control inner operations */
	
	/** Stores the information about the Count Matrix validity. The Count Matrix is good to be used when this value is true */
	private boolean					valid = false;
	
	/** The cursor position of window inner binMatrix. */
	private int windowIndex=0;
	/** Window size */
	private int windowWidth=0;
	
	
	
	/** Constructor of the CountMatrix class when is given the data files path, bin size and interval 
	 * 
	 * Given a interval  I=[a;b] and a directory, where are stored the date files of neurons, this method builds a matrix
	 * in which each row stores the spike count information of a neuron. The spike count is taken in time intervals
	 * with binSize seconds of width. Only spike occurring within [a;b] are counted.  If a neuron has less than 10 spikes 
	 * within  [a;b] it is not taken to build the Count Matrix.
	 * 
	 * @param path directory where the data files are stored;
	 * @param binSize size of the bin used the build the count Matrix;
	 * @param a begin of the interval where the count will be taken;
	 * @param b end of the interval where the count will be taken;
	 * */
	public CountMatrix (String path, double binSize, double a, double b) {
		
		// Validates the intervals and calculates the histogram size
		if (!this.setupInterval(binSize, a, b)) {
			return;
		}
		
		this.datasetPath = path;
		
		// get number and names of neurons
		this.setupNeurons();
		this.numberOfRows = (short) this.numberOfNeurons();
		this.numberOfCols = this.histSize;
		this.binSize = binSize;
			
		IHistogram1D h1 = null; 
		if (!this.createMatrix2D()) {
			this.valid = false;
			this.log = this.log + "Problems creating Count Matrix !!\n";
		}
		h1 = new Histogram1D("H",this.histSize,this.first,this.last);
		for (int i=0; i<this.numberOfNeurons(); i++) {
			h1.reset();
			if (this.insertNeuronSpikes(h1,i)) {
				if (!this.fillMatrixRow2D(h1, i)) {
					return;
				}
				
			}
			if (!this.isValid()){
				return;
			}
		}
		
			
		// Release the Count Matrix to be used. 
		this.valid = true; 
	}
	
	/** Checks the spike trains for each neuron within the given count interval
	 * 
	 *  Remove neurons, from internal list, which have no minimum spikes into count interval 
	 *  @see CountMatrix#validNeuron*/  
	private void checkNeurons() {
		
		int numberOfNeurons = this.numberOfNeurons();
		
		for (int i=0; i<numberOfNeurons; i++) {
			if (!validNeuron(i)){
				this.removeNeuronName(i);
				
			}
		}
		if (numberOfNeurons<=0) {
			this.valid = false;
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
		this.matrix = new DenseDoubleMatrix2D (this.numberOfRows,this.numberOfCols);
		
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
		
		if (h1.entries()<this.numberOfCols) {
			this.valid = false;
			this.log = this.log + "fillMatrixRow2D: Histogram not smaller than number of columns\n";
			return (false);
		}
		for (int column = 0; column < this.numberOfCols; column++) {
			this.matrix.setQuick(row, column, h1.binEntries(column));
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
	
	/** Given the column position and window width, returns an array of spikes of Count Matrix.
	  *  
	 * @param firstCol : first column from which the pattern will be formed.
	 * @param windowWidth : window width to form the pattern.
	 * @return pattern: a int vector with the correspondent pattern derive from the window.
	 * 
	 * todo: check it !!
	 */
	public int[] getPattern(int firstCol, int windowWidth){
		
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
		
		int pattern[] = new int[this.numberOfRows*windowWidth];
		int colLimit = firstCol + windowWidth;
		 
		
		for (int row=0, i=0; row<this.numberOfRows; row++) {
			for (int col=firstCol; col<colLimit; col++,i++) {		
				pattern[i]= (int) this.matrix.getQuick(row, col);
			}
		} 
		return (pattern);
	}
	
	/* ------------------------------------------------------------------------------------------ */
	/** Inserts spike trains for a given neuron in the Count Matrix.
	 * 
	 *  Given a histogram and a index this method:
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
	private boolean insertNeuronSpikes(IHistogram1D h1, int i) {
		String neuronFilename = this.datasetPath+"/"+this.neuronNames.get(i) +".txt";
		try { 
			BufferedReader in = new BufferedReader(new FileReader(
					neuronFilename));
			String str;
			double spikeTime = 0;
			int numOfSpikes = -1;
			while (((str = in.readLine()) != null) && (spikeTime < this.last)) {
				if ((spikeTime >= this.first) && (spikeTime <= this.last)) {
					spikeTime = Double.parseDouble(str);
					numOfSpikes++;
					h1.fill(spikeTime);
				}
		}
		if (numOfSpikes==0) {
			this.valid = false;
			this.log = this.log + "Problems reading spikes from " +  neuronFilename+"\n";
		
		}
		else {
			this.valid = true;
		}
		in.close(); 
		
		} catch (IOException e) { } 
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
	
	/** Given a index, remove a name of neuron from the internal list of neuron names. 
	 * 
	 * If index is not valid the operation is not done, and is added a message into log string 
	 * 
	 * @param i index of neuron name to be removed.
	 * */
	private boolean removeNeuronName(int i) {
		boolean result = false;
		if ( (i<=this.neuronNames.size()) && (i>=0) ){
			this.neuronNames.remove(i);
			result = true;
		}
		else {
			this.log = this.log + "Neuron name not removed: " +i;
		}
		return (result);
		
	}
	
	/* ------------------------------------------------------------------------------------------ */
	/** Setups the internal time interval.
	 * 
	 * 
	 *  @param binSize size of bin used to build the count matrix (seconds).  
	 *  @param a begin of time interval to build the count matrix (seconds).
	 *  @param b end of time interval to build the count matrix (seconds).
	 * */
	private boolean setupInterval(double binSize, double a, double b) {
		
		if ((a<0) || (b<0)) {
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
		
		return true;
	}
	/** Setup the neurons informations
	 *
	 * This method not read the spikes from neurons, only general information into them. 
	 * The current neurons information are:
	 * - names 
	 * - number of neurons */
	private void setupNeurons() {
		
		// Gets the list of files in the dataset path
		File dir = new File(this.datasetPath);
		String name[] = dir.list();
		this.neuronNames = new ArrayList<String>();
		int numberOfNeurons = name.length;
	
		for (int i=0; i<numberOfNeurons; i++) {
			name[i]=name[i].replace(".txt", "");
			this.neuronNames.add(name[i]);
		}
		
		Collections.sort(this.neuronNames);
		checkNeurons();
		
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
	        	  str=str+this.matrix.getQuick(row, column)+"\t";
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
	        	  str=str+this.matrix.getQuick(row, column)+"\t";
	          }
	          str=str+"\n";
	   }
		return str;
	}
	
	 /* ------------------------------------------------------------------------------------------ */
	/** Checks the spike trains, for a given neuron, within the given count interval
	 * 
	 *  Given the index of this neuron in the internal list. If the index is valid the correspondig
	 *  data file is read and is cheked if the number of spikes within the interval I=[a;b] 
	 *  is not less than the minimum the neuron is considered valid, otherwise is considered
	 *  invalid.  
	 *  Before all, is checked if the data file exist. If data file does not exist it is logged a msg
	 *  in the internal log and returned FALSE.  
	 *  If the index is not valid in internal list the internal flag valid is defined as false and a 
	 *  log msg is added to internal log and returned FALSE.
	 *  
	 *  @param i index of neuron in internal list 
	 *  @return TRUE if neuron is valid, or FALSE otherwise.*/
	private boolean validNeuron(int i) {
		
		boolean valid = false;
		int numOfSpikes = -1;
		
		if ( (i>this.neuronNames.size()) || (i<0) ){
			
			this.log = this.log + "Neuron index is not valid : " +i;
			return (valid);
		}
		
		String neuronFilename = this.datasetPath+"/"+this.neuronNames.get(i) +".txt";
		// File exist ?
		boolean exists = (new File(neuronFilename )).exists();
		if (!exists)  {
			this.log = this.log + "There is no corresponding data file for neuron: " +neuronFilename;
			return (valid);
		}
		
		try { 
			BufferedReader in = new BufferedReader(new FileReader(
					neuronFilename));
			String str;
			double spikeTime = 0;
			while (((str = in.readLine()) != null) && (spikeTime < this.last) && numOfSpikes<this.minSpikes) {
				spikeTime = Double.parseDouble(str);
				if ((spikeTime >= this.first) && (spikeTime <= this.last)) {
					numOfSpikes++;
				}
		}
		in.close(); 
		} catch (IOException e) { } 
		if (numOfSpikes<this.minSpikes) {
			valid = false;
		}
		return (valid);
		
	
	}
	/* ------------------------------------------------------------------------------------------ */
	
	/* ------------------------------------------------------------------------------------------ */
	/** View part of the Count Matrix 
	 * 
	 * Return a piece of the Count Matrix. The piece is defined by position (row,column) and the
	 * width. Therefore, is returned the piece that starts in (row,column) and ends in (row+width,column+width). 
	 * It is implemented using the method DoubleMatrix2D#viewPart. 
	 * 
	 * @param row row in the Count Matrix where is defined the part
	 * @param column column in the Count Matrix where is defined the part 
	 * @param width width the Count Matrix where is defined the part
	 * @return the piece of count matrix  
	 * 
	 * @see DoubleMatrix2D#viewPart */
	public DoubleMatrix2D viewPart(int row, int column, int width) {
		
		return (this.matrix.viewPart(row,column,this.numberOfRows,width));
	}
	/* ------------------------------------------------------------------------------------------ */
	
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
	
	
	

}
