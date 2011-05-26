package nda.data.text;

/**
 * 
 * @author giulianoxt
 */
public class InvalidDataDirectoryException extends Exception {
    private static final long serialVersionUID = -9120670480556201544L;

    public InvalidDataDirectoryException() { }

    public InvalidDataDirectoryException(String message) {
        super(message);
    }

    public InvalidDataDirectoryException(Throwable cause) {
        super(cause);
    }

    public InvalidDataDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
