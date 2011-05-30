package nda.data.text;


/**
 * Thrown to indicate that a method tried to read an invalid data file.
 * 
 * Possible scenarios that can cause this error: corrupted text file or a data file
 * that doesn't conform to the input specification.
 * 
 * @author Giuliano Vilela
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
