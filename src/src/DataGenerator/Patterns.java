package DataGenerator;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import cern.colt.matrix.DoubleMatrix1D;

/** Defines a set of numeric patterns */
public class Patterns {
	
	Hashtable<String, ArrayList<Pattern>> pats = null; 
	int dimension=-1;
	String label="";
	
	/** \brief Define mode related to dimension. There are the following modes: \n
	 * - single dimension, in which all patterns have the same dimension, in this case its value is \c true;
	 * - multi dimension, in which all patterns may have not the same dimension, in this case its value is \c false;
	 * */
	boolean singleDimension=true;
	
	
	/** List of string where are stored the name of neurons used to build the Weka File */
	private ArrayList<String> 		neuronNames = null;

	/** Creates a empty instance using only basic setup */
	public Patterns () { 
		this.basicSetup();
		
	}
	
	/**
	 * Creates a instance based on the input information
	 * 
	 * This constructor is useful when is already knew a initial set of
	 * patterns. The object will be created using the \c ps patterns and
	 * considering as initial time the given \time. After the inserted the first
	 * pattern, the following patterns will use as initial time \c time + \c
	 * N*timeStep
	 * 
	 * @param ps
	 *            set of patterns to be put in the patterns;
	 * @param labels
	 *            label to be used in the initial inserted patterns;
	 * @param time
	 *            initial time to be used in the initial inserted patterns;
	 * @param timeStep
	 *            time step to be used in the initial inserted patterns. The
	 *            first inserted pattern will be as begin time the give \c time,
	 *            the second inserted pattern will be as begin time the give \c
	 *            time + timeStep, and so an.
	 * */
	public Patterns (ArrayList<DoubleMatrix1D> ps, String labels, double time, double timeStep) {
		this.basicSetup();
		this.addPatterns(ps, labels, time, timeStep);
		
	}
	
	/**
	 * Creates a empty set of patterns object using a given title.
	 * 
	 * Some times is useful associate a title a set of patterns, for example, in
	 * visualization operations. This constructor creates a empty set of
	 * patterns, using the given title
	 * 
	 * @param title
	 *            name to identify the object.
	 * 
	 * */
	public Patterns (String title) {
		this.label = title;
		this.basicSetup();
	}
	
	/**
	 * Adds the given pattern in the current set of patterns.
	 * 
	 * @param pat
	 *            given pattern to be added in the current set of patterns;
	 * */
	public void addPattern (Pattern pat) {
		//String currentLabel = ; 
		ArrayList<Pattern> list = pats.get(pat.getLabel()); 
		if (pat==null) {
			return;
			// \todo Patterns:addPattern ---> exception 
		}
		if (list==null) {
			list = new ArrayList<Pattern>();
		}
		
		if (this.singleDimension) {
			if (this.dimension < 0) {
				this.dimension = pat.pattern.size();
			}
			if (this.dimension == pat.pattern.size()) {
				list.add(pat);
				pats.put(pat.getLabel(), list);
			} else {
				// Gerar um exception de formato inválido.
			}
		}
		
				
	}
	
	public int getDimension () {
		return (this.dimension);
	}
	
	
	/** Inserts a set of raw pattern in the current set of patterns.
	 * 
	 * Given a list of raw patterns (double 1Dvectors) inserts them in current set of patterns using the additional information: labels, initial time, and time step
	 * 
	 * @param ps list of raw patterns to be inserted in the current set of patterns;
	 * @param labels label to be used in the inserted patterns;
	 * @param time initial time to be used in the inserted patterns;
	 * @param timeStep time step to be used in the inserted patterns from first time.
	 *  */
	public void addPatterns (ArrayList<DoubleMatrix1D> ps, String labels, double time, double timeStep) {
		Pattern p = null;
		
		
		if (ps==null) {
			return;
			// Would be better log this kind of event: e equal to null.
		}
		//get the Enumeration object
	    Enumeration<DoubleMatrix1D> e = Collections.enumeration(ps);
	    if (e==null) {
	    	return;
	    	// Would be better log this kind of event: e equal to null.
	    }
	    
	    while(e.hasMoreElements()) {
	    	p = new Pattern(e.nextElement(), labels, time);
	    	this.addPattern(p);
			time += timeStep;
	    }
		
	}
	
	/** Implement a basic setup operation.*/
	private void basicSetup() {
		pats = new Hashtable<String, ArrayList<Pattern>> ();				
	}
	
	/**
	 * Builds a weka header based on current set of patterns
	 * 
	 * Based on the current neuron names returns a string in which is a Weka
	 * File header, where each neuron contributes with N bins, where N is equal
	 * to (size of pattern)/(number of neurons). N should be a integer.
	 * 
	 * Please see http://www.cs.waikato.ac.nz/~ml/weka/arff.html for more
	 * details about the Attribute-Relation File Format (ARFF)
	 * */
	private String buildBinWekaHeader() {
		String result = "@relation 'binary'\n";
		String neuron="";
		int windowWidth=0;
		
		if (this.neuronNames==null) {
			// Exception !!
			return "Undefined neuron names list!!\n";
		}
		if (this.neuronNames.size()==0) {
			// Exception !!
			return "Empty neuron names list!!\n";
		}
		
		if (!this.singleDimension) {
				// Exception !!
			return "The object is not on single dimension mode !!\n";
		}
		
		windowWidth=(int) Math.floor(this.dimension/this.neuronNames.size());
		Enumeration<String> e = Collections.enumeration(this.neuronNames);
	    if (e==null) {
	    	return "";
	    	// Would be better log this kind of event: e equal to null.
	    }
	    while(e.hasMoreElements()) {
	    	neuron = e.nextElement();
	    	for (int i=0; i<windowWidth; i++) {
	    		result+="@attribute "+neuron+"_"+i+" real\n";	    		
	    	}
	    } 
	    result+="@data\n";
		return (result);
	}
	
	/**
	 * Builds a weka data content based a given set of patterns
	 * 
	 * Based a given set of patterns returns a string in which there is
	 * the data content in the Weka Attribute-Relation File Format.
	 * 
	 * Please see http://www.cs.waikato.ac.nz/~ml/weka/arff.html for more
	 * details about the Attribute-Relation File Format (ARFF)
	 * 
	 * @param label string to be used as label for each pattern in data content weka file.
	 * @param pats set of patterns to be used to build the data content weka file.
	 * @param idx set of indexes to be used from the set of patterns. Only these patterns should be used to build the data content weka file.  
	 * @return the data content in the weka Attribute-Relation File Format based on the current 
	 * */
	private String buildPatterns (String label, ArrayList<Pattern> pats, ArrayList<Integer> idx) {

		String result = "";
		Integer i=0;
		Pattern p=null;
		
		
		Enumeration<Integer> e = Collections.enumeration(idx);
	    if (e==null) {
	    	return "";
	    	// Would be better log this kind of event: e equal to null.
	    }
	    while(e.hasMoreElements()) {
        	//result+=list.get(i).toString();
	    	i = e.nextElement();
	    	p = pats.get(i);
	    	result+=p.toWeka(label)+"\n";
	    } 
		
		return result;
		
	}
	
	// MUDAR assinatura !! Essa maneira não está adequada.
	/** Builds a weka file based on */
	public String buildWekaFile(String label, ArrayList<Pattern> pats, ArrayList<Integer> idx) {
		String result="";
		result+=this.buildBinWekaHeader();
		result+=this.buildPatterns(label, pats, idx);
		return (result);
	}
	
	
	/** Given a label, returns the correspondent set of patterns stored
	 * 
	 * If there is no patterns associated with that label returns \c null.
	 * @param label label to be used to returns the patterns 
	 * @return A list of patterns that are associated to label */
	public ArrayList<Pattern> getPatterns (String label) {
		return (this.pats.get(label));
	}
	
	
	/** Given a label and an index returns the correspondent pattern stored
	 * 
	 * If there is no patterns associated with that label returns \c null.
	 * @param label label to be used to returns the patterns 
	 * @return A pattern that are associated to label in a given index */
	public Pattern getPattern (String label, int index) {
		if (index>this.pats.get(label).size()) {
			return null;
		}
		 
		return (this.pats.get(label).get(index));
	}
	
	/**
	 * Informs if the current object is single dimension.
	 * 
	 * A Pattern object is single dimension when ALL their patterns have the
	 * same dimension (number of elements)
	 * 
	 * @return \c true if the current object is single dimension or \c false otherwise.
	 */
	public boolean isSingleDimension() {
		return singleDimension;
	}
	
	/** Sets the neurons names 
	 * 
	 * Based on a given list of names sets the internal neuron names
	 * 
	 *  @param names list of neuron names */
	public void setNeuronNames(ArrayList<String> names) {
		this.neuronNames = new ArrayList<String> ();
		this.neuronNames.addAll(names); 
	}
	
	/**
	 * Define the value of single dimension flag
	 * 
	 * @param singleDimension
	 *            value to be used on sigle dimension flag. When it is defined
	 *            as \c true all patterns in set should have the same dimension.
	 */
	public void setSingleDimension(boolean singleDimension) {
		this.singleDimension = singleDimension;
	}
	
	/** Returns a string with all information of the set of patterns */
	public String toString () {
		ArrayList<Pattern> list = null;
		Enumeration labels = this.pats.keys();
		String result="";
		String currentLabel="";
		
		
		for (; labels.hasMoreElements() ;) {
			currentLabel = (String) labels.nextElement();
			list = pats.get(currentLabel );
			result+=currentLabel+"\n";
			Enumeration<Pattern> e = Collections.enumeration(list);
		    if (e==null) {
		    	return ("");
		    	// Would be better log this kind of event: e equal to null.
		    }
		    while(e.hasMoreElements()) {
	        	//result+=list.get(i).toString();
	        	result+=e.nextElement().toString();
		    }

	     }
		
		return (result);
	}
	
	private  ArrayList<Integer> getRandom (ArrayList<Integer> indexes, int N ){
		ArrayList<Integer> rndIndexes = new ArrayList<Integer>();
		ArrayList<Integer> allIndexes = new ArrayList<Integer>();
		int	pos=0;
		int currentSize = indexes.size();
		
		allIndexes.addAll(indexes);
		
		Random R = new Random ();
		// If the list is smaller than N returns a empty list
		if (currentSize<N) {
			return (rndIndexes);
		}
		
		for (int i=0; i<N; i++) {
			pos = R.nextInt(currentSize-1);
			rndIndexes.add(allIndexes.get(pos));
			allIndexes.remove(pos);
			currentSize--;
		}
		return (rndIndexes);
	}
	
	public String getLabels () {	
		return (this.pats.keys().toString());
	}
	
	public int numPatterns (String label) {
		ArrayList<Pattern> list = this.pats.get(label);
		if (list==null) {
			return (0);
		}
		return (list.size());
		
	}
		
	

}
