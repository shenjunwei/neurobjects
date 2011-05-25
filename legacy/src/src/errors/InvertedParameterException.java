package errors;

/**
 *  \brief Represents a exception that occurs when two or more parameters are passed to a method in an erroneous order of magnitude.
 *  For example: someMethod(int firstTime, int LastTime) needs that the firstTime parameter be minor or equal to the LastTime parameter \n
 *  If firstTime > lastTime this exception will be called.
 *  
 *  
 * @author ambar
*/
public class InvertedParameterException extends IllegalArgumentException {
	
	private static final long serialVersionUID = 5114324521075873647L;
	
	private static final String msg = "You have entered two parameters that are in inverse order of magnitude";

	public InvertedParameterException(String msg) {
		super(msg);
	}
	
	public InvertedParameterException() {
		super(msg);
		
	}

}
