package nda.data.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nda.data.BehaviorHandlerI;
import nda.data.Interval;


/**
 * \brief Handles a animal behavior file
 * 
 * In our model the animal behavior is captured as a set of: label and a time
 * interval. \n The label must be a string and time interval must be formed
 * using two numbers . This class implements the interface BehavHandlerI when
 * the animal behavior is in a file. Please, see the \ref behaveDataSpec about
 * the specification of this kind of file.
 * 
 */
public class TextBehaviorHandler implements BehaviorHandlerI {
    private String filepath;
    private Map<String, List<Interval>> behavior;


    /** \brief Creates a BehavHandlerFile object given a filename
     * 
     * The file should be in a Behavior Data Format
     * 
     * @param filename name of file in which there is the animal behavior description
     * @see \ref behaveDataSpec.
     */
    public TextBehaviorHandler(String path)
    throws MissingDataFileException, InvalidDataFileException {
        filepath = path;
        behavior = new HashMap<String, List<Interval>>();

        File file = new File(filepath);
        if (!file.exists()) {
            throw new MissingDataFileException("File not found: " + filepath);
        }

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));

            String line;
            while (((line = in.readLine()) != null) ) {
                String[] values = line.split(",");

                if (values.length != 3) {
                    throw new InvalidDataFileException(
                    "Behavior file has wrong number of values on line");
                }

                double start = Double.parseDouble(values[0]);
                double end = Double.parseDouble(values[1]);
                String label = values[2].trim();

                addInterval(label, Interval.make(start, end));
            }

            in.close();
        }
        catch (NumberFormatException e) {
            throw new InvalidDataFileException(e);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidDataFileException(e);
        }
        catch (IOException e) {
            throw new InvalidDataFileException(e);
        }

        sortIntervals();
    }


    public TextBehaviorHandler(BehaviorHandlerI handler) {
        filepath = null;
        behavior = new HashMap<String, List<Interval>>();

        for (String label : handler.getLabelSet()) {
            List<Interval> intervals = handler.getContactIntervals(label);
            List<Interval> new_intervals = new ArrayList<Interval>();
            new_intervals.addAll(intervals);
            behavior.put(label, new_intervals);
        }
    }


    /** \brief Adds a time interval with a given label to animal behavior description
     * 
     * @param interval a 2 position double array where which must contain the time interval
     * @param label tag to be used in that time interval
     */
    private void addInterval(String label, Interval interval) {
        List<Interval> list = behavior.get(label);

        if (list == null) {
            list = new ArrayList<Interval>();
        }

        list.add(interval);
        behavior.put(label, list);
    }


    private void sortIntervals() {
        for (String label : behavior.keySet()) {
            List<Interval> list = behavior.get(label);

            Collections.sort(list, new Interval.ElementComparator(0));
            behavior.put(label, list);
        }
    }


    /** \brief Returns view of animal behavior description.
     * 
     * @return a String with animal behavior description.*/
    @Override
    public String toString() {
        return behavior.toString();
    }


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
    @Override
    public Interval getExpositionInterval() {
        Interval global = null;

        for (List<Interval> list : behavior.values()) {
            Interval interval = list.get(0);
            interval = interval.enclose(list.get(list.size()-1));

            if (global == null) {
                global = interval;
            }
            else {
                global = global.enclose(interval);
            }
        }

        return global;
    }


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
    @Override
    public List<Interval> getContactIntervals(String label) {
        return behavior.get(label);
    }


    @Override
    public void setContactIntervals(String label, List<Interval> intervals) {
        behavior.put(label, intervals);
    }


    @Override
    public String getLabel(double time) {
        for (Map.Entry<String,List<Interval>> entry : behavior.entrySet()) {
            String label = entry.getKey();

            for (Interval interval : entry.getValue())
                if (interval.contains(time))
                    return label;
        }

        return null;
    }


    @Override
    public Set<String> getLabelSet() {
        return behavior.keySet();
    }
}
