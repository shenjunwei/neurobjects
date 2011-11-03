package nda.analysis.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    protected Random random;
    protected boolean verbose;


    protected DatasetEvaluator(EvaluatorSetup _setup) {
        setup = _setup;
        random = new Random();
        setVerbose(false);
    }


    public void reSeed(long seed) {
        random.setSeed(seed);
    }


    public abstract List<EvaluationResult> evaluate() throws EvaluationException;


    protected List<EvaluationResult> evaluateRound(
            GeneratorSetup.Dataset dataset, int round,
            Instances trainData, Instances testData) throws EvaluationException {

        if (setup.isCrossValidation()) {
            int numFolds = setup.getCVNumFolds();
            return evaluateCrossValidation(dataset, round, trainData, numFolds);
        }
        else {
            return evaluateTrainTest(dataset, round, trainData, testData);
        }
    }


    /**
     * Evaluate a single round of a dataset for all classifiers
     */
    private List<EvaluationResult> evaluateTrainTest(
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


    /**
     * Evaluate all folds for a single round of a dataset
     */
    private List<EvaluationResult> evaluateCrossValidation(
            GeneratorSetup.Dataset dataset, int round,
            Instances data, int numFolds)
            throws EvaluationException {

        List<EvaluationResult> results = new ArrayList<EvaluationResult>();

        List<NamedClassifier> classifiers = setup.getClassifiers();
        data.setClass(data.attribute("label"));

        for (NamedClassifier n_classifier : classifiers) {
            try {
                List<Evaluation> evaluations = getCrossValidationEvaluations(
                        n_classifier.getClassifier(), data, numFolds);

                int cvId = 1;
                for (Evaluation evaluation : evaluations) {
                    EvaluationResult result = new EvaluationResult();
                    result.dataset = dataset;
                    result.trainSetName = data.relationName();
                    result.testSetName = "<no_test>";
                    result.roundNumber = round;
                    result.cvFoldNumber = cvId++;
                    result.classifier = n_classifier;
                    result.evaluation = evaluation;
                    results.add(result);
                }
            }
            catch (Exception e) {
                throw new EvaluationException(e);
            }
        }

        return results;
    }


    /**
     * This is a duplicate of the logic from EvaluationUtils.getCVPredictions.
     * 
     * The replication is due to the need for results in the form Evaluation
     * instances instead of Prediction's.
     */
    private List<Evaluation> getCrossValidationEvaluations(
            Classifier originalClassifier,
            Instances data, int numFolds) throws Exception {

        List<Evaluation> results = new ArrayList<Evaluation>();
        Instances cvData = new Instances(data);
        cvData.randomize(random);

        if (cvData.classAttribute().isNominal() && (numFolds > 1))
            cvData.stratify(numFolds);

        for (int fold = 0; fold < numFolds; ++fold) {
            Instances trainData = cvData.trainCV(numFolds, fold, random);
            Instances testData = cvData.testCV(numFolds, fold);

            Classifier model = Classifier.makeCopy(originalClassifier);

            // Train
            model.buildClassifier(trainData);

            // Classify
            Evaluation evaluation = new Evaluation(trainData);
            evaluation.evaluateModel(model, testData);

            // Store
            results.add(evaluation);
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
