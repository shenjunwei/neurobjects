package utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import weka.core.Instances;


/** Defines the dataset model 
 * 
 * */
public class Dataset {
	
	Instances trainData;
	Instances testData;
	String    animal="";
	String    label="";
	String    area="";
	double 	  binSize=0.0;
	int 	  windowWidth=0;
	String 	  tag = "";

	
	
	public Dataset (Instances trainData, Instances testData,AnimalSetup animal, String label, String area ) {
		
		this.trainData = new Instances (trainData);
		this.testData = new Instances (testData);
		
		this.animal= animal.getName();
		this.binSize = animal.getBinSize();
		this.windowWidth = animal.getWindowWidth();
		this.label = label;
		this.area = area;
		
		this.trainData.setClassIndex(this.trainData.numAttributes()-1);
		this.testData.setClassIndex(this.testData.numAttributes()-1);
		
	}
	
	public String toString () {
		
		String result="\nAnimal: "+animal + "\tLabel: "+this.label+"\tArea:"+this.area+"\n";
		
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
	
	public void saveZip (ZipOutputStream out) throws IOException {
		this.saveSingleDatasetZip(this.getTrainFilename(), this.trainData.toString(), out);
        this.saveSingleDatasetZip(this.getTstFilename(), this.testData.toString(), out);
	}
	
	public void saveSingleDatasetZip (String filename, String data, ZipOutputStream out) throws IOException {
		
		byte content[] = data.toString().getBytes("UTF-8");
		ZipEntry entry = new ZipEntry(filename);
		out.putNextEntry(entry);
		out.write(content, 0, content.length);
		
	}
	
	private void saveSingleDataset(String filename, String data) throws UnsupportedEncodingException {
		FileOutputStream out = null;
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
		String baseFilename = this.animal+"."+this.area+"."+this.label+"."+this.binSize+"."+this.windowWidth+"."+this.hashCode();
		return (baseFilename);
	}
	
	public Instances getTrainData() {
		return trainData;
	}

	public Instances getTestData() {
		return testData;
	}

	public String getAnimal() {
		return animal;
	}

	public String getLabel() {
		return label;
	}

	public String getArea() {
		return area;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public double getBinSize() {
		return (this.binSize);
	}

	public int getWindowWidth() {
		return windowWidth;
	}
	
	public int getNumAtts() {
		return (this.trainData.numAttributes());
	}

	
}
