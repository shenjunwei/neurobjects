package nda.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import nda.data.text.InvalidDataFileException;
import nda.data.text.MissingDataFileException;


/**
 * Component to calculate the probability distribution for a random 1D variable
 * using a Histogram.
 * 
 * A histogram is tied to a given interval and a bin size. Values outside the interval
 * are ignored. Internally, the histogram will use time windows with a binSize duration
 * to estimate the function.
 * 
 * The bins are organized as follows:
 * 
 * \begincode
 * H[0] = number of entries in the bin 0, t0 <= x < t1
 * H[1] = number of entries in the bin 1, t1 <= x < t2
 * .
 * .
 * .
 * H[N-1] = number of entries in the bin N-1, x <= tN
 *\endcode
 * 
 * @author Giuliano Vilela.
 */
public class Histogram {
    private double binSize;
    private Interval interval;
    private int[] histogram;


    /**
     * Copy constructor.
     */
    public Histogram(Histogram hist) {
        this(hist.getInterval(), hist.getBinSize());
    }


    /**
     * Create a Histogram with the specied interval and a bin size of 250ms.
     */
    public Histogram(Interval interval) {
        this(interval, 0.250);
    }


    /**
     * Create a Histogram with the specified interval and a bin size of
     * <tt>interval.duration() / binCount</tt>.
     */
    public Histogram(Interval interval, int binCount) {
        double binSize = interval.duration() / binCount;
        init(interval, binCount, binSize);
    }


    /**
     * Create a Histogram with the specified interval and binSize.
     */
    public Histogram (Interval interval, double binSize) {
        int binCount = (int) Math.ceil(interval.duration() / binSize);
        init(interval, binCount, binSize);
    }


    protected void init(Interval _interval, int binCount, double _binSize) {
        interval = _interval;
        binSize = _binSize;
        histogram = new int[binCount];
    }


    @Override
    public String toString() {
        String str ="Histogram {" +
        " interval:" + interval +
        " binSize:" + String.format("%.03f", binSize) +
        " counts:" + Arrays.toString(getBinCounts()) +
        " }";

        return str;
    }


    /**
     * Set to zero all entries of the histogram buffer.
     * 
     * This method is very useful when the same instance is used as histogram
     * for more than one counting process. For example, the spike counting
     * process of a neuronal population in a same time interval. After each
     * counting process, for each neuron, this method can be called to allow the
     * reuse of the same instance of the histogram to the other counts.
     * 
     * The other parameters of the histograms (number of bins of the histogram,
     * starting time, ending time, etc.) are kept unchanged.
     */
    public void reset() {
        Arrays.fill(histogram, 0);
    }


    /**
     * Process all activation times in the given SpikeTrainI.
     */
    public void load(SpikeTrainI spikeTrain) {
        load(spikeTrain.getTimes());
    }


    /**
     * Calculates the histogram, considering that the spikes are sorted and stored
     * in a double array.
     */
    public void load(double[] sample) {
        reset();
        double start = interval.start();

        int i = Arrays.binarySearch(sample, start);
        if (i < 0) i = -i - 1;

        while (i < sample.length && sample[i] <= interval.end())
            addFast(sample[i++]);
    }


    public void load(String filepath)
    throws MissingDataFileException, InvalidDataFileException {
        reset();
        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(filepath));
        } catch (IOException e) {
            throw new MissingDataFileException();
        }

        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isEmpty())
                    continue;

                double value = Double.parseDouble(line);

                if (value >= interval.start()) {
                    if (value <= interval.end())
                        addFast(value);
                    else
                        break;
                }
            }
        } catch (IOException e) {
            throw new InvalidDataFileException(e);
        } catch (NumberFormatException e) {
            throw new InvalidDataFileException(e);
        }
    }


    public void add(double value) {
        if (interval.contains(value)) addFast(value);
    }


    protected void addFast(double value) {
        if (value != interval.end()) {
            double offs = value - interval.start();

            int bin = (int) Math.floor(offs / binSize);
            ++histogram[bin];
        }
        else {
            ++histogram[histogram.length-1];
        }
    }


    /**
     * Returns the number of entries in a given bin
     * 
     * @param bin bin in which should be informed the counting
     * @return the number of entries in the given bin
     */
    public int getBinCount(int bin) {
        if (0 <= bin && bin < histogram.length)
            return histogram[bin];
        else
            throw new IllegalArgumentException("bin lies outside range");
    }


    /**
     * Returns all histogram entries
     */
    public int[] getBinCounts() {
        return histogram;
    }


    /**
     * @return The histogram bin in which a given value should be added
     */
    public int getBinFor(double value) {
        if (!interval.contains(value)) {
            throw new IllegalArgumentException(
            "value lies outside the histogram interval");
        }

        if (value != interval.end()) {
            double offs = value - interval.start();
            return (int) Math.floor(offs / binSize);
        }
        else {
            return getNumberBins()-1;
        }
    }


    /**
     * @return The time instant represented by this bin
     */
    public double getTimeForBin(int bin) {
        return interval.start() + bin*binSize;
    }


    public void saveBinCounts(int[] counts) {
        assert counts.length == getNumberBins();

        for (int i = 0; i < histogram.length; ++i)
            counts[i] = histogram[i];
    }


    public Interval getInterval() {
        return interval;
    }


    /** Tells the size of bin used to build the histogram
     * 
     * @return the value of the used size of bin used into the histogram.*/
    public double getBinSize() {
        return binSize;
    }


    /** Tells the size of the histogram
     * 
     * Tells the number of bins uses into the histogram.
     * @return the size of the histogram
     */
    public int getNumberBins() {
        return histogram.length;
    }
}
