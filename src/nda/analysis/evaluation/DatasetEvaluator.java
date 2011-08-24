package nda.analysis.evaluation;

import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import nda.analysis.generation.GeneratorSetup;
import nda.util.Verbose;


/**
 * AbstractEvaluator
 * 
 * @author Giuliano Vilela
 */
public abstract class DatasetEvaluator implements Verbose {
    protected EvaluatorSetup setup;
    protected boolean verbose;


    protected DatasetEvaluator(EvaluatorSetup _setup) {
        setup = _setup;
        setVerbose(false);
    }


    public abstract List<EvaluationResult> evaluate() throws EvaluationException;


    /**
     * Evaluate a single round of a dataset
     */
    protected EvaluationResult evaluate(
            GeneratorSetup.Dataset dataset,
            Instances trainData, Instances testData)
    throws EvaluationException {

        List<NamedClassifier> classifiers = setup.getClassifiers();

        EvaluationResult result = new EvaluationResult(
                dataset, trainData.relationName(), testData.relationName());
        result.setClassifers(classifiers);

        trainData.setClass(trainData.attribute("label"));
        testData.setClass(testData.attribute("label"));

        for (NamedClassifier n_classifier : classifiers) {
            try {
                Classifier model = n_classifier.getClassifier();

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


    @Override
    public void setVerbose(boolean _verbose) {
        verbose = _verbose;
    }


    @Override
    public boolean getVerbose() {
        return verbose;
    }
}
