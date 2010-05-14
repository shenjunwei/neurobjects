package utils;

import java.util.ArrayList;
import cern.colt.matrix.DoubleMatrix1D;

/** @brief Handle animal behavior informations 
 * 
 * This component should be provide the interface between the animal behavior data and the application.
 *  The animal behavior data contains information about important aspects of the animal behavior models
 *  as a set of intervals, in which each element has a bahavior label and its respective time interval. 
 *  Therefore in our model the animal behavior is captured as a set of: label and a time interval. \n
 *  Ex: \n
 *  \code ball 2010 2012 \endcode 
 *  means that the time interval [2010;2012] has been labeled with 'ball' tag. 
 *  \code north 1000 1005 \endcode 
 *  means that the time interval [2010;2012] has been labeled with 'north' tag.   
 *
 */
public interface BehavHandlerI {
	
	/** \brief Returns a list of intervals tagged with a give label.
	 *  
	 *  This method allow knows, from the animal behavior data, the list of time intervals associeted with 
	 *  a give label.
	 *  Ex: If one needs know the set of time intervals in which the time was labeled with 'north' the 
	 *  following call will provide:
	 *  
	 *   \code ArrayList<DoubleMatrix1D> timeList = BH.getIntervals("north"); \endcode
	 *  
	 * @param label label target.
	 * @return Returns a list of intervals tagged with a give label as list of 2D vector.
	 */
	public ArrayList<DoubleMatrix1D> 		getIntervals(String label);
	public double[] 						getBigInterval(ArrayList<String> labels);
 
}
