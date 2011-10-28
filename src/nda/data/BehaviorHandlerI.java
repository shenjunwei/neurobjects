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
 * (time interval, label) pairs. The label must be a string and time interval must be
 * formed using two numbers. Ex:
 * 
 * \code 2010 2012 ball \endcode
 * means that the time interval [2010;2012] has been labeled with the "ball" tag.
 * 
 * \code 1000 1005.5 north \endcode
 * means that the time interval [1000;1005.5] has been labeled with the "north" tag.
 *
 * @author Nivaldo Vasconcelos
 */
public interface BehaviorHandlerI {

    /**
     * @return The set of all possible animal behaviors, represented as Strings.
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
     * <tt>List<Interval> intervals = behavior.getIntervals("north");</tt>
     * 
     * @param label
     *            label target.
     * @return Returns a list of intervals tagged with a give label as list of
     *         1D vector, or \c null if the label was not found in animal
     *         behavior description.
     */
    public List<Interval> getContactIntervals(String label);


    /**
     * Change the list of contact intervals for a given label.
     * 
     * @param label The label to be modified
     * @param intervals The new list of intervals
     */
    public void setContactIntervals(String label, List<Interval> intervals);


    /**
     * Determine, for a given instant, which behavior the animal was presenting.
     * 
     * @param time The time instant
     * @return A label L such that there is a contact interval I of L that contains time
     */
    public String getLabel(double time);


    /**
     * Returns the smallest interval that contains all the contact intervals with
     * the behaviors in this BehaviorHandlerI.
     * 
     * For most experiments, it is the interval in which the animal has been exposed
     * with a given set of objects. For example, consider that in the animal behavior data
     * there is the following information:
     * 
     * \code
     * 1010,1014,label01
     * 1015,1020,label03
     * 900,902,label04
     * 900,915,label01
     * \endcode
     * 
     * This method should return [900, 1020].
     */
    public Interval getExpositionInterval();
}
