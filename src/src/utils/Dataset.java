package utils;

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

import weka.core.Instances;


/** Defines the dataset model 
 * 
 * */
public class Dataset {
	
	Instances trainData=null;
	Instances testData=null;

	String 	  tag = "";
	Properties properties = null;
	

	
	
	public Dataset (Instances trainData, Instances testData,AnimalSetup animal, String label, String area ) {
		
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
	
	public Dataset (Properties setup) {
		this.properties = new Properties();
		this.properties.values = setup.cloneTable();
		/*this.properties.setProperty("animal", setup.getValue("animal"));
		this.properties.setProperty("bin_size", setup.getValue("bin_size"));
		this.properties.setProperty("window_width", setup.getValue("window_width"));
		this.properties.setProperty("label", setup.getValue("label"));
		this.properties.setProperty("area", setup.getValue("area")); */
		 
		
	}
	
	public void setTrainData (Instances trainData) {
		this.trainData = new Instances (trainData);
	}
	
	public void setTestData (Instances testData) {
		this.testData = new Instances (testData);
	}
	
	public String toString () {
		
		String result="\nAnimal: "+this.properties.getValue("animal")+ "\tLabel: "+this.properties.getValue("label")+"\tArea:"+this.properties.getValue("area")+"\n";
		
		result+="\nTraining set:\n "+this.trainData;
		result+="\nTest set: \n "+this.testData;
		return (result);
	}
	
	public void save (String path) throws UnsupportedEncodingException, FileNotFoundException {
		String baseFilename = path+File.separatorChar+this.buildBaseFilename();
		String trnFilename=baseFilename+".trn.arff";
		String tstFilename=baseFilename+".tst.arff";
		
		this.saveSingleDataset(trnFilename, this.trainData.toString());
		this.saveSingleDataset(tstFilename,this.testData.toString());
	}
	
	public String getTrainFilename() {
		String baseFilename = this.buildBaseFilename();
		return (baseFilename+".trn.arff");		
	}
	public String getTstFilename() {
		String baseFilename = this.buildBaseFilename();
		return (baseFilename+".tst.arff");		
	}
	
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
	
	public boolean isValid() {
		if ((this.trainData != null) && (this.testData != null)) {
			return (true);
		}
		return (false);
	}
	
	public void saveZip (ZipOutputStream out) throws IOException {
		
		String dirPath = "."+File.separatorChar+this.hashCode()+File.separatorChar; 
	
		this.saveSingleDatasetZip(dirPath+this.getTrainFilename(), this.trainData.toString(), out);
        this.saveSingleDatasetZip(dirPath+this.getTstFilename(), this.testData.toString(), out);
	}
	
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
	
	public Instances getTrainData() {
		return trainData;
	}

	public Instances getTestData() {
		return testData;
	}

	public String getAnimal() {
		return this.properties.getValue("animal");
	}

	public String getLabel() {
		return this.properties.getValue("label");
	}

	public String getArea() {
		return this.properties.getValue("area");
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public double getBinSize() {
		return Double.parseDouble(this.properties.getValue("bin_size"));
		//return (this.binSize);
	}

	public int getWindowWidth() {
		return Integer.parseInt(this.properties.getValue("window_width"));
	}
	
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
