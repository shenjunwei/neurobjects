package utils;

import java.util.ArrayList;
import java.util.Hashtable;

import weka.classifiers.Evaluation;
import weka.core.Instances;

public class ModelEvaluater extends Thread {
	
	String model="";
	Instances trainData=null;
	Instances testData=null;
	Context context=null;
	ModelEvaluater(Setup info, Instances trainData, Instances testData, String model) {
		
        super();
       
        this.model = model;
        this.trainData = trainData;
        this.testData = testData;
       // this.context = new Context (info,);
        
    }
	public void run () {
		weka.classifiers.Classifier cModel = null;
		if (model.equals("NBayes")) {
			cModel = new weka.classifiers.bayes.NaiveBayes();	
		}
		if (model.equals("MLP")) {
			cModel = new weka.classifiers.functions.MultilayerPerceptron();
		}
		if (model.equals("J48")) {
			cModel = new weka.classifiers.trees.J48();
		}
		if (model.equals("RBF")) {
			cModel = new weka.classifiers.functions.RBFNetwork();
		}
		if (model.equals("SVM")) {
			cModel = new weka.classifiers.functions.SMO();
			try {
				cModel.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (cModel==null) {
			//TODO creates a exception !!
			System.out.println ("Unknown model: " + model);
			return;
		}
		// Builds the classifier based on training data informations 
		try {
			cModel.buildClassifier(this.trainData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Evaluation eval = null;
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
		this.context.setAUROC(eval.weightedAreaUnderROC());
		this.context.setFMeasure(eval.fMeasure(0));
		this.context.setKappa(eval.kappa());
		this.context.setPctCorrect(eval.pctCorrect());
		this.context.setEndTime(System.currentTimeMillis());
		
	
		this.context.showSQL("ioc_results_basic2");
	
		
	}

}
