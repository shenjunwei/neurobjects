package nda.analysis.evaluation;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.RBFNetwork;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import nda.analysis.Setup;


/**
 * AbstractEvaluator
 * 
 * @author Giuliano Vilela
 */
public abstract class DatasetEvaluator {
    protected List<Classifier> models;


    protected DatasetEvaluator() {
        loadDefaultModels();
    }


    public abstract List<EvaluationResult> evaluate() throws EvaluationException;


    protected EvaluationResult evaluate(
            Setup.Dataset dataset,
            Instances trainData, Instances testData)
    throws EvaluationException {

        trainData.setClass(trainData.attribute("label"));
        testData.setClass(testData.attribute("label"));

        EvaluationResult result = new EvaluationResult(
                dataset, trainData.relationName(), testData.relationName());

        for (Classifier model : models) {
            try {
                // Train
                model.buildClassifier(trainData);

                // Classify
                Evaluation evaluation = new Evaluation(trainData);
                evaluation.evaluateModel(model, testData);

                // Store
                result.addEvaluation(evaluation);
            }
            catch (Exception e) {
                throw new EvaluationException(e);
            }
        }

        return result;
    }


    public void addModel(Classifier model) {
        models.add(model);
    }


    public void removeModel(Classifier model) {
        models.remove(model);
    }


    public List<Classifier> getModels() {
        return models;
    }


    protected void loadDefaultModels() {
        models = new ArrayList<Classifier>();

        models.add(new NaiveBayes());
        models.add(new J48());
        models.add(new RBFNetwork());
        models.add(new SMO());
    }
}
