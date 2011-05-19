package utils;

import java.util.Arrays;


/**
 * This class create the histogram of spikeTimes
 *
 */
public class Histogram {
	
	
	private double 	first = 0; /**< First value of the interval in which the histogram is calculated*/
	private double 	last = 0;
	private int 	histSize = 0;
	private double  begin=0;
	private double  end=0;
	private double 	binSize=0;
	private int 	histogram[] = null;
	
	
	/** 
	 * This constructor sets several properties to start make the histogram.
	 * @param a : Begin Time, first spikeTime to search on interval.
	 * @param b : End Time, last spikeTime to search on interval.
	 * @param binSize : width of bin.
	 * @throws IllegalArgumentException 
	 */
	public Histogram (double a, double b, double binSize) throws IllegalArgumentException {
		this.begin = a;
		this.end = b;
		this.binSize = binSize;
		this.first = (Math.floor(a/binSize)*binSize); //defines the biggest bin limit smaller than a;
		this.last = (Math.ceil(b/binSize)*binSize);   //defines the smallest bin limit bigger than b;
		double value = (end-begin)/binSize;
		if (Math.ceil(value)!=value) {
			 throw new IllegalArgumentException("Invalid size of bin. It value should divide the interval width");
		}
		this.histSize = (int)Math.ceil((end-begin)/binSize)+1;
		this.histogram = new int[histSize];
		this.resetCounts();
	}
	
	/**
	 * \brief Set to zero all entries of the histogram buffer.
	 * 
	 * 
	 * This method is very useful when the same instance is used as histogram
	 * for more than one counting process. For example, the spike counting
	 * process of a neuronal population in a same time interval. After each
	 * counting process, for each neuron, this method can be called to allow the
	 * reuse of the same instance of the histogram to the other counts.
	 * 
	 * The other parameters of the histograms (number of bins of the histogram,
	 * starting time, ending time, etc.) are kept unchanged.
	 * 
	 * 
	 */
	public void resetCounts () {
		// setup the histogram vector with zeros.
		for (int i=0; i<histSize; i++) {
			this.histogram[i]=0;
		}
		
	}
	
	/** Returns the number of entries in a given bin 
	 * 
	 * @param index bin in which should be informed the counting
	 * @return the number of entries in the given bin  
	 * 
	 * */
	public int binEntries (int index) {
		if ( (index>=0) && (index<=this.histogram.length-1) ) {
			return (this.histogram[index]);
		}
		return (-1);
	}
	
	/** Fills the histogram with a given sample 
	 * 
	 * @param sample value to be counted into histogram 
	 * */
	public void fill (double sample) {
		if ((sample>=this.begin) && (sample<=this.end) ) {
			int index = (int) ((sample-this.begin)/this.binSize);
			this.histogram[index]++;
		}		
	}
	
	/**
	 * Calculates the histogram, considering that the spikes are sorted e stored
	 * in a double array.
	 * 
	 * @param spike
	 *            buffer with all spikeTimes to consider on histogram.
	 */
	public void calc(double[] sample) {

		int index = 0;
		int numberOfSpikes = sample.length;

		this.resetCounts();
		// Calculates the histogram
		//@todo Optimize the point where the counting should be started.  
		for (int i=0; i<numberOfSpikes; i++) {
			if ((sample[i]>=this.begin) && (sample[i]<=this.end) ) {
				index = (int) ((sample[i]-this.first)/this.binSize);
				this.histogram[index]++;
			}
		}	
	}
	
	public String toString () {
		
		return ("1x"+this.histogram.length+":"+Arrays.toString(this.histogram));
		
	}
	/** Shows the histogram */
	public void show () {
		for (int i=0; i<this.histSize; i++) {
			
				System.out.println(i+":\t"+ this.histogram[i]);
		}		
	}
	
	/** Returns all entries 
	 * 
	 * H[0] = number of entries in the bin 0
	 * H[1] = number of entries in the bin 1
	 * .
	 * .
	 * .
	 * H[N-1] = number of entries in the bin N-1.
	 * 
	 * Where N is the size of the histogram.
	 * 
	 * @return a int vector with all entries in the histogram 
	 */
	public int[] getCounts() {
		
		return (this.histogram.clone());
	}

	/** Returns the value used as beginning of the histogram 
	 * 
	 * @return the value used as beginning of the histogram.*/
	public double getBegin() {
		return begin;
	}

	/** Tells the size of bin used to build the histogram 
	 * 
	 * @return the value of the used size of bin used into the histogram.*/
	public double getBinSize() {
		return binSize;
	}

	/** Returns the value used as end of the histogram 
	 * 
	 * @return the value used as end of the histogram.*/
	public double getEnd() {
		return end;
	}

	/** \brief First value of the interval in which the histogram is calculated 
	 * 
	 * @return the biggest bin limit smaller than beginning of the histogram */
	public double getFirst() {
		return first;
	}

	/** Tells the size of the histogram
	 * 
	 * Tells the number of bins uses into the histogram.
	 * @return the size of the histogram
	 */
	public int getHistSize() {
		return histSize;
	}

	
	/** \brief Last value of the interval in which the histogram is calculated 
	 * 
	 * @return the the smallest bin limit bigger than end of the histogram; */
	public double getLast() {
		return last;
	}
	
	public void reset() {
		this.resetCounts();
	}

	
}