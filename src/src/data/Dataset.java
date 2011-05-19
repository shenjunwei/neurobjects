package data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.omg.CORBA.DynAnyPackage.InvalidValue;

import errors.InvalidArgumentException;

import DataGenerator.AnimalSetup;

import utils.Properties;
import weka.core.Instances;

/**
 * \brief Defines the dataset model
 * 
 * A dataset object is mainly defined by two sets: training set and a testing
 * set. This class provides a set of methods to handle this kind of information.
 * 
 * */
public class Dataset {
	
	Instances trainData=null;
	Instances testData=null;

	//String 	  tag = "";
	public Properties properties = null;

	/** \brief Creates a dataset object.
	 * 
	 * Creates a dataset object based on: training dataset, testing dataset, information about the animal set
	 * up, label, and area. Uses as class index the last attribute.
	 * @param trainData training dataset 
	 * @param testData testing dataset 
	 * @param animal Information about the animal setup
	 * @param label label to be used by the dataset
	 * @param area area related to dataset 
	 * @throws InvalidArgumentException 
	 */
	public Dataset (Instances trainData, Instances testData,AnimalSetup animal, String label, String area ) throws InvalidArgumentException {
		this.validSetup(trainData, testData, animal, label, area);
		
		
		this.trainData = new Instances (trainData);
		this.testData = new Instances (testData);
		this.properties = new Properties();
		
		this.properties.setProperty("animal", animal.getName());
		this.properties.setProperty("bin_size", animal.getBinSize()+"");
		this.properties.setProperty("window_width", animal.getWindowWidth()+"");
		this.properties.setProperty("label", label);
		this.properties.setProperty("area", area);
		
		this.trainData.setClassIndex(this.trainData.numAttributes()-1);
		this.testData.setClassIndex(this.testData.numAttributes()-1);
		
	}
	
	private void validSetup (Instances trainData, Instances testData,AnimalSetup animal, String label, String area ) throws InvalidArgumentException {
		
		if ( (trainData==null) || (testData==null)) {
			throw new InvalidArgumentException("null pointer in instances information  !!");	
		}
		
		if (animal==null) {
			throw new InvalidArgumentException("null pointer in animal information !!");
			
		}
		if (label==null) {
			throw new InvalidArgumentException("null pointer label information !!");
		}
		
		if (area==null) {
			throw new InvalidArgumentException("null pointer in area information !!");
		}
		
		
		if ( (trainData.numAttributes()==0) || (testData.numAttributes()==0) ) {
			throw new InvalidArgumentException("Empty set in instances information !!");
		}
		
		if (!trainData.equalHeaders(testData)) {
			throw new InvalidArgumentException("Training and testing instances have different headers !!");

		}
		
		// Validates animal information
		if ( (animal.getName().isEmpty()) || (animal.getBinSize()<=0) || (animal.getWindowWidth()<=0) ) {
			throw new InvalidArgumentException("Empty values in animal information !!");
		}
		
		// Validates label information
		if (label.isEmpty()) {
			throw new InvalidArgumentException("Empty value in label information !!");
		}
			
		}
	
	
	/**
	 * \brief Creates a dataset instances
	 * 
	 * Creates a dataset instances based on a Properties object in which should
	 * be defined the following properties: 
	 *  - animal: name of the animal in which the dataset has been built. Ex: "ge5";
	 *  - bin_size: size, in milliseconds, of bin used to build the dataset. Ex: 250;
	 *  - window_width: number of bins for each neuron;
	 *  - label: label to be used by the dataset. Ex: "HP", "High";
	 *  - area: name of area related to dataset. Ex: "HP"
	 * 
	 * @param setup
	 */
	public Dataset (Properties setup) {
		this.properties = new Properties();
		this.properties.setValues(setup.cloneTable());
		
	}
	 
	
	/** \brief Defines the training dataset
	 * 
	 * Creates a new Instances based on given dataset and use it as the training dataset.
	 * 
	 * @param trainData training dataset
	 */
	public void setTrainData (Instances trainData) {
		this.trainData = new Instances (trainData);
	}
	
	/** \brief Defines the testing dataset
	 * 
	 * Creates a new Instances based on given dataset and use it as the training dataset.
	 * 
	 * @param testData testing dataset
	 */
	public void setTestData (Instances testData) {
		this.testData = new Instances (testData);
	}
	
	/** \brief Returns a string with the dataset informations 
	 * 
	 * Very useful to print a dataset
	 * 
	 * @return a String in which there are informations about the dataset
	 */
	public String toString () {
		
		String result="\nAnimal: "+this.properties.getValue("animal")+ "\tLabel: "+this.properties.getValue("label")+"\tArea:"+this.properties.getValue("area")+"\n";
		
		result+="\nTraining set:\n "+this.trainData;
		result+="\nTest set: \n "+this.testData;
		return (result);
	}
	
	/**
	 * \brief Saves the dataset in file
	 * 
	 * Given a path, saves the dataset in two files, with specific names. The
	 * training set will be saved using sufix filename ".trn.arff" and the
	 * testing set will be saved using the sufix filename ".tst.arff" 
	 * 
	 * @param path path to directory where the files should be saved.
	 * \todo verify the give path (exists, permissions, etc)
	 * */
	public void save (String path) throws UnsupportedEncodingException, FileNotFoundException {
		String baseFilename = path+File.separatorChar+this.buildBaseFilename();
		String trnFilename=baseFilename+".trn.arff";
		String tstFilename=baseFilename+".tst.arff";
		
		this.saveSingleDataset(trnFilename, this.trainData.toString());
		this.saveSingleDataset(tstFilename,this.testData.toString());
	}
	
	/** \brief Returns the training filename 
	 * 
	 * @return the training filename 
	 */
	public String getTrainFilename() {
		String baseFilename = this.buildBaseFilename();
		return (baseFilename+".trn.arff");		
	} 
	
	/** \brief Returns the testing filename 
	 * 
	 * @return the testing filename 
	 */
	public String getTstFilename() {
		String baseFilename = this.buildBaseFilename();
		return (baseFilename+".tst.arff");		
	}
	
	/**
	 * \brief Saves the dataset into a zip file
	 * 
	 * Given a filename (including the path to file, e.g.: "/tmp/file.zip"), saves the dataset in a zip file with that filename, one
	 * file for training, and one file for testing dataset. The training file
	 * uses the sufix filename ".trn.arff", and the testing file uses the sufix
	 * filename ".tst.arff"
	 * 
	 * @param zipfilename String with the zipfile filename and path to file. (e.g.: '/tmp/someFile.zip')
	 * @throws IOException
	 */
	public void saveZip (String zipfilename) throws IOException {
		if (!this.isValid()) {
			new InvalidValue("Dataset internal content is not valid!!");
		}
		String baseFilename = this.buildBaseFilename();
		String trnFilename=baseFilename+".trn.arff";
		String tstFilename=baseFilename+".tst.arff";
				
        FileOutputStream dest = new FileOutputStream(zipfilename);
        CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));
        this.saveSingleDatasetZip(trnFilename, this.trainData.toString(), out);
        this.saveSingleDatasetZip(tstFilename, this.testData.toString(), out);
        out.close();
	}
	
	
	/** \brief Informs if the current dataset is valid 
	 * 
	 * @return \c true if is valid or \c false otherwise.
	 */
	public boolean isValid() {
		if ((this.trainData != null) && (this.testData != null)) {
			return (true);
		}
		return (false);
	}
	
	
	/**
	 * \brief Put the dataset files into a zip file stream 
	 * 
	 * Given a zip stream, saves the dataset in a zip file with that filename, one
	 * file for training, and one file for testing dataset. The training file
	 * uses the sufix filename ".trn.arff", and the testing file uses the sufix
	 * filename ".tst.arff"
	 * 
	 * @param out zip output stream in which should be saved the dataset files
	 * @throws IOException
	 */
	public void saveZip (ZipOutputStream out) throws IOException {
		
		String dirPath = "."+File.separatorChar+this.hashCode()+File.separatorChar; 
	
		this.saveSingleDatasetZip(dirPath+this.getTrainFilename(), this.trainData.toString(), out);
        this.saveSingleDatasetZip(dirPath+this.getTstFilename(), this.testData.toString(), out);
	}
	
	/**
	 * \brief Saves a single dataset file into a into a zip file stream
	 * 
	 * This methods saves into that zip stream the String data with the filename
	 * 
	 * @param out zip stream in which should be saved the String data;
	 * @param filename filename to be used in zip out strem, when the String data is saved;
	 * @parm data String data that should be saved in zip out stream.
	 *            zip output stream in which should be saved the dataset files
	 * @throws IOException
	 * 
	 * \todo Este método deveria ser privado.
	 */
	public void saveSingleDatasetZip (String filename, String data, ZipOutputStream out) throws IOException {
		
		if (!this.isValid()) {
			new InvalidValue("Dataset internal content is not valid!!");
		}
		
		String fileContent = this.buildHeaderInfo();
		
		fileContent +=  data.toString();
		
		byte content[] = fileContent.getBytes("UTF-8");
		ZipEntry entry = new ZipEntry(filename);
		out.putNextEntry(entry);
		out.write(content, 0, content.length);
		
	}
	
	private String buildHeaderInfo() {
		
		String fileContent = "%<setup>\n";
		fileContent += this.properties.toComment("%");
		fileContent +=   "%</setup>\n\n";
		return fileContent;
		
	}
	
	private void saveSingleDataset(String filename, String data) throws UnsupportedEncodingException {
		FileOutputStream out = null;
		if (!this.isValid()) {
			new InvalidValue("Dataset internal content is not valid!!");
		}
		try {
			out = new FileOutputStream(filename);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		byte content[] = data.toString().getBytes("UTF-8");
		try {
			out.write (content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private String buildBaseFilename () {
		String baseFilename = ""+this.hashCode();
		return (baseFilename);
	}
	
	/** \brief Returns the training data 
	 * 
	 * @return The training data in the Instances format.
	 */
	public Instances getTrainData() {
		return trainData;
	}

	/** \brief Returns the testing data 
	 * 
	 * @return The testing data in the Instances format.
	 */
	public Instances getTestData() {
		return testData;
	}

	/** \brief Returns the animal name related to the dataset. 
	 * 
	 * When is created the dataset should be related to a animal name. For example: ge5.
	 * This methods returns this information.
	 * 
	 * @return The animal name related to the dataset.
	 */
	public String getAnimal() {
		return this.properties.getValue("animal");
	}

	/** \brief Returns the label related to the dataset. 
	 * 
	 * When is created the dataset should be related to a label. For example: "HP", or "High"
	 * This methods returns this information.
	 * 
	 * @return The label related to the dataset.
	 */
	public String getLabel() {
		return this.properties.getValue("label");
	}

	/**
	 * \brief Returns the area related to the dataset.
	 * 
	 * When is created the dataset should be related to a area. Normally this
	 * information is used to associate the dataset with a anatomic area, ex: S1, V1.
	 * 
	 * @return The area related to the dataset.
	 */
	public String getArea() {
		return this.properties.getValue("area");
	}
	
	/*public void setTag(String tag) {
		this.tag = tag;
	} */
	
	/**
	 * \brief Returns the size of bins used to build the dataset.
	 * 
	 * The dataset is built based from a spike counting process, and in this
	 * process is used a size of bin to get the counting. This method returns,
	 * in milliseconds, this size of bin used.
	 * 
	 * @return The size of bins used to build the dataset.
	 */
	public double getBinSize() {
		return Double.parseDouble(this.properties.getValue("bin_size"));
		//return (this.binSize);
	}

	/**
	 * \brief Returns the size of window used to build the dataset.
	 * 
	 * The dataset is built getting the information  based from a spike counting process, and in this
	 * process is used a size of bin to get the counting. This method returns,
	 * in milliseconds, this size of bin used.
	 * 
	 * @return The size of bins used to build the dataset.
	 */
	public int getWindowWidth() {
		return Integer.parseInt(this.properties.getValue("window_width"));
	}
	
	/** \brief Returns the number of attributes in the dataset
	 * 
	 * @return number of attributes in the dataset*/
	public int getNumAtts() {
		return (this.trainData.numAttributes());
	}
	
	public boolean isTrainingFilename(String filename) {
		
		if (filename.endsWith(".trn.arff")){
			return true;
		}
		return false;
	}
	
	public boolean isTestingFilename(String filename) {
		
		if (filename.endsWith(".tst.arff")){
			return true;
		}
		return false;
	}

	public Properties getProperties() {
		return properties;
	}

	
}
