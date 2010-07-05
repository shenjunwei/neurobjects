package DataGenerator;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/**
 * \brief Models the spike train information as a time series.
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
 * \todo Documentation
 */
abstract class SpikeTrain {
	
	/** 1D vector to store the spike times */
	DoubleMatrix1D times=null;
	
	/** Spike train name*/
	String			name="";
	
	/** Total number of spikes in 'times' attribute */
	int			numberOfSpikes=0;
	
	/** value of first spike time */
	double			first=0;
	
	/** value of last spike time */
	double			last=0;
	
	/** is it valid this spike train ?*/
	boolean valid=false;
	
	
	
	/** \brief Returns the time series of the spike train. 
	 * 
	 * Returns a 1D vector in which is time series representing the spike train.
	 * @return a 1D vector with the times of spike train
	 * 
	 * */
	public DoubleMatrix1D getTimes() {
		return times;
	}

	/** \brief  Returns the spike train name. 
	 * 
	 * @return spike train name */
	public String getName() {
		return name;
	}

	/** \brief  Defines the spike train name.
	 * 
	 * @param name to be used by spike train.
	 * */
	public void setName(String name) {
		this.name = name;
	}
	
	/** \brief Returns the first time in the spike train
	 * 
	 *  @return the value of the first time in the spike train */
	public double getFirst() {
		return first;
	}

	/** \brief Returns the last time in the spike train
	 * 
	 *  @return the value of the last time in the spike train */
	public double getLast() {
		return last;
	}
	
	/** \brief  Informs if the spike train content is valid.
	 * 
	 * @return \c TRUE if the spike is valid \b or \n
	 *         \c FALSE otherwise */
	public boolean isValid() {
		return (valid);
	}
	
	/**
	 * \brief Returns the total number of spikes in 'times' attribute
	 * @return
	 */
	public int getNumberOfSpikes() {
		return numberOfSpikes;
	}

	

		
	
}
