package nda.analysis.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import weka.classifiers.Evaluation;

import nda.analysis.Setup;


/**
 * 
 * @author Giuliano Vilela
 */
public class EvaluationResult {
    private Setup.Dataset dataset;
    private List<NamedClassifier> classifiers;
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
        classifiers = new ArrayList<NamedClassifier>();
    }


    @Override
    public String toString() {
        String str = String.format(
                "###\n# train_set: %s\n# test_set: %s\n###\n\n\n",
                trainSetName, testSetName
        );

        for (int i = 0; i < classifiers.size(); ++i) {
            NamedClassifier classifier = classifiers.get(i);
            Evaluation evaluation = modelEvaluations.get(i);

            String[] options = classifier.getClassifier().getOptions();
            String optionsStr = StringUtils.join(options, ' ').trim();
            if (optionsStr.isEmpty())
                optionsStr = "<default>";

            String modelHeader = String.format(
                    "## Model: %s (%s) ##\n\n",
                    classifier.getName(), optionsStr);

            str += modelHeader;

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


    public void setClassifers(List<NamedClassifier> _classifiers) {
        classifiers = _classifiers;
    }


    public List<NamedClassifier> getClassifiers() {
        return classifiers;
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
