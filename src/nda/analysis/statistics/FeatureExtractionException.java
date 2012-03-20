package nda.analysis.statistics;


/**
 * Thrown to indicate that an exceptional error ocurred when
 * extracting features from a dataset;
 * 
 * @author Giuliano Vilela
 */
public class FeatureExtractionException extends Exception {
    private static final long serialVersionUID = -3481591819891142338L;

    public FeatureExtractionException() { }

    public FeatureExtractionException(String message) {
        super(message);
    }

    public FeatureExtractionException(Throwable cause) {
        super(cause);
    }

    public FeatureExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
