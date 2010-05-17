package utils;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * Models the spike train information as a time series.
 * 
 * A set of spike, a spike train, s=[t1 t2 ... tN], is modeled as a 1D vector,
 * in which element, xi is equal to ti from spike train s. Therefore, the models 
 * associate a spike train to a 1D vector following:
 * \code
 * 
 * s=[t1 t2 ... tN] <-> v=(x1,x2, ...,xN), where x1=t1, x2=t2, ... , xN=tN.
 * \endcode
 * @author Nivaldo Vasconcelos
 * @date 17Mai2010
 */
public class SpikeTrain {
	
	private  DoubleMatrix1D times;
	private  String			name;
	
	
	public SpikeTrain(DoubleMatrix1D setTime, String setName) {
		
		this.times = setTime.copy();
		this.name = setName;
	}
	
	public SpikeTrain(String fileName) {
		
		
	}
	
	public SpikeTrain(String fileName, double a, double b) {
		
		
	}
	
	public SpikeTrain(String fileName, String name) {		
		
	}
	
	public SpikeTrain(String fileName, String name, double a, double b) {		
		
	}

	public DoubleMatrix1D getTimes() {
		return times;
	}

	public void setTimes(DoubleMatrix1D times) {
		this.times = times;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
