package nda.analysis.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import nda.analysis.InvalidSetupFileException;


/**
 * Thin wrapper for the feature sampler configuration files
 * 
 * @author Giuliano Vilela
 */
public class FeatureSamplerSetup {
    private static Yaml yaml = null;
    private Map<String,Object> topMap;

    public static Yaml getYaml() {
        if (yaml == null) yaml = new Yaml();
        return yaml;
    }


    public FeatureSamplerSetup(String filepath)
    throws FileNotFoundException, InvalidSetupFileException {
        this(new FileReader(filepath));
    }


    @SuppressWarnings("unchecked")
    public FeatureSamplerSetup(Reader reader) throws InvalidSetupFileException {
        try {
            topMap = (Map<String,Object>) getYaml().load(reader);
        } catch (ClassCastException e) {
            throw new InvalidSetupFileException(e);
        }
    }


    @Override
    public String toString() {
        return getYaml().dump(topMap);
    }


    public String getName() {
        return (String) topMap.get("name");
    }


    public String getSpikesDirectory() {
        if (topMap.containsKey("data_dir")) {
            File file = new File(getDataDirectory(), "spikes");
            file = new File(file, "01");
            return file.getPath();
        }
        else {
            String spikes_dir = (String) topMap.get("spikes_dir");
            return new File(spikes_dir).getPath();
        }
    }


    public String getContactsFilepath() {
        if (topMap.containsKey("data_dir")) {
            String filename = getName() + "_contacts.txt";
            File file = new File(getDataDirectory(), filename);
            return file.getPath();
        }
        else {
            return (String)topMap.get("contacts_file");
        }
    }


    private String getDataDirectory() {
        return (String)topMap.get("data_dir");
    }


    public String getFeature() {
        return (String)topMap.get("feature");
    }


    @SuppressWarnings("unchecked")
    public Map<String,Object> getParams() {
        return (Map<String,Object>) topMap.get("params");
    }


    public String getOutputFile() {
        return (String)topMap.get("output_file");
    }
}
