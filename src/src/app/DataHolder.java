package app;

import java.io.Serializable;

import DataGenerator.AnimalSetup;

import weka.core.Instances;

public class DataHolder implements Serializable {
	
	private Instances dataTrain = null;
	private Instances dataTest = null;
	private AnimalSetup setup = null;
	
	public DataHolder (Instances dataTrain, Instances dataTest) {
		
		this.dataTrain = new Instances (dataTrain);
		this.dataTest  = new Instances (dataTest);
	}
	
	public DataHolder (AnimalSetup a) {
		this.setup = a;
	}
	

}
