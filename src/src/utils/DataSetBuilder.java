package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import cern.colt.matrix.DoubleMatrix1D;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class DataSetBuilder {
	
	SpkHandlerI spikes = null;
	RateMatrixI matrix = null;
	BehavHandlerI behave = null;
	Patterns 	  patterns = null;
	AnimalSetup   setup = null;
	
	String 			currentFilter = "";
	
	public DataSetBuilder (AnimalSetup s) {
		this.setup = s;
		
	}
	
	public Instances getInstances (String filter, String positiveLabel) {
		Instances data = null;
		
		 
		if (!this.validBuildParams(filter, positiveLabel)) {
			//TODO exception  
			return data;
		}
		if (!this.dataIsReady(filter, positiveLabel)) {
			this.buildData(filter);
		}
		
		data = this.buildInstances(filter, positiveLabel);
		
		return (data);
		
		
	}
	
	
	private boolean validBuildParams (String filter, String positiveLabel) {
		
		// Validate parameters
		if (!this.setup.getFilters().contains(filter)) {
			
			System.out.println ("DataSetBuilder:getInstances unknown filter");
			return (false);
		}
		
		if (!this.setup.getLabels().contains(positiveLabel)) {
			
			System.out.println ("DataSetBuilder:getInstances unknown label");
			return (false);
		}
		return (true);
		
	}
	
	private Instances buildInstances  (String filter, String positiveLabel) {
		Instances data = null;
			
		
		FastVector atts  = buildAtts();
		int numAllPositive = this.setup.totalSamples;
		int numAllNegative = (int) Math.floor(numAllPositive*this.setup.getBeta());
		
								
		ArrayList<String> labels = this.setup.getLabels();
		
		int numOther = (int) Math.floor(numAllNegative/(labels.size()-1));
		if (!validNumOthers(positiveLabel, numOther)) {
			System.out.println ("Invalid number of other labels for positive label equal to :" +positiveLabel);
			return (null);
		}

		
		
		return (data);
	}
	
	private Hashtable<String, Integer> buildPatsIdx (ArrayList<String> labels ) {
		if (labels==null) {
			System.out.println ("There is no labeled samples !! ");
			return null;
		}
		Hashtable<String, Integer> patIdx = new Hashtable<String, Integer>  ();
		
		Enumeration <String> e = Collections.enumeration(labels);
		if (e==null) {
			System.out.println ("There is no labeled samples !! ");
			return (null);
		}
		String label = "";
		int numOfPatterns =0;
		int i=0;
		while(e.hasMoreElements()) {
			numOfPatterns = this.patterns.getPatterns(label).size();
			label = e.nextElement();
			for (i=0; i<numOfPatterns; i++) {
				patIdx.put(label,i);
			}
			
	    }
		return (patIdx);
	}
	
	private boolean validNumOthers(String positiveLabel, int numOther) {
		ArrayList<String> labels = new ArrayList<String> ();
		
		Enumeration <String> e = Collections.enumeration(labels);
		if (e==null) {
			System.out.println ("There is no labeled samples !! ");
			return (false);
		}
		String label = e.nextElement();
		while(e.hasMoreElements()) {
			if ( (label!=positiveLabel) && (this.patterns.getPatterns(label).size()<(3*numOther)) ) {
				return (false);
			}
	    }
		return (true);
	}
	
	private boolean dataIsReady (String filter, String positiveLabel) {
		
		if (setup==null) {
			System.out.println ("There is setup definition");
			return (false);
		}
		if (patterns==null) {
			System.out.println ("There is patterns definition");
			return (false);
		}
		if (patterns==null) {
			System.out.println ("There is spikes definition");
			return (false);
		}
		if ((patterns.getDimension()<=0) || (!patterns.isSingleDimension()) ) {
			System.out.println ("There is no correct dimension definition");
			return (false);
		}
		if (spikes.getNeuronNames().size()==0) {
			System.out.println ("The neuron names aren't defined ");
			return (false);
		}
		
		// Is there enough samples with this positive label ?
		if (patterns.getPatterns(positiveLabel).size()<setup.totalSamples) {
			System.out.println ("There is no enough samples with this positive label: " +positiveLabel);
			return (false);
		}
		
		// Is there enough samples with this negative label ?
		ArrayList<String> others = (ArrayList<String>)setup.getLabels().clone();
		others.remove(positiveLabel);
		Enumeration<String> e = Collections.enumeration(others);
		if (e==null) {
			System.out.println ("There is no negative samples with this positive label: " +positiveLabel);
			return (false);
		}
		int totalNegativeSamples = 0;
		while(e.hasMoreElements()) {
			totalNegativeSamples += patterns.getPatterns(e.nextElement()).size();
	    }
		if (totalNegativeSamples<(setup.getBeta()*setup.totalSamples)) {
			System.out.println ("There is no enough negative samples with this positive label: " +positiveLabel);
			return (false);
		}
		
		if (currentFilter==filter) {
			return (true);
		}
		return (false);
		
	}
	
	private void buildData(String filter) {
		this.behave = new BehavHandlerFile(setup.getPathToBehavior());
		
		if (this.behave==null) {
			System.out.println ("Was not possible read behavior from "+setup.getPathToBehavior());
		}
		double intervals[] = this.behave.getBigInterval(this.setup.getLabels().toString());
		this.spikes = new TxtSpkHandler(this.setup.getPathToSpikes(), filter,intervals[0] , intervals[1]);
		this.matrix = new CountMatrix (spikes,this.setup.getBinSize());
		if (this.matrix==null) {
			System.out.println ("Was not possible create the count matrix ");
		}
		this.patterns = new Patterns ();
		this.patterns.setNeuronNames(spikes.getNeuronNames());
		this.patterns.setSingleDimension(true);
		if (!this.fillPatterns()) {
			//TODO
			System.out.println ("Was not possible create set of patterns");
			return ;
		}
		
	}
	
	private boolean fillPatterns() {
		
		ArrayList<String> labels = setup.getLabels();
		
		Enumeration<String> e = Collections.enumeration(labels);
		if (e==null) {
			System.out.println ("There is no labels defined ");
			return (false);
		}
		ArrayList<double[]> intervals = null;
		String label="";
		while(e.hasMoreElements()) {
			label = e.nextElement();
			intervals = this.behave.getIntervals(label);
			if (!this.fillPatterns(intervals, label)) {
				//TODO exception
				return (false);
			}
	    }
		
		return (true);
	}
	
	/** Fills the set of patterns with patterns into a given set of intervals and with a label.
	 * 
	 * Given a set of intervals, get from the matrix a set of patterns into that give set of intervals. All these patterns 
	 * will be labeled with the given label.
	 * 
	 *  @param intervals list of intervals in which should be got patterns from RateMatrix;
	 *  @param label label to be used for all patterns inserted;
	 *  
	 *  @return \c true if the operation was sucessful, \b or \c false otherwise;
	 * */
	private boolean fillPatterns (ArrayList<double[]> intervals, String label ) {
		
		Enumeration<double[]> e = Collections.enumeration(intervals);
		
		if (e==null) {
			System.out.println ("There is no defined intervals");
			return (false);
		}
		
		ArrayList<DoubleMatrix1D> pats = null;
		double interval[] = null;
		while(e.hasMoreElements()) {
			interval = e.nextElement();
			pats = this.matrix.getPatterns(interval[0], interval[1]);
			if (pats==null) {
				System.out.println ("Problems reading patterns from matrix: "+Arrays.toString(interval));
				return (false);
			}
			this.patterns.addPatterns(pats, label, interval[0], this.setup.getBinSize());
	    }
		return (true);
	}
	
	private FastVector buildAtts() {
		FastVector atts=null;
		ArrayList <String> neuronNames = spikes.getNeuronNames();
		int numOfBinsPerNeuron = this.patterns.getDimension()/neuronNames.size();
		int numOfNeurons = neuronNames.size();
		
		Enumeration<String> e = Collections.enumeration(neuronNames);
		
		if (e==null) {
			System.out.println ("There is no defined neuron names");
			return (null);
		}
		
		String neuronName = "";
		int i=0;
		atts = new FastVector();
		while(e.hasMoreElements()) {
			neuronName = e.nextElement();
			for (i=0; i<numOfBinsPerNeuron; i++) {
				atts.addElement(new Attribute(neuronName+"_" + i));
			}
		}
		
		
	//	int numBins = patterns
		
		
		
		return atts;
	}

}
