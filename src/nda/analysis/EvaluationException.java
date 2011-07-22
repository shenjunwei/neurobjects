package nda.analysis;


/**
 * Thrown to indicate that an exceptional error ocurred
 * when evaluating datasets.
 * 
 * @author Giuliano Vilela
 */
public class EvaluationException extends Exception {
    private static final long serialVersionUID = 4481591829591142338L;

    public EvaluationException() { }

    public EvaluationException(String message) {
        super(message);
    }

    public EvaluationException(Throwable cause) {
        super(cause);
    }

    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
