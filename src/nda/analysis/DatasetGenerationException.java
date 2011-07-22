package nda.analysis;


/**
 * Thrown to indicate that an exceptional error ocurred when
 * generating datasets from a DatasetGenerator.
 * 
 * @author Giuliano Vilela
 */
public class DatasetGenerationException extends Exception {
    private static final long serialVersionUID = -3481591819591142338L;

    public DatasetGenerationException() { }

    public DatasetGenerationException(String message) {
        super(message);
    }

    public DatasetGenerationException(Throwable cause) {
        super(cause);
    }

    public DatasetGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
