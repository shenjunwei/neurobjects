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

	
	
	public Dataset (Instances trainData, Instances testData, String animal, String label, String area ) {
		
		this.trainData = new Instances (trainData);
		this.testData = new Instances (testData);
		
		this.animal= animal;
		this.label = label;
		this.area = area;
		
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

	
}
