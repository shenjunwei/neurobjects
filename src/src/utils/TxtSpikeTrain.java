package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/**
 * \brief Models the spike train information as a time series read from a text file.
 * 
 * This implementation considers that the set of spike, a spike train, s=[t1 t2 ... tN], is placed
 * one spike time per line.
 * 
 * @author Nivaldo Vasconcelos
 * @date 18Mai2010
 * 
 */

public class TxtSpikeTrain extends SpikeTrain {
	
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
	public TxtSpikeTrain(DoubleMatrix1D setTime, String setName) {
		times = setTime;
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
	public TxtSpikeTrain(String filename) {
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
	public TxtSpikeTrain(String filename, double a, double b) {
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
	public TxtSpikeTrain(String filename, String name) {
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
	public TxtSpikeTrain(String filename, String name, double a, double b) {
		
		int numberOfSpikes = this.getNumSpkFromFile(filename,a,b);
		this.times = new DenseDoubleMatrix1D (numberOfSpikes);
		this.fillFromFile(filename,a,b);
		setInitialValues(name);
		return;
		
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
