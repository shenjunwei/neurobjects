package nda.data;

import java.util.Arrays;


/**
 * Temporal sequence of action potentials reported by a measurement of a neuron.
 * 
 * A set of spikes, a spike train @f$S = (t_0, t_1, ..., t_n) @f$ is modeled as a 1D
 * vector of double floating point precision. Time is assumed to be measured in seconds.
 * The series is guaranteed to be ascending.
 * 
 * This is an abstract class that should be extended for each type of data source. The
 * method implementations in this class can be considered a reference implementation of
 * SpikeTrain based on a <tt>double[]</tt> array. Other data sources are free to
 * represent the time series in another way.
 * 
 * @author Nivaldo Vasconcelos
 * @date 17 Mai 2010
 */
public abstract class SpikeTrain {
    /**
     * 1D vector to store the spike times.
     * 
     * For every @f$i \in [0,n] @f$, <tt>spikeTimes[i] =</tt> @f$ t_i. @f$
     */
    protected double[] spikeTimes = null;

    /** Name of the neuron from which this measurement came from. */
    protected String neuronName = null;


    /**
     * String representation of this SpikeTrain.
     * 
     * The string is formated as <tt>"name: [t0,...,tn]"</tt>, where name is the
     * neuron name, as returned by getName(), and <tt>t0,...,tn</tt> are the activation
     * times for this neuron.
     */
    @Override
    public String toString() {
        return String.format("%s: %s", neuronName, Arrays.toString(spikeTimes));
    }


    /**
     * Return the time series of this spike train.
     * 
     * Return an array where each value is a time measurement, usually in seconds,
     * representing the time of an activation of a single neuron.
     * 
     * @return An array containing the time series representing this spike train.
     */
    public double[] getTimes() {
        return spikeTimes;
    }


    /**
     * Return the spike train name.
     * 
     * The spike train name is a unique identifier for this spike train, provided
     * by the SpikeHandlerI containing this SpikeTrain. Usually, it's the name of the
     * neuron for this spike train.
     * 
     * @return The spike train name.
     */
    public String getName() {
        return neuronName;
    }


    /**
     * Set the spike train name.
     * 
     * @param name New name for this spike train.
     */
    public void setName(String name) {
        this.neuronName = name;
    }


    /**
     * Get the first time in the spike train.
     * 
     * @return The value of the first time in the spike train, or \c Double.NaN if
     * this spike train is invalid (i.e., it's empty).
     */
    public double getFirst() {
        if (spikeTimes.length != 0)
            return spikeTimes[0];
        else
            return Double.NaN;
    }


    /**
     * Get the last time in the spike train.
     * 
     * @return The value of the last time in the spike train, or \c Double.NaN if
     * this spike train is invalid (i.e., it's empty).
     */
    public double getLast() {
        if (spikeTimes.length != 0)
            return spikeTimes[spikeTimes.length-1];
        else
            return Double.NaN;
    }


    /**
     * Get the time of a single activation of the neuron.
     * 
     * @param i Index of the activation time. Must be between \c 0 and
     * <tt>getNumberOfSpikes()-1</tt>, inclusive.
     * @return Return the ith activation time.
     */
    public double getSpike(int i) {
        return spikeTimes[i];
    }


    /**
     * Returns the time interval in which the spikes in this train occur.
     * 
     * @return An Interval representing the time interval containing all spikes in this
     * train: <tt>[getFirst(), getLast()]</tt>. If this spike train is empty, the
     * interval also will be empty.
     */
    public Interval getInterval() {
        return new Interval(getFirst(), getLast());
    }


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
    public abstract SpikeTrain extractInterval(Interval interval);


    /**
     * Determine if this spike train is empty.
     * 
     * @return True if this spike train contains no activation times, false otherwise.
     */
    public boolean isEmpty() {
        return spikeTimes.length == 0;
    }


    /**
     * Number of spikes in this train (number of elements in the time series)
     * 
     * @return Returns the total number of spikes in 'spikeTimes' attribute
     */
    public int getNumberOfSpikes() {
        return spikeTimes.length;
    }


    /**
     * Calculate the interspike intervals (ISI) for this spike train.
     * 
     * The interspike intervals for a given spike train is defined as a 1D vector
     * of length <tt>getNumberOfSpikes()-1</tt> where @f$ v_i = t_{i+1} - t_i @f$.
     * 
     * @return The ISI vector for this spike train.
     */
    public double[] getInterspikeInterval() {
        if (spikeTimes.length == 0)
            throw new IllegalArgumentException("Spike train is empty.");

        double[] isi = new double[spikeTimes.length-1];

        for (int i = 1; i < spikeTimes.length; ++i)
            isi[i-1] = spikeTimes[i] - spikeTimes[i-1];

        return isi;
    }
}
