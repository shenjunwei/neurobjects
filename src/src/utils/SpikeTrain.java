package utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/**
 * \brief Models the spike train information as a time series.
 * 
 * A set of spike, a spike train, s=[t1 t2 ... tN], is modeled as a 1D vector,
 * in which element, xi is equal to ti from spike train s. Therefore, the models 
 * associate a spike train to a 1D vector following:
 * \code
 * 
 * s=[t1 t2 ... tN] <-> v=(x1,x2, ...,xN), where x1=t1, x2=t2, ... , xN=tN.
 * \endcode
 * @author Nivaldo Vasconcelos
 * @date 17Mai2010
 * \todo Documentation
 */
public class SpikeTrain {
	
	/** 1D vector to store the spike times */
	private  DoubleMatrix1D times=null;
	
	/** Spike train name*/
	private  String			name="";
	
	/** value of first spike time */
	private  double			first=0;
	
	/** value of last spike time */
	private  double			last=0;
	
	/** is it valid this spike train ?*/
	private boolean valid=false;
	
	
	/**
	 * \brief Constructor of a Spike Train given a 1D vector and a name.
	 * 
	 * This constructor receive a 1D vector in which should be the spike time in a
	 * crescent order, internally is built a copy of this 1D vector. Moreover receives the name of spike train. Normally it is the name of neuron.
	 * 
	 * @param setTime 1D vector with spike times to be stored;
	 * @param setName String with name of spike train name.
	 * 
	 *
	 * */ 
	public SpikeTrain(DoubleMatrix1D setTime, String setName) {
		this.times = setTime;
		setInitialValues(setName);
	}
	
	
	/**
	 * \brief Constructor of a Spike Train given a filename in which there is
	 * the spike train.
	 * 
	 * This constructor receive a filename (with full path) in which should be
	 * there is a spike train, one time per row. The 
	 * 
	 * @param filename full path and file name in which are stored the spike times
	 * 
	 * */
	public SpikeTrain(String filename) {
		int numberOfSpikes = this.getNumSpkFromFile(filename);
		this.times = new DenseDoubleMatrix1D (numberOfSpikes);
		this.fillFromFile(filename);
		setInitialValues(filename);
		return;
	}
	
	/** \brief Constructor of a Spike Train given a filename in which there is
	 * the spike train and a time interval.
	 * 
	 * This constructor receive a filename (with full path) in which should be
	 * there is a spike train, one time per row and a time interval I=[a;b] and
	 * build spike that stores those spike times into I interval.
	 * 
	 * @param filename full path and file name in which are stored the spike times
	 * @param a first time in time interval;
	 * @param b last time in time interval; 
	 *            
	 *  
	 * */
	public SpikeTrain(String filename, double a, double b) {
		int numberOfSpikes = this.getNumSpkFromFile(filename,a,b);
		this.times = new DenseDoubleMatrix1D (numberOfSpikes);
		this.fillFromFile(filename,a,b);
		setInitialValues(filename);
		return;
	}
	
	/**
	 * \brief Constructor of a Spike Train given a filename in which there is
	 * the spike train and the name of spike train.
	 * 
	 * This constructor receive a filename (with full path) in which should be
	 * there is a spike train, one time per row.  
	 * 
	 * @param filename full path and file name in which are stored the spike times
	 * @param name name to be used by the spike train
	 * 
	 * */
	public SpikeTrain(String filename, String name) {
		int numberOfSpikes = this.getNumSpkFromFile(filename);
		this.times = new DenseDoubleMatrix1D (numberOfSpikes);
		this.fillFromFile(filename);
		setInitialValues(name);
		return;
	}
	
	/** \brief Constructor of a named Spike Train given a filename in which there is
	 * the spike train and a time interval.
	 * 
	 * This constructor receive a filename (with full path) in which should be
	 * there is a spike train, one time per row and a time interval I=[a;b] and
	 * build spike that stores those spike times into I interval.
	 * 
	 * @param filename full path and file name in which are stored the spike times;
	 * @param name name to be used by spike train;
	 * @param a first time in time interval;
	 * @param b last time in time interval; 
	 *            
	 *  
	 * */
	public SpikeTrain(String filename, String name, double a, double b) {
		
		int numberOfSpikes = this.getNumSpkFromFile(filename,a,b);
		this.times = new DenseDoubleMatrix1D (numberOfSpikes);
		this.fillFromFile(filename,a,b);
		setInitialValues(name);
		return;
		
	}

	/** \brief Returns the time series of the spike train. 
	 * 
	 * Returns a 1D vector in which is time series representing the spike train.
	 * @return a 1D vector with the times of spike train
	 * 
	 * */
	public DoubleMatrix1D getTimes() {
		return times;
	}

	/** \brief  Returns the spike train name. 
	 * 
	 * @return spike train name */
	public String getName() {
		return name;
	}

	/** \brief  Defines the spike train name.
	 * 
	 * @param name to be used by spike train.
	 * */
	public void setName(String name) {
		this.name = name;
	}
	
	/** \brief Returns the first time in the spike train
	 * 
	 *  @return the value of the first time in the spike train */
	public double getFirst() {
		return first;
	}

	/** \brief Returns the last time in the spike train
	 * 
	 *  @return the value of the last time in the spike train */
	public double getLast() {
		return last;
	}
	
	/** \brief  Informs if the spike train content is valid.
	 * 
	 * @return \c TRUE if the spike is valid \b or \n
	 *         \c FALSE otherwise */
	public boolean isValid() {
		return (valid);
	}
	
	private String parseName(String name) {
		String newName = name;
		
		int dotPos = name.lastIndexOf('.');
		int pathPos = name.lastIndexOf('/');
		
		if (dotPos <0 && pathPos<0)
			return (name); 

		newName = name.substring(pathPos+1, dotPos);
		
		return (newName); 
	}
	
	private int getNumSpkFromFile (String filename) {
		
		int numberOfSpikes= 0;
		
		// Number of spike times
		try {
			BufferedReader in = new BufferedReader(new FileReader(
					filename));
	        while (in.readLine() != null){  
	        	numberOfSpikes++;
	     
	        }
	        in.close();
			
		} catch (IOException e) {
			System.out.println("SpikeTime: Problems reading the file: " + filename);
		}
		return (numberOfSpikes);
		
	}
	
	private int getNumSpkFromFile (String filename, double a, double b) {
		
		int numberOfSpikes= 0;
		
		// Number of spike times
		try {
			BufferedReader in = new BufferedReader(new FileReader(
					filename));
			double spikeTime=0;
			String str="";
			while (((str = in.readLine()) != null) && (spikeTime < b)) {
				spikeTime = Double.parseDouble(str);
				if ((spikeTime >= a) && (spikeTime <= b)) {
					numberOfSpikes++;
				}
			}
	        in.close();
		} catch (IOException e) {
			System.out.println("SpikeTime: Problems reading the file: " + filename);
		}
		return (numberOfSpikes);
		
	}
	
	
	
	private void fillFromFile(String filename){
		
		
		try { 
			BufferedReader in = new BufferedReader(new FileReader(
					filename));
			String str;
			
			int i = 0;
			while ((str = in.readLine()) != null) {
				this.times.set(i++, Double.parseDouble(str));
			}
			in.close();
		} catch (IOException e) { }
		
	}
	
	private void fillFromFile(String filename, double a, double b){
		
		
		try { 
			BufferedReader in = new BufferedReader(new FileReader(
					filename));
			String str;
			int i = 0;
			double spikeTime=0;
			
			while (((str = in.readLine()) != null) && (spikeTime < b)) {
				spikeTime = Double.parseDouble(str);
				if ((spikeTime >= a) && (spikeTime <= b)) {
					this.times.set(i++, spikeTime);
				}
			}
			in.close();
		} catch (IOException e) { }
		
	}
	
	/**
	 * \brief Set initial values like: if the spike trains is valid, first time
	 * and last time, neuron name
	 */
	private void setInitialValues(String name) {
		int numberOfSpikes = this.times.size();
		if (numberOfSpikes==0) {
			this.valid = false;
		}
		else {
			this.valid = true;
		}
		if (!this.valid) {
			return;
		}
		this.name = this.parseName(name);
		this.first= this.times.get(0);
		this.last= this.times.get(this.times.size()-1);
	}
	
	
}
