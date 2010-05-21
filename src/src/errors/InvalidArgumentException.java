package errors;

/**
 * \brief Represents a exception that occurs when an Invalid Argument is passed to a method.
 * @author Edson Anibal de Macedo Reis Batista
 */
public class InvalidArgumentException extends Exception {

	private static final long serialVersionUID = -814346027495390691L;

	private static final String msg = "The parameter that you have entered is invalid";
	
	
	
	public InvalidArgumentException(String message) {
		super(message);
	}
	
	public InvalidArgumentException() {
		super(msg);
	}
	
	

}
