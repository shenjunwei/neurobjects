package utils;

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

	
}
