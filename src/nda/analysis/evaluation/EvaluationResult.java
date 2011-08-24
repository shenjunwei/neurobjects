package nda.analysis.evaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import weka.classifiers.Evaluation;

import nda.analysis.generation.GeneratorSetup;


/**
 * Result of the evaluation of a single round of a dataset.
 * 
 * @author Giuliano Vilela
 */
public class EvaluationResult {
    private GeneratorSetup.Dataset dataset;
    private List<NamedClassifier> classifiers;
    private List<Evaluation> modelEvaluations;
    private String trainSetName, testSetName;
    private Date creationTime;


    public EvaluationResult(GeneratorSetup.Dataset dataset) {
        this(dataset, "<train>", "<test>");
    }


    public EvaluationResult(GeneratorSetup.Dataset _dataset, String train_n, String test_n) {
        dataset = _dataset;
        trainSetName = train_n;
        testSetName = test_n;

        modelEvaluations = new ArrayList<Evaluation>();
        classifiers = new ArrayList<NamedClassifier>();
        creationTime = new Date();
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


    public void setCreationTime(Date time) {
        creationTime = time;
    }


    public List<NamedClassifier> getClassifiers() {
        return classifiers;
    }


    public GeneratorSetup.Dataset getDataset() {
        return dataset;
    }


    public List<Evaluation> getModelEvaluations() {
        return modelEvaluations;
    }


    public void addEvaluation(Evaluation evaluation) {
        modelEvaluations.add(evaluation);
    }


    public int numEvaluations() {
        return modelEvaluations.size();
    }


    public String getTrainSetName() {
        return trainSetName;
    }


    public String getTestSetName() {
        return testSetName;
    }


    public Object getParameter(String paramName) {
        return dataset.getParameter(paramName);
    }


    public Date getCreationTime() {
        return creationTime;
    }
}
