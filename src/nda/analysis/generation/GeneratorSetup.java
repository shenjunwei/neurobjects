package nda.analysis.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import nda.analysis.InvalidSetupFileException;
import nda.data.text.InvalidDataDirectoryException;
import nda.data.text.TextSpikeHandler;


/**
 * Thin wrapper for the dataset setup configuration files.
 *
 * @author Giuliano Vilela
 */
public class GeneratorSetup {
    private static Yaml yaml = null;

    public static Yaml getYaml() {
        if (yaml == null) yaml = new Yaml();
        return yaml;
    }


    private Map<String, Object> topMap;
    private List<Dataset> datasets;

    public GeneratorSetup(String filepath) throws InvalidSetupFileException, FileNotFoundException {
        this(new FileReader(filepath));
    }


    @SuppressWarnings("unchecked")
    public GeneratorSetup(Reader reader) throws InvalidSetupFileException {
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


    /**
     * @TODO Handle cases where other parameters, besides 'neurons', are composite
     *       Should call a recursive function
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getParameterChoices() {
        List<Map<String, Object>> paramChoices = new ArrayList<Map<String,Object>>();

        Map<String, Object> paramsMap = (Map<String, Object>) topMap.get("params");
        Object areaChoices = paramsMap.get("areas");

        int choiceId = 1;

        if (areaChoices instanceof List<?>) {
            for (String area : (List<String>) areaChoices) {
                Map<String, Object> choice = new HashMap<String, Object>();
                choice.put("areas", area);

                for (String paramName : paramsMap.keySet())
                    if (!paramName.equals("areas"))
                        choice.put(paramName, paramsMap.get(paramName));

                choice.put("_id", choiceId++);
                paramChoices.add(choice);
            }

            return paramChoices;
        }
        else {
            paramsMap.put("_id", 1);
            paramChoices.add(paramsMap);
            return paramChoices;
        }
    }


    private String getDataDirectory() {
        String data_dir = (String) topMap.get("data_dir");
        return data_dir;
    }


    @SuppressWarnings("unchecked")
    private void parseDatasets() throws InvalidSetupFileException {
        datasets = new ArrayList<Dataset>();

        List<Map<String, Object>> paramChoices = getParameterChoices();
        List<Object> datasetList = (List<Object>) topMap.get("datasets");

        for (Map<String, Object> paramsMap : paramChoices) {
            for (Object datasetObj : datasetList) {
                Map<String, Object> datasetMap = (Map<String, Object>) datasetObj;

                List<Dataset> list = Dataset.parseAll(topMap, datasetMap, paramsMap, this);
                datasets.addAll(list);
            }
        }
    }


    public static class Dataset {
        private String name;
        private Map<String, Object> topMap;
        private Map<String, Object> datasetMap;
        private Map<String, Object> paramsMap;
        private Map<String, Object> localParams;
        private List<Class> classes;
        private GeneratorSetup setup;

        private static List<Dataset> parseAll(
                Map<String, Object> topMap,
                Map<String, Object> datasetMap,
                Map<String, Object> paramsMap,
                GeneratorSetup setup) throws InvalidSetupFileException {

            if (datasetMap.containsKey("1_vs_n"))
                return parse1vsN(topMap, datasetMap, paramsMap, setup);
            else
                throw new UnsupportedOperationException("Not yet implemented");
        }

        @SuppressWarnings("unchecked")
        private static List<Dataset> parse1vsN(
                Map<String, Object> topMap,
                Map<String, Object> datasetMap,
                Map<String, Object> paramsMap,
                GeneratorSetup setup) throws InvalidSetupFileException {

            Map<String, Object> typeMap = (Map<String, Object>) datasetMap.get("1_vs_n");
            List<String> labels = (List<String>) typeMap.get("labels");

            int n_labels = labels.size();
            List<Dataset> datasets = new ArrayList<Dataset>(n_labels);

            double train_ratio = Double.valueOf(datasetMap.get("train_ratio").toString());
            double class_ratio = Double.valueOf(typeMap.get("class_ratio").toString());
            int total_positives = (Integer) typeMap.get("total_positives");

            for (String pos_label : labels) {
                String name;
                if (datasetMap.containsKey("name"))
                    name = (String) datasetMap.get("name");
                else
                    name = (String) topMap.get("name");
                name += "_" + pos_label;

                Dataset dataset = new Dataset(name);

                dataset.topMap = topMap;
                dataset.datasetMap = datasetMap;
                dataset.classes = new ArrayList<Class>(n_labels);
                dataset.paramsMap = paramsMap;
                dataset.setup = setup;

                // Create the positive class
                int pos_train_s = (int) Math.floor(total_positives * train_ratio);
                int pos_test_s = total_positives - pos_train_s;

                Class positiveClass = new Class(
                        "yes", dataset, pos_label, pos_train_s, pos_test_s);


                // Create the negative class
                List<String> neg_labels = new ArrayList<String>(labels);
                neg_labels.remove(pos_label);

                int neg_train_s = (int) Math.floor(pos_train_s * class_ratio);
                int neg_test_s = (int) Math.floor(pos_test_s * class_ratio);

                Class negativeClass = new Class(
                        "no", dataset, neg_labels, neg_train_s, neg_test_s);


                // Add the classes to the dataset
                dataset.classes.add(positiveClass);
                dataset.classes.add(negativeClass);

                boolean doNeuronDrop = paramsMap.containsKey("neuron_drop");
                boolean doSurrogate = paramsMap.containsKey("surrogate");

                if (doNeuronDrop && doSurrogate) {
                    throw new InvalidSetupFileException("Can't do neuron dropping and surrogates");
                }
                // Generate extra datasets for neuron dropping
                else if (doNeuronDrop || doSurrogate) {
                    String dir = setup.getSpikesDirectory();
                    String filter = (String) dataset.getParameter("areas");

                    int numNeurons;
                    try {
                        numNeurons = TextSpikeHandler.spikeTrainCount(dir, filter);
                    } catch (InvalidDataDirectoryException e) {
                        numNeurons = 0;
                    }

                    int limChanged;
                    if (doNeuronDrop)
                        limChanged = numNeurons-1;
                    else
                        limChanged = numNeurons;

                    for (int num_changed = 0; num_changed <= limChanged; ++num_changed) {
                        Dataset sub_dataset = new Dataset(dataset);

                        if (doNeuronDrop) {
                            sub_dataset.name = dataset.name + "_d" + num_changed;
                            sub_dataset.localParams.put("num_drop", num_changed);
                        }
                        else {
                            sub_dataset.name = dataset.name + "_s" + num_changed;
                            sub_dataset.localParams.put("num_surrogate", num_changed);

                            String sur_type = (String) paramsMap.get("surrogate");
                            sub_dataset.localParams.put("surrogate_type", sur_type);
                        }

                        datasets.add(sub_dataset);
                    }
                }
                else {
                    // Add the dataset to the main list
                    datasets.add(dataset);
                }
            }

            return datasets;
        }


        private Dataset(String _name) {
            name = _name;
            localParams = new HashMap<String, Object>();
        }


        private Dataset(Dataset copy) {
            name = copy.name;
            topMap = copy.topMap;
            datasetMap = copy.datasetMap;
            paramsMap = copy.paramsMap;
            classes = copy.classes;
            setup = copy.setup;

            localParams = new HashMap<String, Object>();
        }


        @Override
        public String toString() {
            return String.format("Datasetmap: %s\nTopMap: %s\nClasses: %s",
                    datasetMap, topMap, classes);
        }

        public String getSetName() {
            return name;
        }

        public String getName() {
            return name + "_p" + paramsMap.get("_id");
        }

        public int getParameterChoiceId() {
            return (Integer) paramsMap.get("_id");
        }

        public int getNumberRounds() {
            return (Integer) datasetMap.get("rounds");
        }

        public double getTrainRatio() {
            return (Double) datasetMap.get("train_ratio");
        }

        public Object getParameter(String parameter) {
            if (localParams.get(parameter) != null)
                return localParams.get(parameter);
            else
                return paramsMap.get(parameter);
        }

        public List<Class> getClasses() {
            return classes;
        }

        public GeneratorSetup getSetup() {
            return setup;
        }

        public List<String> getGeneratedFileNames() {
            int numRounds = getNumberRounds();
            List<String> files = new ArrayList<String>();

            for (int round = 1; round <= numRounds; ++round) {
                files.add(getGeneratedFileName("train", round));
                files.add(getGeneratedFileName("test", round));
            }

            return files;
        }

        public String getGeneratedFileName(String set, int round) {
            return String.format("%s_p%d_r%d_%s.arff",
                    name, paramsMap.get("_id"), round, set);
        }
    }

    public static class Class {
        private String name;
        private Dataset dataset;
        private List<String> labels;
        private int trainSamples, testSamples;

        private Class(String c_name, Dataset ds, String label, int train_s, int test_s) {
            this(c_name, ds, Arrays.asList(label), train_s, test_s);
        }

        private Class(String c_name, Dataset ds, List<String> label_l, int train_s, int test_s) {
            name = c_name;
            dataset = ds;
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

        public Dataset getDataset() {
            return dataset;
        }
    }
}
