package nda.analysis.evaluation;

import java.util.ArrayList;
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
     * Evaluate a single round of a dataset for all classifiers
     */
    protected List<EvaluationResult> evaluateTrainTest(
            GeneratorSetup.Dataset dataset, int round,
            Instances trainData, Instances testData)
            throws EvaluationException {

        List<EvaluationResult> results = new ArrayList<EvaluationResult>();

        List<NamedClassifier> classifiers = setup.getClassifiers();
        trainData.setClass(trainData.attribute("label"));
        testData.setClass(testData.attribute("label"));

        for (NamedClassifier n_classifier : classifiers) {

            EvaluationResult result = new EvaluationResult();

            try {
                Classifier originalModel = n_classifier.getClassifier();
                Classifier model = Classifier.makeCopy(originalModel);

                // Train
                model.buildClassifier(trainData);

                // Classify
                Evaluation evaluation = new Evaluation(trainData);
                evaluation.evaluateModel(model, testData);

                // Store result
                result.dataset = dataset;
                result.trainSetName = trainData.relationName();
                result.testSetName = testData.relationName();
                result.roundNumber = round;
                result.classifier = n_classifier;
                result.evaluation = evaluation;
                results.add(result);
            }
            catch (Exception e) {
                throw new EvaluationException(e);
            }
        }

        return results;
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
