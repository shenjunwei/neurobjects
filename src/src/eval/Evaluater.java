package eval;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import utils.Context;
import utils.DatasetBuffer;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import errors.InvalidArgumentException;

public class Evaluater {
	
	
	weka.classifiers.Classifier cModel = null;
	String 						models[] = {"NBayes","MLP","J48","RBF","SVM"};
	ArrayList<String> 			supportedModels=null;
	String 						dataFiles[]=null;			
	
	
	public Evaluater (String dataFiles[]) {
		
		this.buildModelList();
		
	}
	
	private void buildModelList () {
		this.supportedModels = new ArrayList<String>();
		int numModels = this.models.length;
		int i=0;
		
		for (i=0; i<numModels; i++) {
			this.supportedModels.add (this.models[i]);
		}
		
	}
	public synchronized void  run () throws FileNotFoundException, IOException {
		
		String trainFilename = "";
		String testFilename = "";
		for (int i=0; i<this.dataFiles.length; i++) {
			trainFilename = this.dataFiles[i];
			testFilename = trainFilename.replaceFirst(".trn.", ".tst.");
			for (int j=0; j<this.models.length; j++ ) {
				this.setModel(this.models[j]);
				this.eval (trainFilename,testFilename);
                


			}
		}
	}
	
	private synchronized Evaluation eval(String trainFilename,
			String testFilename) {
		Instances dataTrain = null;
		try {
			dataTrain = new Instances(new BufferedReader(new FileReader(
					trainFilename)));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Test set
		Instances dataTest = null;
		try {
			dataTest = new Instances(new BufferedReader(new FileReader(
					testFilename)));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		dataTrain.setClassIndex(dataTrain.numAttributes() - 1);
		dataTest.setClassIndex(dataTest.numAttributes() - 1);
		try {
			cModel.buildClassifier(dataTrain);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Evaluation eval = null;
		try {
			eval = new Evaluation(dataTrain);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			eval.evaluateModel(cModel, dataTest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (eval);

	}

	
private synchronized void  setModel (String model) {
		
		this.cModel = null;
		if (model.equals("NBayes")) {
			this.cModel = new weka.classifiers.bayes.NaiveBayes();	
		}
		if (model.equals("MLP")) {
			this.cModel = new weka.classifiers.functions.MultilayerPerceptron();
		}
		if (model.equals("J48")) {
			this.cModel = new weka.classifiers.trees.J48();
		}
		if (model.equals("RBF")) {
			this.cModel = new weka.classifiers.functions.RBFNetwork();
		}
		if (model.equals("SVM")) {
			this.cModel = new weka.classifiers.functions.SMO();
			try {
				this.cModel.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (this.cModel==null) {
			//TODO creates a exception !!
			new InvalidArgumentException("Unknown model: " + model);
			return;
		}
		
	}

}
