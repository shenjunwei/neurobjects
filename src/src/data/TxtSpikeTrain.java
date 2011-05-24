package data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import errors.InvertedParameterException;


/**
 * \page TxtSpikeTrainTests Tests on TxtSpikeTrain
 *
 * 	The following tests were performed (in tests.utils_TxtSpikeTrainTests): \n
 *   1- Testing the constructor in normal situation: new TxtSpikeTrain(filePath);  \n
 *   2- Testing the constructor in normal situation: new TxtSpikeTrain(filePath, "neuron_S1_02a"); \n
 *   3- Testing the constructor in normal situation: new TxtSpikeTrain(filePath, "neuron_S1_02a", 0, 100); \n
 *   4- Testing the constructor in normal situation with a negative firsTime parameter: new TxtSpikeTrain(filePath, "neuron_S1_02a", -10, 100.00000000000000000009); \n
 *   5- Testing in an erroneous situation with firstTime > lastTime: new TxtSpikeTrain(filePath, "neuron_S1_02a", 100.00000000000000000009, 0); \n
 *   6- Testing in normal situation with equals parameters 'firstTime' and 'LastTime': new TxtSpikeTrain(filePath, "neuron_S1_02a", 0, 0); \n
 *   7- Testing in an abnormal situation with a sourceFile that don't is a neuronFile but an Gif binary image file: new TxtSpikeTrain("/tmp/talesmileto01.gif", "neuron_S1_02a", 0, 500); \n
 *   8- Testing in an abnormal situation with a sourceFile that don't exists: new TxtSpikeTrain("/tmp/huiahsuihsi", "neuron_S1_02a", 0, 200); \n
 *   9- Testing in an abnormal situation with a source file that comes from /dev/random: new TxtSpikeTrain("/dev/random", "neuron_S1_02a", 0, 100); \n
 *   10- Testing in an abnormal situation which the 'firstime' and 'lastTime' parameter is much larger than the existing in source file: new TxtSpikeTrain(filePath, "neuron_S1_02a", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);  \n
 *    \n
 *   In all performed tests were made the follow commands: \n
 *    TxtSpikeTrain spkTrain_1 = new TxtSpikeTrain(filePath); \n
 *	  DoubleMatrix1D spikes_1 = spkTrain_1.getTimes(); \n
 *	  System.out.println("spikes size: "+spkTrain_1.getNumberOfSpikes()); \n \n
 */


/**
 * \brief Models the spike train information as a time series read from a text file.
 * 
 * This implementation considers that the set of spike, a spike train, s=[t1 t2 ... tN], is placed
 * one spike time per line.
 * 
 * @author Nivaldo Vasconcelos
 * @date 18Mai2010
 *
 * \TODO Verify if the spike file have a correct format (one double number per line), if not: throws an specific exception. 
 * 
 */

public class TxtSpikeTrain extends SpikeTrain {
	
	int numberOfSpikes=0;
	
	/**
	 * \brief Constructor of a Spike Train given a 1D vector and a name.
	 * 
	 * This constructor receive a 1D vector in which should be the spike time in a
	 * crescent order, internally a copy of this 1D vector is built. Moreover receives the name of spike train. Normally it is the name of neuron.
	 * 
	 * @param setTime 1D vector with spike times to be stored;
	 * @param setName String with name of spike train name.
	 * 
	 *
	 * */ 
	public TxtSpikeTrain(double[] setTime, String setName) {
		times = setTime;
		setInitialValues(setName);
		this.numberOfSpikes = setTime.length;
	}
	
	
	/**
	 * \brief Constructor of a Spike Train given a filename in which there is
	 * the spike train.
	 * 
	 * This constructor receives a filename (with full path) in which there is a spike
	 * train, one time per row. The 
	 * 
	 * @param filename full path and file name in which are stored the spike times
	 * 
	 * */
	public TxtSpikeTrain(String filename) {
		this.numberOfSpikes = this.getNumSpkFromFile(filename);
		this.times = new double[numberOfSpikes];
		this.fillFromFile(filename);
		setInitialValues(filename);
		return;
	}
	
	/** \brief Constructor of a Spike Train given a filename in which there is
	 * the spike train and a time interval.
	 * 
	 * This constructor receive a filename (with full path) in which there is a spike
	 * train, one time per row and a time interval I=[a;b] and
	 * build spike that stores those spike times into I interval.
	 * 
	 * @param filename full path and file name in which are stored the spike times
	 * @param a first time in time interval;
	 * @param b last time in time interval; 
	 * @throws InvertedParameterException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 *            
	 *  
	 * */
	public TxtSpikeTrain(String filename, double a, double b) throws InvertedParameterException, IOException, FileNotFoundException  {
		
		if (a > b){
			throw new InvertedParameterException("First timestamp 'a' must be minor or equal than last timestamp 'b'");
		}
		
		this.numberOfSpikes = this.getNumSpkFromFile(filename,a,b);
		this.times = new double[numberOfSpikes];
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
		this.numberOfSpikes = this.getNumSpkFromFile(filename);
		this.times = new double[numberOfSpikes];
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
	 * @throws InvertedParameterException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 *            
	 *  
	 * */
	public TxtSpikeTrain(String filename, String name, double a, double b) throws InvertedParameterException, FileNotFoundException, IOException  {
		
		if (a > b){
			throw new InvertedParameterException("First timestamp 'a' must be minor or equal than last timestamp 'b'");
		}

		
		this.numberOfSpikes = this.getNumSpkFromFile(filename,a,b);
		this.times = new double[numberOfSpikes];
		this.fillFromFile(filename,a,b);
		setInitialValues(name);
		return;
		
	}
	
	public String toString () {
	return (this.name + "\n"+this.times.toString()+"\n");	
	}
	private String parseName(String name) {
		String newName = name;
		
		int dotPos = name.lastIndexOf('.');
		int pathPos = name.lastIndexOf('/');
		
		if (dotPos <0 && pathPos<0)
			return (name); 

		newName = name.substring(pathPos+1, dotPos);
		
		return (newName.toLowerCase()); 
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
				times[i++] = Double.parseDouble(str);
			}
			in.close();
		} catch (IOException e) { }
	}
	
	private void fillFromFile(String filename, double a, double b) throws  IOException, FileNotFoundException {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String str;
			int i = 0;
			double spikeTime=0;
			
			while (((str = in.readLine()) != null) && (spikeTime < b)) {
				spikeTime = Double.parseDouble(str);
				if ((spikeTime >= a) && (spikeTime <= b)) {
					times[i++] = spikeTime;
				}
			}
			
			//Some times there's no spikes in given interval
			if (i == 0) {
				this.valid = false;
				System.out.println("TxtSpikeTrain:: WARNING:  "+filename+" has no spikes in given interval [ "+a+" , "+b+" ]");
			}
			in.close();
	}
	
	/**
	 * \brief Set initial values like: if the spike trains is valid, first time
	 * and last time, neuron name
	 */
	private void setInitialValues(String name) {
		int numberOfSpikes = times.length;
		if (numberOfSpikes==0) {
			this.valid = false;
		}
		else {
			this.valid = true;
			this.numberOfSpikes = numberOfSpikes;
		}
		if (!this.valid) {
			System.err.println("WARNING: IOException has occured causing an invalid spike train. Maybe there's no spikes in datasource." + numberOfSpikes + "(" + name + ")");			
			return;
		}
		this.name = this.parseName(name);
		this.first= times[0];
		this.last= times[times.length-1];
	}
	
	public int getNumberOfSpikes() {
		return (numberOfSpikes);
	}



}
