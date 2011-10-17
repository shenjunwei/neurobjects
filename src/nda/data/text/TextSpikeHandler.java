package nda.data.text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nda.data.Interval;
import nda.data.SpikeHandlerI;
import nda.data.SpikeTrain;


/**
 * Implementation of the SpikeHandlerI component that uses text files as a data source.
 * 
 * @author Nivaldo Vasconcelos
 */
public class TextSpikeHandler implements SpikeHandlerI {
    protected String animalName;
    protected String spikeFilter;
    protected String dataDir;
    protected Interval spikeInterval;
    protected List<SpikeTrain> neurons;

    protected final static String DEFAULT_FILTER = "";


    public TextSpikeHandler(TextSpikeHandler handler) {
        animalName = handler.animalName;
        spikeFilter = handler.spikeFilter;
        dataDir = handler.dataDir;
        spikeInterval = handler.spikeInterval;

        neurons = new ArrayList<SpikeTrain>(handler.neurons.size());
        for (SpikeTrain spikeTrain : handler.neurons)
            neurons.add(new TextSpikeTrain(spikeTrain));
    }

    public TextSpikeHandler(String dir)
    throws InvalidDataFileException, InvalidDataDirectoryException {
        this(dir, DEFAULT_FILTER);
    }


    public TextSpikeHandler(String dir, String filter)
    throws InvalidDataFileException, InvalidDataDirectoryException {
        this(dir, filter, Interval.INF);
    }


    public TextSpikeHandler(String dir, String filter, Interval itv)
    throws InvalidDataFileException, InvalidDataDirectoryException {
        dataDir = dir;
        spikeInterval = itv;
        animalName = "<unknown>";
        spikeFilter = filter.toLowerCase();
        readSpikes(dataDir, filter, spikeInterval);
    }


    private TextSpikeHandler() { }


    @Override
    public String toString() {
        return String.format("%s: %s", animalName, neurons);
    }


    @Override
    public String getSourceType() {
        return "txt";
    }


    @Override
    public String getAnimalName() {
        return animalName;
    }


    @Override
    public SpikeTrain getSpikeTrain(int i) {
        return neurons.get(i);
    }


    @Override
    public SpikeTrain getSpikeTrain(String name) {
        for (SpikeTrain spikeTrain : neurons)
            if (spikeTrain.getName().equalsIgnoreCase(name))
                return spikeTrain;

        return null;
    }


    @Override
    public int getNumberOfSpikeTrains() {
        return neurons.size();
    }


    @Override
    public SpikeHandlerI withFilter(String filter) {
        String newFilter = filter.toLowerCase();

        if (newFilter.equals(spikeFilter))
            return this;

        List<SpikeTrain> newNeurons = new ArrayList<SpikeTrain>();

        for (SpikeTrain spikeTrain : neurons) {
            String spikeName = spikeTrain.getName();

            if (filterMatch(spikeName, newFilter))
                newNeurons.add(spikeTrain);
        }

        TextSpikeHandler newHandler = new TextSpikeHandler();
        newHandler.animalName = animalName;
        newHandler.spikeFilter = newFilter;
        newHandler.dataDir = dataDir;
        newHandler.spikeInterval = spikeInterval;
        newHandler.neurons = newNeurons;

        return newHandler;
    }


    @Override
    public String getFilter() {
        return spikeFilter;
    }


    @Override
    public List<String> getNeuronNames() {
        List<String> names = new ArrayList<String>(neurons.size());

        for (SpikeTrain spikeTrain : neurons)
            names.add(spikeTrain.getName());

        return names;
    }


    @Override
    public List<SpikeTrain> getAllSpikeTrains() {
        return neurons;
    }


    @Override
    public List<SpikeTrain> getAllSpikeTrains(Interval interval) {
        List<SpikeTrain> trains = new ArrayList<SpikeTrain>(neurons.size());

        for (SpikeTrain spikeTrain : neurons) {
            SpikeTrain intervalSt = spikeTrain.extractInterval(interval);
            if (!intervalSt.isEmpty())
                trains.add(intervalSt);
        }

        return trains;
    }


    @Override
    public Interval getGlobalSpikeInterval() {
        if (neurons.size() == 0)
            return Interval.EMPTY;

        double first = Double.POSITIVE_INFINITY;
        double last = Double.NEGATIVE_INFINITY;

        for (SpikeTrain spikeTrain : neurons) {
            if (!spikeTrain.isEmpty()) {
                first = Math.min(first, spikeTrain.getFirst());
                last = Math.max(last, spikeTrain.getLast());
            }
        }

        return new Interval(first, last);
    }


    public static int spikeTrainCount(String dataDir, String spikeFilter)
    throws InvalidDataDirectoryException {

        String[] files = listDirectory(dataDir);

        int count = 0;
        for (String filename : files)
            if (filterMatch(filename, spikeFilter))
                count++;

        return count;
    }


    protected void readSpikes(String dataDir, String spikeFilter, Interval spikeInterval)
    throws InvalidDataDirectoryException, InvalidDataFileException {

        String[] files = listDirectory(dataDir);
        Arrays.sort(files);

        neurons = new ArrayList<SpikeTrain>();

        for (String filename : files) {
            if (!filterMatch(filename, spikeFilter))
                continue;

            String name = filename.substring(0, filename.lastIndexOf("."));
            String filepath = new File(dataDir, filename).getAbsolutePath();

            try {
                SpikeTrain spikeTrain = new TextSpikeTrain(filepath, name, spikeInterval);
                neurons.add(spikeTrain);
            } catch (MissingDataFileException e) {
                /*
                 * We can guarantee that filepath exists because
                 * we got it from listDirectory
                 */
            }
        }
    }


    protected static String[] listDirectory(String dirpath)
    throws InvalidDataDirectoryException {

        File dir = new File(dirpath);
        String[] files = dir.list();

        if (files == null)
            throw new InvalidDataDirectoryException(dirpath);

        return files;
    }


    protected final static boolean filterMatch(String name, String filter_str) {
        name = name.toLowerCase();
        String[] filters = filter_str.split(",");

        for (String filter : filters) {
            filter = filter.toLowerCase().trim();

            if (name.startsWith(filter) || filter.equals("*"))
                return true;
        }

        return false;
    }
}
