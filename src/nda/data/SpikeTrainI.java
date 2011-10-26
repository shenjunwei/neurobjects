package nda.data;

import java.util.List;


/**
 * Temporal sequence of action potentials reported by a measurement of a neuron.
 * 
 * A set of spikes, a spike train @f$S = (t_0, t_1, ..., t_n) @f$ is modeled as a 1D
 * lsit of double floating point precision. Time is assumed to be measured in seconds.
 * The series is guaranteed to be ascending.
 */
public interface SpikeTrainI extends List<Double> {
    /**
     * Return the time series of this spike train.
     * 
     * Return an array where each value is a time measurement, usually in seconds,
     * representing the time of an activation of a single neuron.
     * 
     * @return An array containing the time series representing this spike train.
     */
    public double[] getTimes();


    /**
     * Return the spike train name.
     * 
     * The spike train name is a unique identifier for this spike train, provided
     * by the SpikeHandlerI containing this SpikeTrain. Usually, it's the name of the
     * neuron for this spike train.
     * 
     * @return The spike train name.
     */
    public String getNeuronName();


    public String getNeuronArea();


    /**
     * Get the first time in the spike train.
     * 
     * @return The value of the first time in the spike train, or \c Double.NaN if
     * this spike train is invalid (i.e., it's empty).
     */
    public double getFirstSpike();


    /**
     * Get the last time in the spike train.
     * 
     * @return The value of the last time in the spike train, or \c Double.NaN if
     * this spike train is invalid (i.e., it's empty).
     */
    public double getLastSpike();


    /**
     * Returns the time interval in which the spikes in this train occur.
     * 
     * @return An Interval representing the time interval containing all spikes in this
     * train: <tt>[getFirst(), getLast()]</tt>. If this spike train is empty, the
     * interval also will be empty.
     */
    public Interval getInterval();


    /**
     * Extract a particular time window of this spike train.
     * 
     * Given a time Interval <tt>I = [a,b]</tt>, this method returns a new SpikeTrain
     * containing all activation times of this neuron that occurred in the interval I.
     * Note that the intersection of \c I and \c getInterval() may be empty, generating
     * an empty SpikeTrain.
     * 
     * @param interval The time window to be extracted.
     * @return A new SpikeTrain representing a time window of this spike train.
     */
    public SpikeTrainI extractInterval(Interval interval);
}
