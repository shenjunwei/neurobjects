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
 * Implementation of BehaviorHandlerI that reads the animal behavior data
 * from a text file.
 */
public class TextBehaviorHandler implements BehaviorHandlerI {
    private String filepath;
    private Map<String, List<Interval>> behavior;


    /**
     * Create a TextBehaviorHandler that reads the information from the given
     * text file.
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


    /**
     * Copy constructor.
     */
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


    @Override
    public String toString() {
        return behavior.toString();
    }


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
