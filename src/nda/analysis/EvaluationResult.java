package nda.analysis;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Evaluation;


/**
 * 
 * @author Giuliano Vilela
 */
public class EvaluationResult {
    private Setup.Dataset dataset;
    private List<Evaluation> modelEvaluations;
    private String trainSetName, testSetName;


    public EvaluationResult(Setup.Dataset dataset) {
        this(dataset, "<train>", "<test>");
    }


    public EvaluationResult(Setup.Dataset _dataset, String train_n, String test_n) {
        dataset = _dataset;
        trainSetName = train_n;
        testSetName = test_n;
        modelEvaluations = new ArrayList<Evaluation>();
    }


    @Override
    public String toString() {
        String str = String.format(
                "###\n# train_set: %s\n# test_set: %s\n###\n\n\n",
                trainSetName, testSetName
        );

        int model_i = 1;
        for (Evaluation evaluation : modelEvaluations) {
            str += "## Model " + model_i++ + " ##\n\n";

            try {
                str += evaluation.toSummaryString("=== Summary ===", false) + "\n";
                str += evaluation.toClassDetailsString() + "\n";
                str += evaluation.toMatrixString() + "\n";
            }
            catch (Exception e) {
                // wtf?
                e.printStackTrace();
            }
        }

        return str + "\n\n";
    }


    public Setup.Dataset getDataset() {
        return dataset;
    }


    public List<Evaluation> getModelEvaluations() {
        return modelEvaluations;
    }


    public void addEvaluation(Evaluation evaluation) {
        modelEvaluations.add(evaluation);
    }
}
