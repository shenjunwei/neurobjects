package nda.data.text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nda.data.HypnogramI;
import nda.data.Interval;


/**
 * Hypnogram taken from a text file description.
 * 
 * @author Giuliano Vilela
 */
public class TextHypnogram implements HypnogramI {
    protected Interval globalInterval;
    protected Map<String,List<Interval>> stateIntervals;


    public TextHypnogram(String filepath, int binSize, Map<Integer,String> codeState)
    throws MissingDataFileException, InvalidDataFileException
    {
        globalInterval = Interval.make(0, 0);

        stateIntervals = new HashMap<String, List<Interval>>();
        for (String state : codeState.values())
            stateIntervals.put(state, new ArrayList<Interval>());

        loadHypnogram(filepath, binSize, codeState);
    }


    protected void loadHypnogram(
            String filepath, int binSize,
            Map<Integer,String> codeState)
    throws MissingDataFileException, InvalidDataFileException {
        try {
            FileReader fileReader = new FileReader(filepath);
            BufferedReader reader = new BufferedReader(fileReader);

            int lastCode = Integer.MAX_VALUE, code = Integer.MAX_VALUE;
            long currentStartTime = 0, currentTime = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                code = Integer.parseInt(line);

                if (lastCode == Integer.MAX_VALUE) {
                    lastCode = code;
                }
                else if (code != lastCode) {
                    Interval interval = Interval.make(currentStartTime, currentTime);
                    String state = codeState.get(lastCode);
                    addInterval(interval, state);
                    currentStartTime = currentTime;
                }

                lastCode = code;
                currentTime += binSize;
            }

            if (currentStartTime != currentTime) {
                Interval interval = Interval.make(currentStartTime, currentTime);
                String state = codeState.get(code);
                addInterval(interval, state);
            }

            reader.close();
        } catch (FileNotFoundException e) {
            throw new MissingDataFileException(e);
        } catch (NumberFormatException e) {
            throw new InvalidDataFileException(e);
        } catch (IOException e) {
            throw new InvalidDataFileException(e);
        }
    }


    protected void addInterval(Interval interval, String state) {
        List<Interval> intervals = stateIntervals.get(state);
        intervals.add(interval);
        stateIntervals.put(state, intervals);

        globalInterval = globalInterval.enclose(interval);
    }


    @Override
    public Interval getInterval() {
        return globalInterval;
    }


    @Override
    public Set<String> getStates() {
        return stateIntervals.keySet();
    }


    /*
     * TODO: keep another list with all the intervals sorted, to reduce the
     * complexity of this operation
     */
    @Override
    public String getStateFor(double time) {
        for (Map.Entry<String,List<Interval>> entry : stateIntervals.entrySet()) {
            for (Interval interval : entry.getValue()) {
                if (interval.contains(time))
                    return entry.getKey();
            }
        }

        return null;
    }

    @Override
    public List<Interval> getIntervalsFor(String state) {
        return stateIntervals.get(state);
    }
}
