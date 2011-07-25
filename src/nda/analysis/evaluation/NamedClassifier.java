package nda.analysis.evaluation;

import weka.classifiers.Classifier;


/**
 * Just a Weka classifier with an associated name.
 *
 * @author Giuliano Vilela
 */
public class NamedClassifier {
    private String name;
    private Classifier classifier;


    public NamedClassifier(Classifier classifier) {
        this(classifier.getClass().getName(), classifier);
    }


    public NamedClassifier(String _name, Classifier _classifier) {
        name = _name;
        classifier = _classifier;
    }


    public String getName() {
        return name;
    }


    public Classifier getClassifier() {
        return classifier;
    }
}
