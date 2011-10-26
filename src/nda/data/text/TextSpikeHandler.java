package nda.data.text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nda.data.Interval;
import nda.data.SpikeHandlerI;
import nda.data.SpikeTrainI;


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
    protected List<SpikeTrainI> neurons;

    protected final static String DEFAULT_FILTER = "";
    protected final static String TEXT_SPIKE_DATA_EXTENSION = ".spk";


    public TextSpikeHandler(TextSpikeHandler handler) {
        animalName = handler.animalName;
        spikeFilter = handler.spikeFilter;
        dataDir = handler.dataDir;
        spikeInterval = handler.spikeInterval;

        neurons = new ArrayList<SpikeTrainI>(handler.neurons.size());
        for (SpikeTrainI spikeTrain : handler.neurons)
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
    public SpikeTrainI getSpikeTrain(int i) {
        return neurons.get(i);
    }


    @Override
    public SpikeTrainI getSpikeTrain(String name) {
        for (SpikeTrainI spikeTrain : neurons)
            if (spikeTrain.getNeuronName().equalsIgnoreCase(name))
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

        List<SpikeTrainI> newNeurons = new ArrayList<SpikeTrainI>();

        for (SpikeTrainI spikeTrain : neurons) {
            String spikeName = spikeTrain.getNeuronName();

            if (filterMatch(spikeName, newFilter, false))
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

        for (SpikeTrainI spikeTrain : neurons)
            names.add(spikeTrain.getNeuronName());

        return names;
    }


    @Override
    public List<SpikeTrainI> getAllSpikeTrains() {
        return neurons;
    }


    @Override
    public List<SpikeTrainI> getAllSpikeTrains(Interval interval) {
        List<SpikeTrainI> trains = new ArrayList<SpikeTrainI>(neurons.size());

        for (SpikeTrainI spikeTrain : neurons) {
            SpikeTrainI intervalSt = spikeTrain.extractInterval(interval);
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

        for (SpikeTrainI spikeTrain : neurons) {
            if (!spikeTrain.isEmpty()) {
                first = Math.min(first, spikeTrain.getFirstSpike());
                last = Math.max(last, spikeTrain.getLastSpike());
            }
        }

        return new Interval(first, last);
    }


    public static int spikeTrainCount(String dataDir, String spikeFilter)
    throws InvalidDataDirectoryException {

        String[] files = listDirectory(dataDir);

        int count = 0;
        for (String filename : files)
            if (filterMatch(filename, spikeFilter, true))
                count++;

        return count;
    }


    protected void readSpikes(String dataDir, String spikeFilter, Interval spikeInterval)
    throws InvalidDataDirectoryException, InvalidDataFileException {

        String[] files = listDirectory(dataDir);
        Arrays.sort(files);

        neurons = new ArrayList<SpikeTrainI>();

        for (String filename : files) {
            if (!filterMatch(filename, spikeFilter, true))
                continue;

            String name = filename.substring(0, filename.lastIndexOf("."));
            String filepath = new File(dataDir, filename).getAbsolutePath();

            try {
                SpikeTrainI spikeTrain = new TextSpikeTrain(filepath, name, spikeInterval);
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


    protected final static boolean filterMatch(
            String name, String filter_str, boolean checkExtension) {
        name = name.toLowerCase();

        if (checkExtension && !name.endsWith(TEXT_SPIKE_DATA_EXTENSION))
            return false;

        String[] filters = filter_str.split(",");

        for (String filter : filters) {
            filter = filter.toLowerCase().trim();

            if (name.startsWith(filter) || filter.equals("*"))
                return true;
        }

        return false;
    }
}
