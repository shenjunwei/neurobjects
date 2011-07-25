package nda.analysis.generation;


/**
 * Thrown to indicate that an exceptional error ocurred when
 * generating datasets from a DatasetGenerator.
 * 
 * @author Giuliano Vilela
 */
public class GenerationException extends Exception {
    private static final long serialVersionUID = -3481591819591142338L;

    public GenerationException() { }

    public GenerationException(String message) {
        super(message);
    }

    public GenerationException(Throwable cause) {
        super(cause);
    }

    public GenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
