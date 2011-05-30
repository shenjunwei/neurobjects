package nda.data.text;


/**
 * Thrown to indicate that a method tried to list an invalid directory.
 * 
 * Some possible scenarios include: the user don't have +rx permission on the directory
 * or the directory path is incorrect.
 * 
 * @author Giuliano Vilela
 */
public class InvalidDataDirectoryException extends Exception {
    private static final long serialVersionUID = -9120670480556201544L;

    public InvalidDataDirectoryException() { }

    public InvalidDataDirectoryException(String message) {
        super(message);
        new IllegalArgumentException();
    }

    public InvalidDataDirectoryException(Throwable cause) {
        super(cause);
    }

    public InvalidDataDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
