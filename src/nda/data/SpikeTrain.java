package nda.data;


/**
 * \brief Models the spike train information as a time series.
 * 
 * A set of spikes, a spike train s=[t1 t2 ... tN] is modeled as a 1D vector, in
 * which element xi is equal to ti from spike train s. Therefore, the models
 * associate a spike train to a 1D vector following:
 * 
 * \code s=[t1 t2 ... tN] <-> v=(x1,x2, ...,xN), where x1=t1, x2=t2, ... ,
 * xN=tN. \endcode
 * 
 * @author Nivaldo Vasconcelos
 * @date 17 Mai 2010
 */
public abstract class SpikeTrain {
    /** 1D vector to store the spike times */
    protected double[] times = null;

    /** Spike train name */
    protected String name = "";

    /** Total number of spikes in 'times' attribute */
    protected int numberOfSpikes = 0;

    /** value of first spike time */
    protected double first = 0;

    /** value of last spike time */
    protected double last = 0;
    

    @Override
    public String toString() {
        String str = name + ": [";
        
        for (double t : times) {
            if (!str.endsWith("[")) str += ", ";
            str += String.format("%.3f", t);
        }
        
        return str + "]";
    }
    
    /**
     * \brief Returns the time series of the spike train.
     * 
     * @return a 1D vector containing the time series representing the spike
     *         train.
     * */
    public double[] getTimes() {
        return times;
    }

    /**
     * \brief Returns the spike train name.
     * 
     * @return spike train name
     */
    public String getName() {
        return name;
    }

    /**
     * \brief Defines the spike train name.
     * 
     * @param name
     *            to be used by spike train.
     * */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * \brief Returns the first time in the spike train
     * 
     * @return the value of the first time in the spike train
     */
    public double getFirst() {
        return first;
    }

    /**
     * \brief Returns the last time in the spike train
     * 
     * @return the value of the last time in the spike train
     */
    public double getLast() {
        return last;
    }

    public double getSpike(int i) {
        return times[i];
    }
    
    /**
     * \brief Returns the time interval in which the spikes in this train occur.
     * 
     * @return a Interval representing the time interval containing all spikes.
     */
    public Interval getInterval() {
        return Interval.make(first, last);
    }
    
    /**
     * \brief Returns a spike train as a time series, into a given time
     * interval.
     * 
     * Given a neuron name, and a time interval I=[a;b], this method returns a
     * set of spike times whose belong to I, from that neuron as a time series.
     * This neuron name is under filter definition, therefore the given neuron
     * name is seek from a list under filter selection, if it is not found, this
     * method returns a \c null value as result.
     * 
     * @return the spike train. If the neuron name is not found under filter
     *         selection returns a \c null value.
     * 
     *         \sa setFilter(), getFilter()
     * */
    public abstract SpikeTrain extractInterval(Interval interval);
    
    
    public boolean isEmpty() {
        return times.length == 0;
    }

    /**
     * \brief Number of spikes in this train (number of elements in the time
     * series)
     * 
     * @return Returns the total number of spikes in 'times' attribute
     */
    public int getNumberOfSpikes() {
        return numberOfSpikes;
    }

    /**
     * \brief Calculate the interspike intervals (ISI) for this spike train
     * 
     * @return The ISI vector for this spike train
     */
    public double[] getInterspikeInterval() {
        if (times.length == 0) {
            throw new IllegalArgumentException("Spike train is empty.");
        }

        double[] isi = new double[times.length-1];
        
        for (int i = 1; i < times.length; ++i)
            isi[i-1] = times[i] - times[i-1];

        return isi;
    }
}
