package nda.data;

import java.util.List;
import java.util.Set;


/**
 * Handles animal behavior information
 * 
 * This component should provide the interface between the animal behavior data and the
 * application. The animal behavior data contains information about important aspects of
 * the animal behavior represented as a set of Intervals, in which the animal showed the
 * respective behavior. Each possible behavior is represented by a String label.
 *
 * Therefore in our model the animal behavior is captured as a set of
 * (label, time interval) pairs. The label must be a string and time interval must be
 * formed using two numbers. Ex:
 * 
 * \code ball 2010 2012 \endcode
 * means that the time interval [2010;2012] has been labeled with 'ball' tag.
 * 
 * \code north 1000 1005.5 \endcode
 * means that the time interval [2010;2012] has been labeled with 'north' tag.
 *
 * @author Nivaldo Vasconcelos
 */
public interface BehaviorHandlerI {

    /**
     * Return the set of all possible animal behaviors, represented
     * as Strings.
     */
    public Set<String> getLabelSet();


    /**
     * Returns a list of intervals tagged with a given label.
     * 
     * This method enables finding, from the animal behavior data, the list of time
     * intervals associated with a given label. Ex: If one needs know the set of time
     * intervals in which the time was labeled with 'north' the following
     * call will suffice:
     * 
     * <tt>List<Interval> intervals = BH.getIntervals("north");</tt>
     * 
     * @param label
     *            label target.
     * @return Returns a list of intervals tagged with a give label as list of
     *         1D vector, or a \c null if the label was not found in animal
     *         behavior description.
     * @author Nivaldo Vasconcelos
     */
    public List<Interval> getContactIntervals(String label);

    public void setContactIntervals(String label, List<Interval> intervals);


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
