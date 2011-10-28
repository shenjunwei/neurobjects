package nda.data;

import java.util.Comparator;
import java.util.List;


/**
 * Handle a set of spike trains loaded from a given data source.
 * 
 * This component should provide the interface between the spike trains data and the
 * application. The spike trains data is modeled as set of ascending time series, one
 * for each neuron, as seen on the documentation for the SpikeTrain class. Besides
 * returning the spike train information, this component provides basic managing
 * operations. For instance, filters to select neuron names patterns. With
 * these filters it is possible to easily access only the spikes from a specific
 * area/electrode.
 * 
 * This interface is implemented for each data source type in which the spike trains
 * data is found. Example: text files, relational databases, Matlab files, etc.
 * 
 * @author Nivaldo Vasconcelos
 * @see Wiki page: SpikeHandlerComponent.
 */
public interface SpikeHandlerI extends List<SpikeTrainI> {
    /**
     * Return the type of the data source.
     * 
     * Provides a succint string description that uniquely identifies the type of the
     * data source this SpikeHandlerI represents. Some examples include: "txt" for text
     * files, "sql" for a generic SQL relational database, "mat" for a .MAT Matlab file.
     * 
     * @return A string representing the type of the data source. Return null if the
     * type of the data source can't be determined.
     */
    public String getDataSourceType();


    /**
     * Return the animal name for this set of spikes.
     * 
     * The set of spike trains that this SpikeHandlerI holds should represent a single
     * test subject. This method should return a string identifying the animal.
     * 
     * @return The data source animal name, or a null string if there is no animal name
     * defined.
     */
    public String getAnimalName();


    /**
     * Return a SpikeTrain in this set, referenced by its name.
     * 
     * Given a neuron name, this method returns the set of spike times from that
     * neuron as a SpikeTrain.
     * 
     * @param name Name of the desired spike train.
     * @return The spike train with the corresponding name. If the neuron name is not
     * found under the current filter selection, return null.
     */
    public SpikeTrainI get(String name);


    /**
     * Return a SpikeTrain in this set, referenced by its position.
     * 
     * A SpikeHandlerI organizes the SpikeTrain's sorted by name, alphabetically,
     * from 0 to <tt>getNumberOfSpikeTrains()-1</tt>. This method returns the ith
     * SpikeTrain.
     * 
     * @param i Position of the desired spike train. Must be between 0 and
     * <tt>getNumberOfSpikeTrains()-1</tt>.
     * @return The ith SpikeTrain.
     */
    @Override
    public SpikeTrainI get(int i);


    /**
     * Create a new SpikeHandlerI containing a subset of the neurons in this
     * handler that match the given filter.
     * 
     * A spike train ST with name N is said to match filter F if N.startsWith(_F) for any
     * _F in F.split(",").
     * 
     * The "*" filter matches all neurons.
     */
    public SpikeHandlerI withFilter(String filter);


    /**
     * Get the current selection filter. See setFilter for an explanation of the
     * selection filter.
     * 
     * @return The current selection filter used by this SpikeHandlerI.
     */
    public String getFilter();


    /**
     * Get the list of neuron names currently loaded.
     * 
     * @return A list of spike train names currently loaded in this SpikeHandlerI.
     */
    public List<String> getNeuronNames();


    /**
     * Return a given time window of every spike train in this SpikeHandlerI.
     * 
     * Following the current filter selection, return a list of new spike trains
     * containing a time window views of the original spike trains.
     * 
     * This method only returns spike trains from which there is at least one activation
     * time in \c interval. No empty SpikeTrain's are returned. If no spike train in this
     * SpikeHandlerI has an activation time in \c interval, the empty List is returned.
     * 
     * @param interval Chosen time window
     * @return List of SpikeTrain's, each corresponding to a time window of an original
     * spike train contained in this SpikeHandlerI, according to the rules mentioned
     * above.
     * 
     * @see SpikeTrain.extractInterval
     */
    public SpikeHandlerI extractInterval(Interval interval);


    /**
     * Return the time interval for all the spike trains in this SpikeHandlerI.
     * 
     * Return an interval <tt>I = [a,b]</tt> where \c a is the smallest activation time
     * of all spike trains currently loaded, and \c b is the greatest.
     * 
     * @return Return the smallest Interval \c I such that, for every SpikeTrain \c st in
     * this handler, <tt>I.contains(st.getInterval()) == true</tt>.
     */
    public Interval getRecordingInterval();


    /**
     * Sort the spike trains in this handler according to the given comparator.
     */
    public void sort(Comparator<SpikeTrainI> comparator);
}
