/** \page */
package DataGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import cern.colt.matrix.DoubleMatrix1D;
import errors.InvalidArgumentException;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;


/** \brief Generates and handles Dataset's 
 * 
 * */
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
	 * 
	 * Defines the positive and negative set sizes to train and test steps
	 */
	public DataSetBuilder (AnimalSetup s) {
		this.setup = s;
		this.numPositiveSamplesToTrain = (int) Math.floor(this.setup.getTotalSamples()*this.setup.getAlfa());
		this.numPositiveSamplesToTest = this.setup.getTotalSamples()-this.numPositiveSamplesToTrain;
		this.numNegativeSamplesToTrain = (int) Math.floor(this.numPositiveSamplesToTrain*this.setup.getBeta());
		this.numNegativeSamplesToTest = (int) Math.floor(this.numPositiveSamplesToTest*this.setup.getBeta());
	}
	
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/**
	 * \brief Sets the configuration parameters from XML file to internal class
	 * parameters.
	 * 
	 * Defines the positive and negative set sizes to train and test steps
	 * 
	 * @throws Exception
	 */
	public DataSetBuilder (AnimalSetup s, String path, String currentFilter) throws Exception {
		this.setup = s;
		this.numPositiveSamplesToTrain = (int) Math.floor(this.setup.getTotalSamples()*this.setup.getAlfa());
		this.numPositiveSamplesToTest = this.setup.getTotalSamples()-this.numPositiveSamplesToTrain;
		this.numNegativeSamplesToTrain = (int) Math.floor(this.numPositiveSamplesToTrain*this.setup.getBeta());
		this.numNegativeSamplesToTest = (int) Math.floor(this.numPositiveSamplesToTest*this.setup.getBeta());
	
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	
	
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/**
	 * \brief Builds a Job Description File
	 * 
	 * Based on a list of zip files, in whose there are the datasets saved, and
	 * a path to application builds the content of a Job Description File (jdf)
	 * to run over those datasets.
	 * 
	 * @param zipfiles
	 *            list of zip files
	 * @param pathToApp
	 *            path to application
	 * @return
	 */
	public String buildJDF (ArrayList<String> zipfiles, String pathToApp, String dirLib) {
		
		Enumeration<String> f = Collections.enumeration(zipfiles);
		String filename="";
		String jdfContent="job : \nlabel  : NDA."+this.hashCode()+"\n\n";
		String tmp[] = pathToApp.split(File.separatorChar+"");
		String appName = tmp[tmp.length-1];
		
		
		while (f.hasMoreElements()) {
			jdfContent += "task :\n";
			filename = f.nextElement();
			jdfContent +="init : store "+filename+" "+filename+"\n";
		//	jdfContent +="\tstore "+dirLib+File.separatorChar+"colt.jar colt.jar\n";
			jdfContent +="\tstore "+dirLib+File.separatorChar+"weka.jar weka.jar\n";
			jdfContent +="\tput "+pathToApp+" "+appName+"\n";
			jdfContent +="remote : java -cp $STORAGE/weka.jar:$PLAYPEN/"+appName+" app.EvaluaterApp $STORAGE/"+filename+" $JOB $TASK > output-$JOB.$TASK.log\n";
			jdfContent +="final: get output-$JOB.$TASK.log output-$JOB.$TASK.log\n\n\n";
			
		}
		return (jdfContent);
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	
	
	/** \brief Sweeps parameters to generates Dataset's and put them in a buffer
	 * 
	 * This is the main method in DataSetBuilder class. 
	 * 
	 * 
	 * @param buffer DatasetBufferSingle in which should be stored the dataset's.
	 * @param table_name name of table in which will be stored the results
	 * @param jobName job name 
	 * @param numOfSamples number of instances for each point
	 * @return A list of filename in whose has been stored the Dataset's
	 * @throws Exception
	 */
	public ArrayList<String> run (DatasetBufferSingle buffer, String table_name, String jobName, int numOfSamples) throws Exception {
		
		String filter = "";
		String label = "";
		String path = buffer.getWorkDir();
		Dataset data = null;
		ArrayList<String> zipfiles = new ArrayList<String> ();
		int i=0;
		if (!this.setup.validFilters())  {
			new InvalidArgumentException("Invalid filter!"
					+ this.setup.getFilters());
			return null;
		}
		if (!this.setup.validLabels())  {
			new InvalidArgumentException("Invalid labels!"
					+ this.setup.getFilters());
			return null;
		}
		if (numOfSamples<=0) {
			new InvalidArgumentException("Invalid number of samples !"
					+ numOfSamples);
			return null;
		}
		
		String zipfilename = "";
		int zipCount = 0;
		Enumeration <String> f = Collections.enumeration(this.setup.getFilters());
		while (f.hasMoreElements()) {
			filter = f.nextElement();
			Enumeration <String> l = Collections.enumeration(this.setup.getLabels());
			while (l.hasMoreElements()) {
				label = l.nextElement();
				System.out.println (filter+">"+label);
				for (i = 0; i < numOfSamples; i++) {
					
					try {
					data = this.get(filter, label);
					data.properties.setProperty("table_name", table_name);
					data.properties.setProperty("job", jobName);
					} catch (Exception e) {};
				
					if (!buffer.add(data)) {
						if (buffer.isFull()) {
							zipfilename = this.setup.getName() + "."
									+ this.setup.getArea(filter) + "."
									+ buffer.hashCode() + "." + zipCount
									+ ".zip";
							zipfiles.add(zipfilename);
							zipfilename = path + File.separatorChar
									+ zipfilename;
							buffer.saveZip(zipfilename); // empty buffer
							zipCount++;
						}
						buffer.add(data); // Stay here !!
					}
					 
					
				}				
			}
		}
		if (!buffer.isEmpty()) {
			zipfilename = this.setup.getName()+"."+this.setup.getArea(filter)+"."+ buffer.hashCode()+"."+zipCount+".zip";
			zipfiles.add (zipfilename);
			zipfilename=path+File.separatorChar+zipfilename;
			buffer.saveZip(zipfilename);		            
			zipCount++;
		}
		return (zipfiles);
		
		
		
	}
	
	
	
	private Instances[] getInstances (String filter, String positiveLabel) throws Exception {
			
		 
		if (!this.validBuildParams(filter, positiveLabel)) {
			//TODO exception  
			return null;
		}
	//	System.out.println ("Building data");
		if (!this.dataIsReady(filter, positiveLabel)) {
			System.out.println ("Building data");
			this.buildData(filter);
		}
		
	//	System.out.println ("Building instances");
		return(this.buildInstances(positiveLabel));
	} 
	
	/** \brief Given a filter and a positive labe, returns a Dataset 
	 * 
	 * @param positiveLabel positive label to be used in the Dataset building
	 * @param filter name of filter to be used in the Dataset building */ 
	public Dataset get (String filter, String positiveLabel) throws Exception {
		
		Instances[] dataVector = this.getInstances(filter, positiveLabel);
		String area = this.setup.getArea(filter);
		/*if (area==null) {
			new InvalidArgumentException("invalid filter value to define area");
		} */
		if (spikes.getNumberOfNeurons()==0) {
			new InvalidArgumentException("There is no spike with given information");
		}
		Dataset	data = new Dataset(dataVector[0], dataVector[1], this.setup, positiveLabel, area);
		
		return data;
	
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
					//System.out.print (".");
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
			System.out.println ("There is no spikes definition");
			return (false);
		}
		if (spikes.getNumberOfNeurons()==0) {
			new InvalidArgumentException("There is no neuron !!");
			return (false);
		} 
		
		if (spikes.getTotalOfSpikes()==0) {
			new InvalidArgumentException("There is no spike !");
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
		
		
		// Is there enough samples with this positive label ?
		if (patterns.getPatterns(positiveLabel).size()<setup.totalSamples) {
			new InvalidArgumentException("There is no enough samples with this positive label: " +positiveLabel);
			
		}
		
		// Is there enough samples with this negative label ?		
		if (!this.isEnoughNegSamples(positiveLabel)) {
			new InvalidArgumentException("There is no enough negative samples with this positive label: " +positiveLabel);
		}
		
		if (!currentFilter.equals(filter)) {
			System.out.println ("Changed filter");
			return (false);
		}
		return (true);
		
	}
	
	private boolean isEnoughNegSamples(String positiveLabel) {
		ArrayList<String> others = new ArrayList<String>();
		others.addAll(setup.getLabels());
		others.remove(positiveLabel);
		Enumeration<String> e = Collections.enumeration(others);
		if (e==null) {
			return (false);
		}
		int totalNegativeSamples = 0;
		while(e.hasMoreElements()) {
			totalNegativeSamples += patterns.getPatterns(e.nextElement()).size();
	    }
		if (totalNegativeSamples<(setup.getBeta()*setup.totalSamples)) {
			return (false);
		}
		return (true);
		
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
		this.currentFilter = filter;
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
	
	public void saveFile (String str, String path) throws IOException {
		File f = new File(path);
		FileWriter fw = new FileWriter(f);
		fw.write(str);
		fw.close();
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
