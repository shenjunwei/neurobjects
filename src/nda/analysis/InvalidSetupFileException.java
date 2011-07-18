package nda.analysis;

/**
 * Thrown to indicate that a method tried to read an invalid setup file.
 * 
 * Possible scenarios that can cause this error: corrupted setup file or a setup file
 * that doesn't conform to the input specification.
 * 
 * @author Giuliano Vilela
 */
public class InvalidSetupFileException extends Exception {
    private static final long serialVersionUID = -3481591818590031238L;

    public InvalidSetupFileException() { }

    public InvalidSetupFileException(String message) {
        super(message);
    }

    public InvalidSetupFileException(Throwable cause) {
        super(cause);
    }

    public InvalidSetupFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
