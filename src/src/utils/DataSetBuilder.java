package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import cern.colt.matrix.DoubleMatrix1D;
import errors.InvertedParameterException;
import errors.MissingDataFileException;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class DataSetBuilder {
	
	
	private  	int numPositiveSamplesToTrain=0;
	private  	int numPositiveSamplesToTest=0;
	private  	int numNegativeSamplesToTrain=0;
	private  	int numNegativeSamplesToTest=0;
	
	SpkHandlerI spikes = null;
	RateMatrixI matrix = null;
	BehavHandlerI behave = null;
	Patterns 	  patterns = null;
	AnimalSetup   setup = null;
	
	String 			currentFilter = "";
	
	/**
	 * \brief Sets the configuration parameters from XML file to internal class parameters.
	 * \n Defines the positive and negative set sizes to train and test steps
	 */
	public DataSetBuilder (AnimalSetup s) {
		this.setup = s;
		this.numPositiveSamplesToTrain = (int) Math.floor(this.setup.getTotalSamples()*this.setup.getAlfa());
		this.numPositiveSamplesToTest = this.setup.getTotalSamples()-this.numPositiveSamplesToTrain;
		this.numNegativeSamplesToTrain = (int) Math.floor(this.numPositiveSamplesToTrain*this.setup.getBeta());
		this.numNegativeSamplesToTest = (int) Math.floor(this.numPositiveSamplesToTest*this.setup.getBeta());
	}
	
	public Instances[] getInstances (String filter, String positiveLabel) throws Exception {
			
		 
		if (!this.validBuildParams(filter, positiveLabel)) {
			//TODO exception  
			return null;
		}
		System.out.println ("Building data");
		if (!this.dataIsReady(filter, positiveLabel)) {
			this.buildData(filter);
		}
		
		System.out.println ("Building instances");
		return(this.buildInstances(positiveLabel));
	}
	
	/**
	 * \brief Validate parameters
	 * @param filter
	 * @param positiveLabel
	 * @return
	 */
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
	
	private Instances[] buildInstances  ( String positiveLabel) {
		
		FastVector atts  = buildAtts();
		int numAllPositive = this.setup.totalSamples;
		int numAllNegative = (int) Math.floor(numAllPositive*this.setup.getBeta());
		int numLabels = this.setup.getLabels().size();
		int numOther = (int) Math.floor(numAllNegative/(numLabels-1));
		
		if (!validNumOthers(positiveLabel, numOther)) {
			System.out.println ("Invalid number of other labels for positive label equal to :" +positiveLabel);
			return (null);
		}
		return (buildDataSets(positiveLabel, atts));
	}
		
	private Instances[] buildDataSets(String positiveLabel, FastVector atts) {
		
		ArrayList<String> labels = this.setup.getLabels(); 
		Hashtable<String, ArrayList<Integer>> patIdxs = buildPatsIdx(labels);
		
		Enumeration <String> e = Collections.enumeration(labels);
		if (e==null) {
			System.out.println ("There is no labeled samples !! ");
			return (null);
		}
		String label = "";
		int idx;
		ArrayList<Integer> list = null;
		ArrayList<Pattern> patternList = null;
		double vals[] = null;
		int numOtherToTrain = (int) Math.floor(this.numNegativeSamplesToTrain/labels.size()-1);
		int numOtherToTest = (int) Math.floor(this.numNegativeSamplesToTest/labels.size()-1);
		int i=0;
		Random R = new Random ();
		FastVector attClass = new FastVector();
		attClass.addElement ("yes");
		attClass.addElement ("no");
		
		// Creates the datasets
		Instances 	data = new Instances("objectId", atts, 0);
		Instances 	dataTest = new Instances("objectId", atts, 0);
		Instances 	dataset[] = {data,dataTest};
		
		dataset[0] = data;
		dataset[1] = dataTest;
			
		while(e.hasMoreElements()) {
			label = e.nextElement();
			list = patIdxs.get(label);
			patternList = this.patterns.getPatterns(label);
			if (label.equals(positiveLabel)) {
				// Positive samples to train
				for (i=0; i<this.numPositiveSamplesToTrain; i++) {
					idx = R.nextInt(list.size()-1);
					System.out.print (".");
					vals = patternList.get(list.get (idx)).toWeka(attClass.indexOf("yes"));
					data.add(new Instance(1.0, vals.clone()));
					list.remove(idx);
				}
				// Positive samples to test
				for (i=0; i<this.numPositiveSamplesToTest; i++) {
					idx = R.nextInt(list.size()-1);
					vals = patternList.get(list.get (idx)).toWeka(attClass.indexOf("yes"));
					dataTest.add(new Instance(1.0, vals.clone()));
					list.remove(idx);
				}
				
			}
			else {
				// Negative sample to train
				for (i=0; i<numOtherToTrain ; i++) {
					idx = R.nextInt(list.size()-1);
					vals = patternList.get(list.get (idx)).toWeka(attClass.indexOf("no"));
					data.add(new Instance(1.0, vals.clone()));
					list.remove(idx);
				}
				// Negative sample to train
				for (i=0; i<numOtherToTest ; i++) {
					idx = R.nextInt(list.size()-1);
					vals = patternList.get(list.get (idx)).toWeka(attClass.indexOf("no"));
					dataTest.add(new Instance(1.0, vals.clone()));
					list.remove(idx);
				}
			}
	    }
		return (dataset);
		
	}
	
	private Hashtable<String, ArrayList<Integer>> buildPatsIdx (ArrayList<String> labels ) {
		if (labels==null) {
			System.out.println ("There is no labeled samples !! ");
			return null;
		}
		Hashtable<String, ArrayList<Integer>> patIdx = new Hashtable<String, ArrayList<Integer>>  ();
		
		Enumeration <String> e = Collections.enumeration(labels);
		if (e==null) {
			System.out.println ("There is no labeled samples !! ");
			return (null);
		}
		String label = "";
		int numOfPatterns =0;
		int i=0;
		ArrayList<Integer> list = null;
		while(e.hasMoreElements()) {
			label = e.nextElement();
			numOfPatterns = this.patterns.numPatterns(label);
			list = new ArrayList<Integer>(); 
			patIdx.put (label,list);
			for (i=0; i<numOfPatterns; i++) {
				list.add (i);
			}
			
	    }
		return (patIdx);
	}
	
	private boolean validNumOthers(String positiveLabel, int numOther) {
		ArrayList<String> labels = this.setup.getLabels();
		
		Enumeration <String> e = Collections.enumeration(labels);
		if (e==null) {
			System.out.println ("There is no labeled samples !! ");
			return (false);
		}
		String label = "";
		while(e.hasMoreElements()) {
			label = e.nextElement();
			if ( (label!=positiveLabel) && (this.patterns.numPatterns(label)<(3*numOther)) ) {
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
		if (spikes==null) {
			System.out.println ("There is spikes definition");
			return (false);
		}
		if (patterns==null) {
			System.out.println ("There is patterns definition");
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
	
	/**
	 * \brief Build data reading informations from XML file
	 * 
	 * @param filter
	 * @throws Exception
	 */
	private void buildData(String filter) throws Exception {
		this.behave = new BehavHandlerFile(setup.getPathToBehavior());
		
		if (this.behave==null) {
			System.out.println ("Was not possible read behavior from "+setup.getPathToBehavior());
		}
		String labels = this.setup.getLabels().toString();
		labels=labels.replace('[', ' ');
		labels=labels.replace(']', ' ');
		labels.trim();
		double intervals[] = this.behave.getBigInterval(labels);
		this.spikes = new TxtSpkHandler(this.setup.getPathToSpikes(), filter,intervals[0] , intervals[1]);
		if (this.spikes==null) {
			System.out.println ("Was not possible read the files !!");
		}
		this.matrix = new CountMatrix (this.spikes,this.setup.getBinSize());
		if (this.matrix==null) {
			System.out.println ("Was not possible create the count matrix ");
		}
		this.matrix.setWindowWidth(this.setup.getWindowWidth());
		this.patterns = new Patterns ();
		this.patterns.setNeuronNames(this.spikes.getNeuronNames());
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
		double width = this.setup.getWindowWidth()*this.setup.getBinSize();
		while(e.hasMoreElements()) {
			interval = e.nextElement();
			if (this.matrix.possibleInterval(interval[0], interval[1])) {
				pats = this.matrix.getPatterns(interval[0], interval[1]);
				if (pats == null) {
					System.err
							.println("Problems reading patterns from matrix: "
									+ Arrays.toString(interval));
					return (false);
				}
				this.patterns.addPatterns(pats, label, interval[0], this.setup
						.getBinSize());
			}
	    }
		return (true);
	}
	
	private FastVector buildAtts() {
		FastVector atts=null;
		FastVector attClass=null;
		ArrayList <String> neuronNames = spikes.getNeuronNames();
		int numOfBinsPerNeuron = this.setup.getWindowWidth();
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
		// Class label
		attClass = new FastVector();
		attClass.addElement ("yes");
		attClass.addElement ("no");
		atts.addElement(new Attribute("class",attClass));
		
		
		return atts;
	}

}
