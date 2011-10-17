package nda.data.text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import nda.data.Interval;
import nda.data.SpikeTrain;
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
class TextSpikeTrain extends SpikeTrain {

    /**
     * Copy constructor.
     *
     * @param st SpikeTrain
     */
    public TextSpikeTrain(SpikeTrain st) {
        spikeTimes = st.getTimes().clone();
        setInitialValues(st.getName());
    }


    /**
     * Construct a spike train from a 1D vector of activation times.
     * 
     * This constructor is only used internally by TextSpikeTrain. The user can only
     * get a TextSpikeTrain by using the higher level methods that read a text file. It
     * receives a 1D vector containing the spike times in ascending order and a string
     * containing the name of this spike train.
     * 
     * @param times Vector with spike times to be stored. Note that this TextSpikeTrain
     * will actually hold the reference to time_v.
     * @param name String with the name of the spike train.
     */
    protected TextSpikeTrain(double[] times, String name) {
        spikeTimes = times;
        setInitialValues(name);
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
     * @param spikeName Name to be used by this TextSpikeTrain
     * @param interval Desired time window to be read
     * 
     * @throws MissingDataFileException If filepath doesn't exist
     * @throws InvalidDataFileException If filepath can't be read or isn't a valid spike
     * train text data file.
     */
    public TextSpikeTrain(String filepath, String spikeName, Interval interval)
    throws MissingDataFileException, InvalidDataFileException {

        fillFromFile(filepath, interval);
        setInitialValues(spikeName);
    }


    @Override
    public SpikeTrain extractInterval(Interval interval) {
        double[] new_times = ArrayUtils.extractInterval(
                spikeTimes, interval.start(), interval.end());

        return new TextSpikeTrain(new_times, neuronName + "@" + interval);
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
    protected void fillFromFile(String filepath, Interval interval)
    throws MissingDataFileException, InvalidDataFileException {

        ArrayList<Double> timesList = new ArrayList<Double>(500);

        try {
            BufferedReader in = new BufferedReader(new FileReader(filepath));
            String str;
            double spikeTime = 0;

            while (((str = in.readLine()) != null) && (spikeTime < interval.end())) {
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


    protected void setInitialValues(String spikeStr) {
        neuronName = FileUtils.parseFileName(spikeStr);
    }
}
