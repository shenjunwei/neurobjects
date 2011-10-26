package nda.data.text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;

import nda.data.Interval;
import nda.data.SpikeTrainI;
import nda.util.ArrayUtils;
import nda.util.FileUtils;


/**
 * A spike train read from a text data file.
 * 
 * Implementation of SpikeTrain used internally by TextSpikeHandler. Represents a
 * spike train that was read from a text data file.
 * 
 * @author Nivaldo Vasconcelos
 * @date 18 Mai 2010
 * @see Wiki page: SpikeDataFileFormat.
 */
public class TextSpikeTrain extends AbstractList<Double> implements SpikeTrainI {

    private String neuronName;
    private double[] spikeTimes;


    /**
     * Internal use only.
     */
    TextSpikeTrain(double[] times, String name) {
        neuronName = name;
        spikeTimes = times;
    }


    /**
     * Copy constructor.
     */
    public TextSpikeTrain(SpikeTrainI st) {
        neuronName = st.getNeuronName();
        spikeTimes = Arrays.copyOf(st.getTimes(), st.size());
    }


    /**
     * Construct a SpikeTrain from the times in a text data file.
     * 
     * This constructor receives the path of a text data file containing activation
     * times. It will read them to create the corresponding TextSpikeTrain.
     * 
     * @param filepath Full path to a text data file containing spike times
     * 
     * @throws MissingDataFileException If filepath doesn't exist
     * @throws InvalidDataFileException If filepath can't be read or isn't a valid spike
     * train text data file.
     */
    public TextSpikeTrain(String filepath)
    throws MissingDataFileException, InvalidDataFileException {

        this(filepath, filepath, Interval.INF);
    }


    /**
     * Construct a SpikeTrain from a time window of the activation times in a text data
     * file.
     * 
     * This constructor receives the path of a text data file containing activation
     * times. It will read a time window of these times, same as
     * SpikeTrain.extractInterval. The name of the TextSpikeTrain will be the name of the
     * file in \c filepath, minus the ".spk" extension.
     * 
     * @param filepath Full path to a text data file containing spike times
     * @param interval Desired time window to be read
     * 
     * @throws MissingDataFileException If filepath doesn't exist
     * @throws InvalidDataFileException If filepath can't be read or isn't a valid spike
     * train text data file.
     */
    public TextSpikeTrain(String filepath, Interval interval)
    throws MissingDataFileException, InvalidDataFileException {

        this(filepath, filepath, interval);
    }


    /**
     * Construct a SpikeTrain from the times in a text data file, with a custom name.
     * 
     * This constructor receives the path of a text data file containing activation
     * times. It will read these times and use a custom name to represent them.
     * 
     * @param filepath Full path to a text data file containing spike times
     * @param spikeName Name to be used by this TextSpikeTrain
     * 
     * @throws MissingDataFileException If filepath doesn't exist
     * @throws InvalidDataFileException If filepath can't be read or isn't a valid spike
     * train text data file.
     */
    public TextSpikeTrain(String filepath, String spikeName)
    throws MissingDataFileException, InvalidDataFileException {

        this(filepath, spikeName, Interval.INF);
    }


    /**
     * Construct a SpikeTrain from a time window of the activation times in a text data
     * file, with a custom name.
     * 
     * This constructor receives the path of a text data file containing activation
     * times. It will read a time window of these times, same as
     * SpikeTrain.extractInterval. The resulting TextSpikeTrain will have a custom name.
     * 
     * @param filepath Full path to a text data file containing spike times
     * @param name Name to be used by this TextSpikeTrain
     * @param interval Desired time window to be read
     * 
     * @throws MissingDataFileException If filepath doesn't exist
     * @throws InvalidDataFileException If filepath can't be read or isn't a valid spike
     * train text data file.
     */
    public TextSpikeTrain(String filepath, String name, Interval interval)
    throws MissingDataFileException, InvalidDataFileException {

        neuronName = FileUtils.parseFileName(name);
        readSpikes(filepath, interval);
    }


    @Override
    public String toString() {
        return neuronName;
    }


    @Override
    public SpikeTrainI extractInterval(Interval interval) {
        if (!interval.isValid())
            throw new IllegalArgumentException("Interval is invalid");

        interval = interval.intersection(getInterval());

        double[] times = ArrayUtils.extractInterval(
                spikeTimes, interval.start(), interval.end());

        return new TextSpikeTrain(times, neuronName + "@" + interval);
    }


    /**
     * Load the spike train data from a text file into this TextSpikeTrain.
     * 
     * Read the text file and load only those spikes that occur in the given interval.
     * 
     * @param filepath Path to text  file
     * @param interval Time interval to be considered
     * @throws MissingDataFileException If filepath doesn't exist
     * @throws InvalidDataFileException If filepath isn't a valid spike train data file
     */
    protected void readSpikes(String filepath, Interval interval)
    throws MissingDataFileException, InvalidDataFileException {

        if (!interval.isValid())
            throw new IllegalArgumentException("Interval is invalid");

        ArrayList<Double> timesList = new ArrayList<Double>(1000);

        try {
            BufferedReader in = new BufferedReader(new FileReader(filepath));
            String str;
            double spikeTime = 0;

            while (((str = in.readLine()) != null) && (spikeTime <= interval.end())) {
                if (str.trim().isEmpty())
                    continue;

                spikeTime = Double.parseDouble(str);

                if (interval.contains(spikeTime))
                    timesList.add(spikeTime);
            }

            in.close();
        }
        catch (FileNotFoundException e) {
            throw new MissingDataFileException(e);
        }
        catch (NumberFormatException e) {
            throw new InvalidDataFileException(e);
        }
        catch (IOException e) {
            throw new InvalidDataFileException(e);
        }

        if (!ArrayUtils.isSorted(timesList)) {
            throw new InvalidDataFileException(
                    filepath + ": times aren't in ascending order.");
        }

        spikeTimes = new double[timesList.size()];
        for (int i = 0; i < spikeTimes.length; ++i)
            spikeTimes[i] = timesList.get(i);
    }


    /**
     * @see nda.data.SpikeTrainI#getTimes()
     */
    @Override
    public double[] getTimes() {
        return spikeTimes;
    }


    /**
     * @see nda.data.SpikeTrainI#getNeuronName()
     */
    @Override
    public String getNeuronName() {
        return neuronName;
    }


    /**
     * @see nda.data.SpikeTrainI#getNeuronArea()
     */
    @Override
    public String getNeuronArea() {
        int pos = neuronName.indexOf('_');
        if (pos != -1)
            return neuronName.substring(0, pos);
        else
            return null;
    }


    /**
     * @see nda.data.SpikeTrainI#getFirstSpike()
     */
    @Override
    public double getFirstSpike() {
        return spikeTimes[0];
    }


    /**
     * @see nda.data.SpikeTrainI#getLastSpike()
     */
    @Override
    public double getLastSpike() {
        return spikeTimes[spikeTimes.length-1];
    }


    /**
     * @see nda.data.SpikeTrainI#getInterval()
     */
    @Override
    public Interval getInterval() {
        return Interval.make(getFirstSpike(), getLastSpike());
    }


    /**
     * @see java.util.AbstractList#get(int)
     */
    @Override
    public Double get(int index) {
        return spikeTimes[index];
    }


    /**
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return spikeTimes.length;
    }
}
