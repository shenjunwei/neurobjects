package nda.data.text;

/**
 * Thrown to indicate that a method tried to open an inexistent file.
 * 
 * @author Giuliano Vilela
 */
public class MissingDataFileException extends Exception {
    private static final long serialVersionUID = -3909662700445854547L;

    public MissingDataFileException() { }

    public MissingDataFileException(String message) {
        super(message);
    }

    public MissingDataFileException(Throwable cause) {
        super(cause);
    }

    public MissingDataFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
