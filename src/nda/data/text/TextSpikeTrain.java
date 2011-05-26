package nda.data.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nda.data.Interval;
import nda.data.SpikeTrain;


/**
 * \page TextSpikeTrainTests Tests on TextSpikeTrain
 *
 *  The following tests were performed (in tests.utils_TxtSpikeTrainTests): \n
 *   1- Testing the constructor in normal situation:
 *      new TextSpikeTrain(filePath);  \n
 *   2- Testing the constructor in normal situation:
 *      new TextSpikeTrain(filePath, "neuron_S1_02a"); \n
 *   3- Testing the constructor in normal situation:
 *      new TextSpikeTrain(filePath, "neuron_S1_02a", 0, 100); \n
 *   4- Testing the constructor in normal situation with a negative firsTime parameter:
 *      new TextSpikeTrain(filePath, "neuron_S1_02a", -10, 100.00000000000000000009); \n
 *   5- Testing in an erroneous situation with firstTime > lastTime:
 *      new TextSpikeTrain(filePath, "neuron_S1_02a", 100.00000000000000000009, 0); \n
 *   6- Testing in normal situation with equals parameters 'firstTime' and 'LastTime':
 *      new TextSpikeTrain(filePath, "neuron_S1_02a", 0, 0); \n
 *   7- Testing in an abnormal situation with a sourceFile that isn't a neuronFile
 *      but an Gif binary image file:
 *      new TextSpikeTrain("/tmp/talesmileto01.gif", "neuron_S1_02a", 0, 500); \n
 *   8- Testing in an abnormal situation with a sourceFile that don't exist:
 *      new TextSpikeTrain("/tmp/huiahsuihsi", "neuron_S1_02a", 0, 200); \n
 *   9- Testing in an abnormal situation with a source file that comes from /dev/random:
 *      new TextSpikeTrain("/dev/random", "neuron_S1_02a", 0, 100); \n
 *   10- Testing in an abnormal situation which the 'firstime' and 'lastTime' parameter
 *      is much larger than the existing in source file:
 *      new TextSpikeTrain(filePath, "neuron_S1_02a", Double.NEGATIVE_INFINITY,
 *           Double.POSITIVE_INFINITY); \n
 *    \n
 *   In all performed tests were made the follow commands: \n
 *    TextSpikeTrain spkTrain_1 = new TextSpikeTrain(filePath); \n
 *    DoubleMatrix1D spikes_1 = spkTrain_1.getTimes(); \n
 *    System.out.println("spikes size: "+spkTrain_1.getNumberOfSpikes()); \n \n
 */

/**
 * \brief Models the spike train information as a time series read from a text
 * file.
 * 
 * This implementation considers that the set of spike, a spike train, s=[t1 t2
 * ... tN], is placed one spike time per line.
 * 
 * @author Nivaldo Vasconcelos
 * @date 18 Mai 2010
 * 
 *       \TODO Verify if the spike file have a correct format (one double number
 *       per line), if not: throws an specific exception.
 */
public class TextSpikeTrain extends SpikeTrain {
    protected int numberOfSpikes = 0;

    /**
     * \brief Constructor of a Spike Train given a 1D vector and a name.
     * 
     * This constructor receive a 1D vector in which should be the spike time in
     * a crescent order, internally a copy of this 1D vector is built. Moreover
     * receives the name of spike train. Normally it is the name of neuron.
     * 
     * @param time_v
     *            1D vector with spike times to be stored;
     * @param name
     *            String with name of spike train name.
     * @throws InvalidDataFileException
     * */
    public TextSpikeTrain(double[] time_v, String name) {
        times = time_v;
        numberOfSpikes = time_v.length;
        setInitialValues(name);
    }

    /**
     * \brief Constructor of a Spike Train given a filename in which there is
     * the spike train.
     * 
     * This constructor receives a filename (with full path) in which there is a
     * spike train, one time per row.
     * 
     * @param filepath
     *            full path and file name in which are stored the spike times
     * @throws InvalidDataFileException
     * 
     * */
    public TextSpikeTrain(String filepath)
            throws MissingDataFileException, InvalidDataFileException {
        
        this(filepath, filepath, Interval.INF);
    }

    /**
     * \brief Constructor of a Spike Train given a filename in which there is
     * the spike train and a time interval.
     * 
     * This constructor receive a filename (with full path) in which there is a
     * spike train, one time per row and a time interval I=[a;b] and build spike
     * that stores those spike times into I interval.
     * 
     * @param filepath
     *            full path and file name in which are stored the spike times
     * @param interval
     *            time interval;
     * @throws InvertedParameterException
     * @throws IOException
     * @throws FileNotFoundException
     * 
     * 
     * */
    public TextSpikeTrain(String filepath, Interval interval)
            throws MissingDataFileException, InvalidDataFileException {
        
        this(filepath, filepath, interval);
    }

    /**
     * \brief Constructor of a Spike Train given a filename in which there is
     * the spike train and the name of spike train.
     * 
     * This constructor receive a filename (with full path) in which should be
     * there is a spike train, one time per row.
     * 
     * @param filepath
     *            full path and file name in which are stored the spike times
     * @param spikeName
     *            name to be used by the spike train
     * @throws InvalidDataFileException
     * @throws MissingDataFileException
     * 
     * */
    public TextSpikeTrain(String filepath, String spikeName)
            throws MissingDataFileException, InvalidDataFileException {
        
        this(filepath, spikeName, Interval.INF);
    }

    /**
     * \brief Constructor of a named Spike Train given a filename in which there
     * is the spike train and a time interval.
     * 
     * This constructor receive a filename (with full path) in which should be
     * there is a spike train, one time per row and a time interval I=[a;b] and
     * build spike that stores those spike times into I interval.
     * 
     * @param filepath
     *            full path and file name in which are stored the spike times;
     * @param spikeName
     *            name to be used by spike train;
     * @param interval
     *            time interval;
     * @throws InvertedParameterException
     * @throws IOException
     * @throws FileNotFoundException
     * 
     * 
     * */
    public TextSpikeTrain(String filepath, String spikeName, Interval interval)
            throws MissingDataFileException, InvalidDataFileException {
        
        fillFromFile(filepath, interval);
        setInitialValues(spikeName);
    }

    protected void fillFromFile(String filename, Interval interval)
            throws MissingDataFileException, InvalidDataFileException {
        
        ArrayList<Double> timesList = new ArrayList<Double>(500);
        
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
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
        
        if (!isSorted(timesList)) {
            throw new InvalidDataFileException(
                    filename + ": times aren't in ascending order.");
        }
        
        times = new double[timesList.size()];
        for (int i = 0; i < times.length; ++i)
            times[i] = timesList.get(i);
        
        numberOfSpikes = times.length;
        if (numberOfSpikes == 0) {
            System.out.println(
                    "TextSpikeTrain::fillFromFile: " + filename +
                    " has no spikes in " + interval
            );
        }
    }

    /**
     * \brief Set initial values like: if the spike trains is valid, first time
     * and last time, neuron name
     */
    protected void setInitialValues(String spikeStr) {
        name = parseFileName(spikeStr);
        first = times[0];
        last = times[times.length-1];
    }

    @Override
    public int getNumberOfSpikes() {
        return numberOfSpikes;
    }
    
    @Override
    public SpikeTrain extractInterval(Interval interval) {
        int i = Arrays.binarySearch(times, interval.start());
        int j = Arrays.binarySearch(times, i, times.length, interval.end());
        
        // see binarySearch docs
        if (i < 0) i = -i - 1;
        if (j < 0) j = -j - 1;
        
        double[] new_times = Arrays.copyOfRange(times, i, j);
        return new TextSpikeTrain(new_times, name + '@' + interval);
    }
    
    protected static String parseFileName(String filepath) {
        int dotPos = filepath.lastIndexOf('.');
        int pathPos = filepath.lastIndexOf(File.separatorChar);
        
        if (dotPos < 0 || pathPos < 0)
            return filepath;

        String newName = filepath.substring(pathPos+1, dotPos);
        return newName.toLowerCase();
    }
    
    protected static boolean isSorted(List<Double> list) {
        for (int i = 1; i < list.size(); ++i)
            if (list.get(i).compareTo(list.get(i-1)) < 0)
                return false;
        
        return true;
    }
    
    /*
     * Needed in TextSpikeTrainTest to test protected methods.
     */
    protected TextSpikeTrain() { }
}
