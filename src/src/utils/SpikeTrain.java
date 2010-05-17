package utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

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
		
		this.times = setTime.copy();
		this.name = setName;
		this.first= this.times.get(0);
		this.last= this.times.get(this.times.size()-1);
	}
	
	
	/**
	 * \brief Constructor of a Spike Train given a filename in which there is
	 * the spike train.
	 * 
	 * This constructor receive a filename (with full path) in which should be
	 * there is a spike train, one time per row. The 
	 * 
	 * @param fileName full path and file name in which are stored the spike times
	 * 
	 * */
	public SpikeTrain(String fileName) {
		String neuronFilename = fileName;
		int numberOfSpikes= 0;
		
		// Number of spike times
		try {
			BufferedReader in = new BufferedReader(new FileReader(
					neuronFilename));
	        while (in.readLine() != null){  
	        	numberOfSpikes++;
	     
	        }
	        in.close();
			
		} catch (IOException e) {
			System.out.println("SpikeTime: Problems reading the file: " + neuronFilename);
		}
		if (numberOfSpikes==0) {
			this.valid = false;
		}
		else {
			this.valid = true;
		}
		if (!this.valid) {
			return;
		}
		
		this.times = new DenseDoubleMatrix1D (numberOfSpikes);
		try { 
			BufferedReader in = new BufferedReader(new FileReader(
					neuronFilename));
			String str;
			
			int i = 0;
			while ((str = in.readLine()) != null) {
				this.times.set(i++, Double.parseDouble(str));
			}
		} catch (IOException e) { }
		this.name = this.parseName(neuronFilename);
		return;
		
		
	}
	
	/** \brief Constructor of a Spike Train given a filename in which there is
	 * the spike train and a time interval.
	 * 
	 * This constructor receive a filename (with full path) in which should be
	 * there is a spike train, one time per row and a time interval I=[a;b] and
	 * build spike that stores those spike times into I interval.
	 * 
	 * @param fileName full path and file name in which are stored the spike times
	 * @param first time in time interval;
	 * @param last time in time interval; 
	 *            
	 *  
	 * */
	public SpikeTrain(String fileName, double a, double b) {
		
		
	}
	
	public SpikeTrain(String fileName, String name) {		
		
	}
	
	public SpikeTrain(String fileName, String name, double a, double b) {		
		
	}

	public DoubleMatrix1D getTimes() {
		return times;
	}

	public void setTimes(DoubleMatrix1D times) {
		this.times = times;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public double getFirst() {
		return first;
	}

	public double getLast() {
		return last;
	}
	
	private String parseName(String name) {
		String newName = name;
		
		int dotPos = name.lastIndexOf('.');
		int pathPos = name.lastIndexOf('\\');
		
		newName = name.substring(pathPos+1, dotPos-1);		
		return (newName); 
	}
	
	
}
