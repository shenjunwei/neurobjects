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
    double[] times = null;

    /** Spike train name */
    String name = "";

    /** Total number of spikes in 'times' attribute */
    int numberOfSpikes = 0;

    /** value of first spike time */
    double first = 0;

    /** value of last spike time */
    double last = 0;

    /** is this spike train valid ? */
    boolean valid = false;

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

    /**
     * \brief Informs if the spike train content is valid.
     * 
     * @return \c TRUE if the spike is valid \b or \n \c FALSE otherwise
     */
    public boolean isValid() {
        return valid;
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
