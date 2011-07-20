package nda.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;


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

        List<Object> datasetList = (List<Object>) topMap.get("datasets");

        for (Object datasetObj : datasetList) {
            Map<String, Object> datasetMap = (Map<String, Object>) datasetObj;

            List<Dataset> list = Dataset.parseAll(topMap, datasetMap);
            datasets.addAll(list);
        }
    }


    public static class Dataset {
        private String name;
        private Map<String, Object> topMap;
        private Map<String, Object> datasetMap;
        private List<Class> classes;

        private static List<Dataset> parseAll(
                Map<String, Object> topMap,
                Map<String, Object> map) {

            if (map.containsKey("1_vs_n")) {
                return parse1vsN(topMap, map);
            }
            else {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        }

        @SuppressWarnings("unchecked")
        private static List<Dataset> parse1vsN(
                Map<String, Object> topMap, Map<String, Object> d_map) {

            Map<String, Object> typeMap = (Map<String, Object>) d_map.get("1_vs_n");
            List<String> labels = (List<String>) typeMap.get("labels");

            int n_labels = labels.size();
            List<Dataset> datasets = new ArrayList<Dataset>(n_labels);

            double train_ratio = Double.valueOf(d_map.get("train_ratio").toString());
            double class_ratio = Double.valueOf(typeMap.get("class_ratio").toString());
            int total_positives = (Integer) typeMap.get("total_positives");

            for (String pos_label : labels) {
                String name;
                if (d_map.containsKey("name"))
                    name = (String) d_map.get("name");
                else
                    name = (String) topMap.get("name");
                name += "_" + pos_label;

                Dataset dataset = new Dataset(name);

                dataset.topMap = topMap;
                dataset.datasetMap = d_map;
                dataset.classes = new ArrayList<Class>(n_labels);

                // Create the positive class
                int pos_train_s = (int) Math.floor(total_positives * train_ratio);
                int pos_test_s = total_positives - pos_train_s;

                Class positiveClass = new Class(
                        "yes", pos_label, pos_train_s, pos_test_s);


                // Create the negative class
                List<String> neg_labels = new ArrayList<String>(labels);
                neg_labels.remove(pos_label);

                int neg_train_s = (int) Math.floor(pos_train_s * class_ratio);
                int neg_test_s = (int) Math.floor(pos_test_s * class_ratio);

                Class negativeClass = new Class(
                        "no", neg_labels, neg_train_s, neg_test_s);


                // Add the classes to the dataset
                dataset.classes.add(positiveClass);
                dataset.classes.add(negativeClass);

                // Add the dataset to the main list
                datasets.add(dataset);
            }

            return datasets;
        }


        private Dataset(String _name) {
            name = _name;
        }

        @Override
        public String toString() {
            return String.format("Datasetmap: %s\nTopMap: %s\nClasses: %s",
                    datasetMap, topMap, classes);
        }

        public String getName() {
            return name;
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

            if (localParams != null && localParams.containsKey(parameter)) {
                return localParams.get(parameter);
            }
            else {
                Map<String,Object> g_params = (Map<String,Object>) topMap.get("params");
                return g_params.get(parameter);
            }
        }

        public List<Class> getClasses() {
            return classes;
        }
    }

    public static class Class {
        private String name;
        private List<String> labels;
        private int trainSamples, testSamples;

        private Class(String c_name, String label, int train_s, int test_s) {
            this(c_name, Arrays.asList(label), train_s, test_s);
        }

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
