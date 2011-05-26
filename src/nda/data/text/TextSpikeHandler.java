package nda.data.text;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import nda.data.Interval;
import nda.data.SpikeTrain;
import nda.data.SpikeHandlerI;


/**
 * 
 * @author giulianoxt
 */
public class TextSpikeHandler implements SpikeHandlerI {
    protected String animalName;
    protected String spikeFilter;
    protected String dataDir;
    protected Interval spikeInterval;

    protected final static String DEFAULT_FILTER = "";
    
    /** The file extension of spike files where some methods will look for spikes */
    protected final static String SPIKE_FILE_EXT = ".spk";

    protected List<SpikeTrain> neurons;

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
        setFilter(filter);
    }
    
    @Override
    public String toString() {
        String str = "{ ";
        
        for (SpikeTrain spikeTrain : neurons) {
            if (str.length() > 2) str += ", ";
            str += spikeTrain;
        }
        
        return str + " }";
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
    public void setFilter(String filter)
            throws InvalidDataFileException, InvalidDataDirectoryException {
        
        spikeFilter = filter.toLowerCase();
        readSpikes(dataDir, spikeFilter, spikeInterval);
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
        
        for (SpikeTrain spikeTrain : neurons)
            trains.add(spikeTrain.extractInterval(interval));
        
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
    
    protected final boolean filterMatch(String name, String filter) {
        name = name.toLowerCase();
        return name.startsWith(filter) && name.endsWith(SPIKE_FILE_EXT);
    }
}
