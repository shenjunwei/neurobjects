package DataGenerator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.activity.InvalidActivityException;

import errors.InvertedParameterException;
import errors.MissingDataFileException;


/**
 * \page TxtSpkHandlerTests Tests on TxtSpkHandler
 * 
 * The following tests were performed (in tests.utils_TxtSpkHandler): \n
 * 
 * 
 * 
 */


/** \todo Implements using hash table 
 * 
 * 
 *   
 * 
 * */
public class TxtSpkHandler implements SpkHandlerI {
	
	String animal="";
	String filter = "";
	String dataPath= "";
	double a=Double.NaN;
	double b=Double.NaN;
	ArrayList<SpikeTrain> neurons = null;
	/** \brief The file extension of spike files where some methods will look for spikes */
	private String spkFileExtension = ".spk"; 
	private int totalOfSpikes = 0;
	
/*	REMOVED 
 *  Cause: for while, it is not necessary deal with situation in which there are no interval [a;b] definitions 
 *  Nivaldo 07June2010 
	public TxtSpkHandler (String dataSourcePath, String filter) throws MissingDataFileException {
		this.filter = filter;
		this.dataPath = dataSourcePath;
		this.readSpikes(dataSourcePath, filter);
		// TODO implements
		
	} */
	
	public TxtSpkHandler (String dataSourcePath, String filter, double a, double b ) throws MissingDataFileException, InvertedParameterException, IOException {
		// TODO implements
		if (!this.validateInterval(a, b)) {
		return;
		}
		this.a = a;
		this.b = b;
		this.filter = filter;
		this.dataPath = dataSourcePath;
		this.readSpikes(dataSourcePath, filter,a,b);
		
	}
	public String getSourceType() {
		return ("TXT");
	}
	
	public String getAnimalName() {	
		return animal;
	}
	
	
	public SpikeTrain getSpikes(String name) {
		int i=0;
		
		// is there this neuron
		while (i < this.neurons.size() && 
		(!this.neurons.get(i).getName().equalsIgnoreCase(name)))
 	    {
			i++;
		}
		if (this.neurons.get(i).getName().equalsIgnoreCase(name)) {
			return (this.neurons.get(i));
		}
		// TODO implements
		return ((SpikeTrain) null);
	}
	
	/**
	 * \todo implement this method in SpikeTrain.
	 */
	public SpikeTrain getSpikes(String name, double a, double b) {
		
		new InvalidActivityException("SpikeTrain getSpikes(String name, double a, double b): method not implemented !!");
		// TODO implements
		// TODO implement this method in SpikeTrain.
		return ((SpikeTrain) null);			
	}
	
	public void setFilter(String filter) {
	//	this.filter = filter;
	}
	
	public String getFilter() {
		return (filter);
	}
	
	public ArrayList<String> getNeuronNames() {
		ArrayList<String>  names = new ArrayList<String> ();  
		for (int i=0; i<this.neurons.size(); i++) {
			names.add(this.neurons.get(i).getName());
		}
		return (names);
		
	}
	
	public ArrayList<SpikeTrain> getAllSpikes() {
		return (this.neurons);
		// TODO implements
		
	}
	
	public ArrayList<SpikeTrain> getAllSpikes(double a, double b) {
		// TODO implements
		return ((ArrayList<SpikeTrain>) null);
	}

	
	public int getNumberOfNeurons() {	
		return (neurons.size());
	}
	
	
	public double firstSpike() {
		int num = neurons.size();
		double first = Double.MAX_VALUE;
	
		if (num!=0) {
			for (int i = 0; i < num; i++) {
				if (neurons.get(i).first < first) {
					first = neurons.get(i).first;
				}
			}
			return (first);
		}
		return (Double.NaN);
	}
	
	
	
	public double lastSpike() {	
		int num = neurons.size();
		double last = Double.MIN_VALUE;

		if (num!=0) {
			for (int i = 0; i < num; i++) {
				if (neurons.get(i).last > last) {
					last = neurons.get(i).last;
				}
			}
			return (last);
		}
		return (Double.NaN);
	}
	
	public String toString () {
		String str = "";
		for (int i=0; i<this.neurons.size(); i++) {
			str = str + this.neurons.get(i).toString();
		}
		return (str);
	}
	private boolean validateInterval(double a, double b){
		
		if ( (a<b) ){
			return true;
		}
		
		return false;
	}
	
	private String[] getDirList(String path) {
		
		 
		// Gets the list of files in the dataset path
		File dir = new File(path);
		if (!dir.canRead()) {
			System.err.println ("(TxtSpikeTrain.readSpikes)\nERROR : Can not read the given path: "+path+"\n");
			return null;
		}
		// 
		String name[] = dir.list();
		return (name);
		
	}
	
	/**
	 * \brief This method read the spikes that are on spike data files, given the Path and the filter
	 * @param path - Folder where the spike data files are
	 * @param filter - Part of the name of desired data files (can also be an empty string)
	 * @throws MissingDataFileException
	 */
	private void readSpikes (String path, String filter) throws MissingDataFileException {

		this.neurons = new ArrayList<SpikeTrain>();
		String filterLowerCase = filter.toLowerCase(); //Used inside loops
		
		String name[] = this.getDirList(path); // Getting the directory listing	 
		if (name==null) {
			throw new MissingDataFileException();
		} 

		int numberOfFiles = name.length;
		if (numberOfFiles==0)
		{
			throw new MissingDataFileException();
		}
		
		//When no filter is passed as argument this method try to read all files that have the extension this.spkFileExtension (in directory) 
		if (filter.isEmpty()) {
			for (int i = 0; i < numberOfFiles; i++) {
				String fileName = name[i].toLowerCase();
				if (fileName.endsWith(this.spkFileExtension)) {
					this.neurons.add(new TxtSpikeTrain(path + "/"+name[i]));
				}
			}
		}
		else { 	//Applying the filter
			for (int i = 0; i < numberOfFiles; i++) {
				String fileName = name[i].toLowerCase(); 
				if (fileName.startsWith(filterLowerCase) && fileName.endsWith(this.spkFileExtension)) {
					this.neurons.add(new TxtSpikeTrain(path + "/"+name[i]));
				}
			}
		}
		
		//If there's no spike data file(s) in directory...
		if (this.neurons.isEmpty())
		{
			throw new MissingDataFileException();
		}
	}
	// --- //
	
	
	/**
	 * \brief This method read the spikes that are on spike data files, given the Path, the filter,  and the interval a, b 
	 * @param path - Folder where the spike data files are
	 * @param filter - Part of the name of desired data files (can also be an empty string)
	 * @param a - First desired time
	 * @param b - Last desired time
	 * @throws InvertedParameterException
	 * @throws MissingDataFileException
	 * @throws IOException
	 */
	private void readSpikes (String path, String filter, double a, double b) throws InvertedParameterException, MissingDataFileException, IOException {
		
		if (a>b)
		{
			throw new InvertedParameterException();
		}
		
		String filterLowerCase = filter.toLowerCase(); //Used inside loops
		
		File dir = new File(path); // Gets the list of files in the dataset path
		String name[] = dir.list();
		if (name==null) {
			throw new MissingDataFileException("Error:Problems reading spikes: " + path+'/'+filter+"*.spk");
		}
		java.util.Arrays.sort(name); // Sort the neuron names 


		int numberOfFiles = name.length;
		if (numberOfFiles==0) {
			throw new MissingDataFileException();
		}

		TxtSpikeTrain spikes=null;
		this.neurons = new ArrayList<SpikeTrain>();
		
		//Applying the filter
		if (!filter.isEmpty()) {
			for (int i = 0; i < numberOfFiles; i++) {
				String fileName = name[i].toLowerCase();
				if (fileName.startsWith(filterLowerCase) && fileName.endsWith(this.spkFileExtension)) {
					spikes = new TxtSpikeTrain(path + "/"+name[i],a,b);
					if (spikes.isValid()) {
						totalOfSpikes+=spikes.numberOfSpikes;
						this.neurons.add(spikes);
					}
				}
			}
		}
		else { //When no filter is passed as argument this method try to read all files that have the extension this.spkFileExtension (in directory) 
			for (int i = 0; i < numberOfFiles; i++) {
				String fileName = name[i].toLowerCase();
				if (fileName.endsWith(this.spkFileExtension))
				{
					spikes = new TxtSpikeTrain(path + "/"+name[i],a,b);
					if (spikes.isValid()) {
						this.neurons.add(spikes);
					}
				}
			}
		}

		//If there's no spike data file(s) in directory...
		if (this.neurons.isEmpty())
		{
			throw new MissingDataFileException();
		}
	}
	// --- //
	
	
	public double beginInterval() {
	return(this.a);

	}
	
	public double endInterval() {
		return (this.b);
	}
	
	public SpikeTrain getSpikeTrain( int i) {
		
	return (this.neurons.get(i));	
	}
	public int getTotalOfSpikes() {
		return totalOfSpikes;
	}
}

