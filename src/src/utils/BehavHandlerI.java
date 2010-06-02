package utils;

import java.util.ArrayList;
import cern.colt.matrix.DoubleMatrix1D;
/** \page behaveDataSpec Behavior Data Format Spec
 * 
 * In our model the animal behavior is captured as a set of: label and a time interval. \n
 * The label must be a string and time interval must be formed using two numbers .
 * For example, consider that in animal bahavior data there is the following information:
 * \code
 * label01,1010,1014
 * label01,1015,1020 
 * label03,900,902
 * label04,900,915
 * \endcode	
 * 
 * @author Nivaldo Vasconcelos
 * */


/** \brief   Handle animal behavior informations 
 * 
 * This component should be provide the interface between the animal behavior data and the application.
 *  The animal behavior data contains information about important aspects of the animal behavior models
 *  as a set of intervals, in which each element has a bahavior label and its respective time interval. 
 *  Therefore in our model the animal behavior is captured as a set of: label and a time interval. \n
 *  The label must be a string and time interval must be formed using two numbers .
 *  Ex: \n
 *  \code ball 2010 2012 \endcode 
 *  means that the time interval [2010;2012] has been labeled with 'ball' tag. 
 *  \code north 1000 1005.5 \endcode 
 *  means that the time interval [2010;2012] has been labeled with 'north' tag.
 *  
 *     \see Behavior Data Format Spec
 *  @author Nivaldo Vasconcelos
 *  @date 17Mai2010
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
	 * @author Nivaldo Vasconcelos
	 */
	public ArrayList<DoubleMatrix1D> 		getIntervals(String label);
	
	/**
	 * \brief Returns a big interval in which the animal execute a set of given
	 * behavior
	 * 
	 * Given a set of labels this method returns a interval I in which all
	 * intervals associated with each given label is contained. \n For example,
	 * consider that in animal behavior data there is the following information:
	 * \code 1010,1014,label01 
	 * 1015,1020,label03 
	 * 900,902,label04 
	 * 900,915,label01
	 * \endcode
	 * 
	 * If the given list of labels is: {label01,label03}; should be returned the
	 * following interval: \code [900;1020] \endcode
	 * 
	 * @param labels
	 *            list of target labels
	 * @return a 2D vector with the big interval [a,b].
	 * @author Nivaldo Vasconcelos
	 * */
	public double[] 						getBigInterval(String labels);
 
}

