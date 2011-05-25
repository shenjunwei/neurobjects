package errors;

public class EmptySourceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2982434287862137561L;
	private static final String msg = "The source is empty";
	
	public EmptySourceException(String message) {
		super(message);
	}
	
	public EmptySourceException() {
		super(msg);
	}
	

	
	

}
