package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Enumeration;
import java.util.Hashtable;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * \brief Handles a animal behavior file
 * 
 * In our model the animal behavior is captured as a set of: label and a time
 * interval. \n The label must be a string and time interval must be formed
 * using two numbers . This class implements the interface BehavHandlerI when
 * the animal behavior is in a file. Please, see the \ref behaveDataSpec about 
 * the specification of this kind of file.
 * 
 * */
public class BehavHandlerFile implements BehavHandlerI {

	String filePath;
	Hashtable<String,  ArrayList<double[]>> intervals = null;
	boolean valid = false;
	
	
	/** \brief Creates a BehavHandlerFile object given a filename 
	 * 
	 * The file should be in a Behavior Data Format
	 * 
	 * @param filename name of file in which there is the animal behavior description
	 * @see \ref behaveDataSpec.
	 */
	public BehavHandlerFile(String filename) {
		// TODO Auto-generated constructor stub
		this.intervals = new Hashtable<String, ArrayList<double[]>> ();
		this.filePath = filename;
		
		
		if (!(new File(filename)).exists()) {
		    // gerar exception 
			System.out.println("File not found: "+filename);
			return;
		} 
		try { 
			BufferedReader in = new BufferedReader(new FileReader(
					filename));
			String str;
			String[] value=null;
			double interval[]={0,0};
			String label="";
			
			while (((str = in.readLine()) != null) ) {
				value = str.split(",");
				interval[0]=Double.parseDouble(value[0].trim());
				interval[1]=Double.parseDouble(value[1].trim());
				label=value[2].trim();
				this.addInterval(interval.clone(), label);
			}
			in.close();
		} catch (IOException e) {
			System.err.println ("BehavHandlerFile\n ERROR: Problems reading from file : " + filename);
			
		}
		this.valid = true;

	}
	
	/** \brief Tells whether the object is valid 
	 * 
	 * @return \c true if the object is valid or \c false otherwise. */
	public boolean isValid() {
		return valid;
	}
	
	/** \brief Adds a time interval with a given label to animal behavior description
	 * 
	 * @param interval a 2 position double array where which must contain the time interval 	 
	 * @param label tag to be used in that time interval 
	 */
	public void addInterval (double[] interval, String label) {
		//String currentLabel = ; 
		ArrayList<double[]> list = this.intervals.get(label);
		if (interval[0]>=interval[1]) {
			java.util.Arrays.sort(interval);
		}
		if (list==null) {
			list = new ArrayList<double[]>();
		}
		list.add (interval);
		this.intervals.put(label, list);
				
	}
	
	private void sortIntervals (ArrayList<double[]> list) {
		int numIntervals = list.size();
		double interval[] = {0,0};
		DoubleMatrix2D M = new DenseDoubleMatrix2D (numIntervals,2);
		for (int i=0; i<numIntervals; i++) {
			interval = list.get(i);
			M.setQuick(i, 0,interval[0]);
			M.setQuick(i, 1,interval[1]);
		}
		M = M.viewSorted(0);
		list.clear();
		for (int i=0; i<numIntervals; i++) {
			interval[0] = M.getQuick(i, 0);
			interval[1] = M.getQuick(i, 1);
			list.add(interval.clone());
		}	
	}
	
	/** \brief Sorts the time intervals list 
	 * 
	 * Based on the beginning of each interval, sorts the list of time intervals */
	public void sort() {
		ArrayList<double[]> list = null;
		String currentLabel="";
		Enumeration<String> labels = this.intervals.keys();
		if (labels==null) {
			return;
		}
		for (; labels.hasMoreElements() ;) {
			currentLabel = labels.nextElement();
			list = this.intervals.get(currentLabel);
			this.sortIntervals(list);
		}
	}
 
	/** \brief Returns view of animal behavior description. 
	 * 
	 * @return a String with animal behavior description.*/
	public String toString () {
		ArrayList<double[]> list = null;
		Enumeration labels = this.intervals.keys();
		String result = "File path: "+this.filePath+"\n";
		String currentLabel="";
		double interval[]={0,0};
		
		
		for (; labels.hasMoreElements() ;) {
			currentLabel = (String) labels.nextElement();
			list = this.intervals.get(currentLabel);
			result+="Label: "+currentLabel+"\n";
			Enumeration<double[]> e = Collections.enumeration(list);
		    if (e==null) {
		    	return ("");
		    	// Would be better log this kind of event: e equal to null.
		    }
		    while(e.hasMoreElements()) {
		    	interval = e.nextElement();
		    	result+="\t"+Arrays.toString(interval)+"\n";
		    }

	     }
		
		return (result);
	}
	
	/**
	 * \brief Returns a big interval in which the animal execute a set of given
	 * behavior
	 * 
	 * Given a set of labels this method returns a interval I in which all
	 * intervals associated with each given label is contained. \n For example,
	 * consider that in animal behavior data there is the following information:
	 * \code 1010,1014,label01 
	 * 1015,1020,label03 
	 * 900,902,label04 
	 * 900,915,label01
	 * \endcode
	 * 
	 * If the given list of labels is: {label01,label03}; should be returned the
	 * following interval: \code [900;1020] \endcode
	 * 
	 * @param labels
	 *            list of target labels
	 * @return a 2D vector with the big interval [a,b].
	 * @author Nivaldo Vasconcelos
	 * */
	public double[] getBigInterval(String labels) {
		double minBegin=Double.MAX_VALUE;
		double maxEnd=Double.MIN_VALUE;
		
		ArrayList<double[]> list = null;
		Enumeration<String> intLabels = this.intervals.keys();
		double interval[]={0,0};
		String currentLabel="";
		 
		
		// Setting a temporary list of given labels
		String[] tmpLabels = labels.split(",");
		ArrayList<String> givenLabels = new ArrayList<String> ();
		for (int i=0; i<tmpLabels.length ; i++) {
			givenLabels.add (tmpLabels[i].trim());
		}
		int checkedLabels=0;
		int numGivenLabels = givenLabels.size();
		// Enumeration to 
		for (; intLabels.hasMoreElements() && checkedLabels<numGivenLabels;) {
			currentLabel = intLabels.nextElement();
			if (givenLabels.contains(currentLabel)) {
				checkedLabels++;
				list = this.intervals.get(currentLabel);
				interval = list.get(0);
				if (interval[0]<minBegin) {
					minBegin = interval[0];
				}
				interval = list.get(list.size()-1).clone();
				if (interval[1]>maxEnd) {
					maxEnd = interval[1];
				}
			}
		}
		interval[0] = minBegin;
		interval[1] = maxEnd;
		
		return (interval);
		
	}

	/**
	 * \brief Returns a list of intervals tagged with a give label.
	 * 
	 * This method allow knows, from the animal behavior data, the list of time
	 * intervals associated with a give label. Ex: If one needs know the set of
	 * time intervals in which the time was labeled with 'north' the following
	 * call will provide:
	 * 
	 * \code double[] timeList = BH.getIntervals("north"); \endcode
	 * 
	 * @param label
	 *            label target.
	 * @return Returns a list of intervals tagged with a give label as list of
	 *         1D vector, or a \c null if the label was not found in animal
	 *         behavior description.
	 * @author Nivaldo Vasconcelos
	 */
	public ArrayList<double []> getIntervals(String label) {
		// TODO Auto-generated method stub
		
		return this.intervals.get(label);
	}
	

}
