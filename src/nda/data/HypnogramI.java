package nda.data;

import java.util.List;
import java.util.Set;


/**
 * Hypnogram data source.
 * 
 * @author Giuliano Vilela
 */
public interface HypnogramI {
    public Interval getInterval();
    public Set<String> getStates();

    public String getStateFor(double time);
    public List<Interval> getIntervalsFor(String state);
}
