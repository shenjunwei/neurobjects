package nda.data.text;

/**
 * @author giulianoxt
 */
public class InvalidDataFileException extends Exception {
    private static final long serialVersionUID = -1481591818590032438L;

    public InvalidDataFileException() { }

    public InvalidDataFileException(String message) {
        super(message);
    }

    public InvalidDataFileException(Throwable cause) {
        super(cause);
    }

    public InvalidDataFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
