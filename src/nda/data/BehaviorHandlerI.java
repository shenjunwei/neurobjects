package nda.data;

import java.util.List;
import java.util.Set;



/** \brief   Handles animal behavior informations
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
public interface BehaviorHandlerI {

    public Set<String> getLabelSet();


    /**
     * \brief Returns a list of intervals tagged with a give label.
     * 
     * This method allow knows, from the animal behavior data, the list of time
     * intervals associated with a give label. Ex: If one needs know the set of
     * time intervals in which the time was labeled with 'north' the following
     * call will provide:
     * 
     * \code double[] timeList = BH.getIntervals("north"); \endcode
     * 
     * @param label
     *            label target.
     * @return Returns a list of intervals tagged with a give label as list of
     *         1D vector, or a \c null if the label was not found in animal
     *         behavior description.
     * @author Nivaldo Vasconcelos
     */
    public List<Interval> getContactIntervals(String label);


    public String getLabel(double time);


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
    public Interval getExpositionInterval();
}
