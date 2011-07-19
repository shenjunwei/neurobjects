package nda.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import nda.data.BehaviorHandlerI;
import nda.data.text.TextBehaviorHandler;


/**
 * Thin wrapper for the dataset setup configuration files.
 * 
 * @author Nivaldo Vasconcelos
 * @author Giuliano Vilela
 */
public class Setup {
    private static Yaml yaml = null;

    public static Yaml getYaml() {
        if (yaml == null) yaml = new Yaml();
        return yaml;
    }


    private Map<String, Object> topMap;
    private List<Dataset> datasets;

    public Setup(String filepath) throws InvalidSetupFileException, FileNotFoundException {
        this(new FileReader(filepath));
    }


    @SuppressWarnings("unchecked")
    public Setup(Reader reader) throws InvalidSetupFileException {
        try {
            topMap = (Map<String, Object>) getYaml().load(reader);
            parseDatasets();
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
            return (String) topMap.get("contacts_file");
        }
    }


    public String getOutputDirectory() {
        if (topMap.containsKey("data_dir")) {
            File file = new File(getDataDirectory(), "datasets");
            return file.getPath();
        }
        else {
            String output_dir = (String) topMap.get("output_dir");
            return new File(output_dir).getPath();
        }
    }


    public List<Dataset> getDatasets() {
        return datasets;
    }


    private String getDataDirectory() {
        String data_dir = (String) topMap.get("data_dir");
        return data_dir;
    }


    @SuppressWarnings("unchecked")
    private void parseDatasets() throws InvalidSetupFileException {
        datasets = new ArrayList<Dataset>();

        Map<String, Object> params = (Map<String,Object>) topMap.get("params");
        List<Object> datasetList = (List<Object>) topMap.get("datasets");

        for (Object datasetObj : datasetList) {
            Map<String, Object> datasetMap = (Map<String, Object>) datasetObj;

            Dataset dataset = new Dataset(params, datasetMap, getContactsFilepath());
            datasets.add(dataset);
        }
    }


    public static class Dataset {
        private String contacts_filepath;
        private Map<String, Object> globalParams;
        private Map<String, Object> datasetMap;
        private List<Class> classes;


        private Dataset(Map<String, Object> params, Map<String, Object> map,
                String contacts) throws InvalidSetupFileException {
            globalParams = params;
            datasetMap = map;
            contacts_filepath = contacts;
            parseClasses();
        }

        @Override
        public String toString() {
            return String.format("Datasetmap: %s\nGlobalParams: %s\nClasses: %s",
                    datasetMap, globalParams, classes);
        }

        public String getName() {
            return (String) datasetMap.get("name");
        }

        public int getNumberRounds() {
            return (Integer) datasetMap.get("rounds");
        }

        public double getTrainRatio() {
            return (Double) datasetMap.get("train_ratio");
        }

        @SuppressWarnings("unchecked")
        public Object getParameter(String parameter) {
            Map<String,Object> localParams = (Map<String,Object>)datasetMap.get("params");

            if (localParams != null && localParams.containsKey(parameter))
                return localParams.get(parameter);
            else
                return globalParams.get(parameter);
        }

        public List<Class> getClasses() {
            return classes;
        }

        private void parseClasses() throws InvalidSetupFileException {
            classes = new ArrayList<Class>();

            if (datasetMap.containsKey("classes"))
                parseExplicitClasses();
            else
                parseImplicitClasses();
        }

        private void parseExplicitClasses() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @SuppressWarnings("unchecked")
        private void parseImplicitClasses() throws InvalidSetupFileException {
            double train_ratio = getTrainRatio();
            double class_ratio = Double.valueOf(datasetMap.get("class_ratio").toString());
            int total_positives = (Integer) datasetMap.get("total_positives");

            // Create the "yes" class
            List<String> pos_labels = (List<String>) datasetMap.get("positives");
            int pos_train_s = (int) Math.floor(total_positives * train_ratio);
            int pos_test_s = total_positives - pos_train_s;

            Class positiveClass = new Class("yes", pos_labels, pos_train_s, pos_test_s);


            // Create the "no" class
            BehaviorHandlerI behaviorHandler;

            try {
                behaviorHandler = new TextBehaviorHandler(contacts_filepath);
            } catch (Exception e) {
                throw new InvalidSetupFileException(e);
            }

            Set<String> labels = behaviorHandler.getLabelSet();
            labels.removeAll(pos_labels);
            List<String> neg_labels = new ArrayList<String>(labels);
            int neg_train_s = (int) Math.floor(pos_train_s * class_ratio);
            int neg_test_s = (int) Math.floor(pos_test_s * class_ratio);

            Class negativeClass = new Class("no", neg_labels, neg_train_s, neg_test_s);


            // Store them in the Dataset object
            classes.add(positiveClass);
            classes.add(negativeClass);
        }
    }

    public static class Class {
        private String name;
        private List<String> labels;
        private int trainSamples, testSamples;

        private Class(String c_name, List<String> label_l, int train_s, int test_s) {
            name = c_name;
            labels = label_l;
            trainSamples = train_s;
            testSamples = test_s;
        }

        @Override
        public String toString() {
            return String.format("%s: {labels: %s, train: %d, test: %d}",
                    name, labels, trainSamples, testSamples);
        }

        public String getName() {
            return name;
        }

        public List<String> getLabels() {
            return labels;
        }

        public int getNumberSamples() {
            return trainSamples + testSamples;
        }

        public int getNumberTrainSamples() {
            return trainSamples;
        }

        public int getNumberTestSamples() {
            return testSamples;
        }
    }
}
