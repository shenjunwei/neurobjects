package utils;

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

	public Patterns () { 
		this.basicSetup();
		
	}

	public Patterns (ArrayList<DoubleMatrix1D> ps, String labels, double time, double timeStep) {
		this.basicSetup();
		this.addPatterns(ps, labels, time, timeStep);
		
	}
	
	public Patterns (String title) {
		this.label = title;
		this.basicSetup();
	}
	
	public void addPattern (Pattern pat) {
		//String currentLabel = ; 
		ArrayList<Pattern> list = pats.get(pat.getLabel()); 
		
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
	
	private void basicSetup() {
		pats = new Hashtable<String, ArrayList<Pattern>> ();				
	}
	
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
	
	public String buildWekaFile(String label, ArrayList<Pattern> pats, ArrayList<Integer> idx) {
		String result="";
		result+=this.buildBinWekaHeader();
		result+=this.buildPatterns(label, pats, idx);
		return (result);
	}
	
	public ArrayList<Pattern> getPatterns (String label) {
		return (this.pats.get(label));
	}
	
	public boolean isSingleDimension() {
		return singleDimension;
	}
	
	public void setNeuronNames(ArrayList<String> names) {
		this.neuronNames = new ArrayList<String> ();
		this.neuronNames.addAll(names); 
	}
	
	public void setSingleDimension(boolean singleDimension) {
		this.singleDimension = singleDimension;
	}
	
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

}
