package nda.analysis.evaluation;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

import weka.classifiers.Evaluation;

import nda.analysis.generation.GeneratorSetup;


/**
 * Result of the evaluation of a single round of a dataset.
 * 
 * @author Giuliano Vilela
 */
public class EvaluationResult {
    public GeneratorSetup.Dataset dataset;
    public String trainSetName;
    public String testSetName;
    public int roundNumber;
    public int cvFoldNumber;
    public Date creationTime;

    public NamedClassifier classifier;
    public Evaluation evaluation;


    public EvaluationResult() {
        dataset = null;
        trainSetName = "<train>";
        testSetName = "<test>";
        roundNumber = -1;
        cvFoldNumber = -1;
        creationTime = new Date();
        classifier = null;
        evaluation = null;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("dataset", dataset.getName())
        .append("trainSet" , trainSetName)
        .append("testSet", testSetName)
        .append("round", roundNumber)
        .append("cvFoldNumber", cvFoldNumber)
        .append("creationTime", creationTime)
        .append("classifier", classifier.getName())
        .append("auroc", evaluation.weightedAreaUnderROC())
        .toString();
    }


    public Object getParameter(String parameter) {
        return dataset.getParameter(parameter);
    }
}
