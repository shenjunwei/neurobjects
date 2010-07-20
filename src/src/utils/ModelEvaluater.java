package utils;

import java.util.ArrayList;
import java.util.Hashtable;

import data.Dataset;

import DataGenerator.DatasetBuffer;

import errors.EmptySourceException;
import errors.InvalidArgumentException;

import weka.classifiers.Evaluation;
import weka.core.Instances;

/** \brief Thread to implements a set of evaluaters */
public class ModelEvaluater extends Thread {
	
	String 						model="";
	Instances 					trainData=null;
	Instances 					testData=null;
	
	weka.classifiers.Classifier cModel = null;
	DatasetBuffer 				dataBuffer = null;
	boolean 					done = false;
	String 						models[] = {"NBayes","MLP","J48","RBF","SVM"};
	ArrayList<String> 			supportedModels=null;
//	boolean						singleMode = false;
//	String 						stdModel = "NBayes";
	
	public ModelEvaluater(DatasetBuffer buffer) {
		
        super();
        this.buildModelList();
        this.dataBuffer = buffer; 
    }
	
	private void buildModelList () {
		this.supportedModels = new ArrayList<String>();
		int numModels = this.models.length;
		int i=0;
		
		for (i=0; i<numModels; i++) {
			this.supportedModels.add (this.models[i]);
		}
		
	}

	public void run() {
		Dataset data = null;
		Evaluation eval = null;
		while (!this.done) {
			while ((this.dataBuffer.isEmpty()) && (!this.done)) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			synchronized (this) {
				if ((!this.dataBuffer.isEmpty())  && (!this.done)) {
					try {
						this.model = this.dataBuffer.nextModel();
					} catch (EmptySourceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						data = this.dataBuffer.getDataset(this.model);
					} catch (EmptySourceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (data != null) {
				this.trainData = data.getTrainData();
				this.testData = data.getTestData();
				this.setModel();
				
				// Builds the classifier based on training data informations
				try {
					this.cModel.buildClassifier(this.trainData);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Evaluates the classifier model
				try {
					eval = new Evaluation(this.trainData);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					eval.evaluateModel(cModel, this.testData);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		/*		this.context.setAUROC(eval.weightedAreaUnderROC());
				this.context.setFMeasure(eval.fMeasure(0));
				this.context.setKappa(eval.kappa());
				this.context.setPctCorrect(eval.pctCorrect());
				this.context.setEndTime(System.currentTimeMillis());
				this.context.showSQL("ioc_results_basic2"); */
			}

			
		}

	}
	
	private synchronized void  setModel () {
		
		this.cModel = null;
		if (this.model.equals("NBayes")) {
			this.cModel = new weka.classifiers.bayes.NaiveBayes();	
		}
		if (this.model.equals("MLP")) {
			this.cModel = new weka.classifiers.functions.MultilayerPerceptron();
		}
		if (model.equals("J48")) {
			this.cModel = new weka.classifiers.trees.J48();
		}
		if (this.model.equals("RBF")) {
			this.cModel = new weka.classifiers.functions.RBFNetwork();
		}
		if (this.model.equals("SVM")) {
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
			new InvalidArgumentException("Unknown model: " + this.model);
			return;
		}
		
	}

	public ArrayList<String> getSupportedModels() {
		return supportedModels;
	}

	

	public boolean isDone() {
		return done;
	}

	public synchronized void setDone(boolean done) {
		this.done = done;
	}

}
