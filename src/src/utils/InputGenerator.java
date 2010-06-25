package utils;

import java.util.ArrayList;

public class InputGenerator {
	
	DataSetBuilder 			data = null;
	AnimalSetup	   			animal = null;
	int						numberOfSamples = 0;
	int						bundleSize=0;
	boolean 				done=false;
	ArrayList<Dataset>		buffer = null;
	String					jobText="";
	 
	
	public InputGenerator (AnimalSetup s, int numSamples, int bundleSize, String initialPath) {
		
		if ()
		
		this.animal = s;
		
		this.numberOfSamples = numSamples;
		this.data = new DataSetBuilder(s);
		this.buffer = new ArrayList<Dataset>(); 
		
	}
	
	

}
