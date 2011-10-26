package nda.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import nda.data.text.InvalidDataFileException;
import nda.data.text.MissingDataFileException;


/**
 * 
 * @author Giuliano Vilela.
 */
public class Histogram {
    private double binSize;
    private Interval interval;
    private int[] histogram;


    public Histogram(Histogram hist) {
        this(hist.getInterval(), hist.getBinSize());
    }


    public Histogram(Interval interval) {
        this(interval, 0.250);
    }


    public Histogram(Interval interval, int binCount) {
        double binSize = interval.duration() / binCount;
        init(interval, binCount, binSize);
    }

    /**
     * This constructor sets several properties to start make the histogram.
     * @param a : Begin Time, first spikeTime to search on interval.
     * @param b : End Time, last spikeTime to search on interval.
     * @param binSize : width of bin.
     * @throws IllegalArgumentException
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
     * \brief Set to zero all entries of the histogram buffer.
     * 
     * 
     * This method is very useful when the same instance is used as histogram
     * for more than one counting process. For example, the spike counting
     * process of a neuronal population in a same time interval. After each
     * counting process, for each neuron, this method can be called to allow the
     * reuse of the same instance of the histogram to the other counts.
     * 
     * The other parameters of the histograms (number of bins of the histogram,
     * starting time, ending time, etc.) are kept unchanged.
     * 
     * 
     */
    public void reset() {
        Arrays.fill(histogram, 0);
    }


    public void load(SpikeTrainI spikeTrain) {
        load(spikeTrain.getTimes());
    }


    /**
     * Calculates the histogram, considering that the spikes are sorted e stored
     * in a double array.
     * 
     * @param spike
     *            buffer with all spikeTimes to consider on histogram.
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


    /** Returns the number of entries in a given bin
     * 
     * @param bin bin in which should be informed the counting
     * @return the number of entries in the given bin
     * 
     * */
    public int getBinCount(int bin) {
        if (0 <= bin && bin < histogram.length)
            return histogram[bin];
        else
            throw new IllegalArgumentException("bin lies outside range");
    }


    /** Returns all entries
     * 
     * H[0] = number of entries in the bin 0
     * H[1] = number of entries in the bin 1
     * .
     * .
     * .
     * H[N-1] = number of entries in the bin N-1.
     * 
     * Where N is the size of the histogram.
     * 
     * @return a int vector with all entries in the histogram
     */
    public int[] getBinCounts() {
        return histogram;
    }


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
