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


    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getParameterChoices() {
        Map<String, Object> paramsMap = (Map<String, Object>) topMap.get("params");

        List<Map<String, Object>> allChoices = new ArrayList<Map<String,Object>>();
        allChoices.add(new HashMap<String, Object>());
        List<Map<String,Object>> allChoicesCopy;

        // Parse areas
        Object areas = paramsMap.get("areas");
        allChoicesCopy = new ArrayList<Map<String,Object>>(allChoices);
        allChoices.clear();
        for (Map<String, Object> choice : allChoicesCopy) {
            if (areas instanceof List<?>) {
                for (String area : (List<String>) areas) {
                    Map<String,Object> newChoice = new HashMap<String, Object>(choice);
                    newChoice.put("areas", area);
                    allChoices.add(newChoice);
                }
            }
            else {
                choice.put("areas", areas);
                allChoices.add(choice);
            }
        }

        // Parse bin sizes
        Object bin_sizes = paramsMap.get("bin_size");
        allChoicesCopy = new ArrayList<Map<String,Object>>(allChoices);
        allChoices.clear();
        for (Map<String, Object> choice : allChoicesCopy) {
            if (bin_sizes instanceof List<?>) {
                for (double bin_size : (List<Double>) bin_sizes) {
                    Map<String,Object> newChoice = new HashMap<String, Object>(choice);
                    newChoice.put("bin_size", bin_size);
                    allChoices.add(newChoice);
                }
            }
            else {
                choice.put("bin_size", bin_sizes);
                allChoices.add(choice);
            }
        }

        // Parse window widths
        Object widths = paramsMap.get("window_width");
        allChoicesCopy = new ArrayList<Map<String,Object>>(allChoices);
        allChoices.clear();
        for (Map<String, Object> choice : allChoicesCopy) {
            if (widths instanceof List<?>) {
                for (int width : (List<Integer>) widths) {
                    Map<String,Object> newChoice = new HashMap<String, Object>(choice);
                    newChoice.put("window_width", width);
                    allChoices.add(newChoice);
                }
            }
            else {
                choice.put("window_width", widths);
                allChoices.add(choice);
            }
        }

        // Parse neuron drop
        Object drops = paramsMap.get("neuron_drop");
        allChoicesCopy = new ArrayList<Map<String,Object>>(allChoices);
        allChoices.clear();
        for (Map<String, Object> choice : allChoicesCopy) {
            if (drops instanceof List<?>) {
                for (boolean neuron_drop : (List<Boolean>) drops) {
                    Map<String,Object> newChoice = new HashMap<String, Object>(choice);
                    newChoice.put("neuron_drop", neuron_drop);
                    allChoices.add(newChoice);
                }
            }
            else {
                if (drops != null) choice.put("neuron_drop", drops);
                allChoices.add(choice);
            }
        }

        // Parse surrogate
        Object surrogates = paramsMap.get("surrogate");
        allChoicesCopy = new ArrayList<Map<String,Object>>(allChoices);
        allChoices.clear();
        for (Map<String, Object> choice : allChoicesCopy) {
            if (surrogates instanceof List<?>) {
                for (String surrogate : (List<String>) surrogates) {
                    Map<String,Object> newChoice = new HashMap<String, Object>(choice);
                    newChoice.put("surrogate", surrogate);
                    allChoices.add(newChoice);
                }
            }
            else {
                if (surrogates != null) choice.put("surrogate", surrogates);
                allChoices.add(choice);
            }
        }

        // Store a unique id for each choice
        int id = 1;
        for (Map<String, Object> choice : allChoices)
            choice.put("_id", id++);

        return allChoices;
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


                boolean doNeuronDrop =
                    paramsMap.containsKey("neuron_drop") &&
                    !paramsMap.get("neuron_drop").equals(false);

                boolean doNeuronSurrogate =
                    paramsMap.containsKey("surrogate") && (
                            paramsMap.get("surrogate").equals("uniform") ||
                            paramsMap.get("surrogate").equals("poisson") ||
                            ((String)paramsMap.get("surrogate")).startsWith("neuron_swap"));

                boolean doFullSurrogate =
                    paramsMap.containsKey("surrogate") && (
                            ((String) paramsMap.get("surrogate")).startsWith("col_swap") ||
                            ((String) paramsMap.get("surrogate")).startsWith("matrix_swap") ||
                            ((String) paramsMap.get("surrogate")).startsWith("poisson_d") ||
                            ((String) paramsMap.get("surrogate")).startsWith("uniform_d") ||
                            ((String) paramsMap.get("surrogate")).startsWith("spike_jitter"));

                boolean doSurrogate = doNeuronSurrogate || doFullSurrogate;

                if (doNeuronDrop && doSurrogate) {
                    throw new InvalidSetupFileException("Can't do neuron dropping and surrogates");
                }
                // Generate datasets per number or neurons transformed
                else if (doNeuronDrop || doNeuronSurrogate) {
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
                            String surrogate = (String) paramsMap.get("surrogate");

                            String str = null;
                            if (surrogate.equals("uniform"))
                                str = "_sur_uniform";
                            else if (surrogate.equals("poisson"))
                                str = "_sur_poisson";
                            else if (surrogate.startsWith("neuron_swap"))
                                str = "_sur_neuron_swap";

                            sub_dataset.name = dataset.name + str + num_changed;
                            sub_dataset.localParams.put("num_surrogate", num_changed);

                            if (surrogate.startsWith("neuron_swap")) {
                                sub_dataset.localParams.put("surrogate_type", "neuron_swap");
                                double pct = Double.valueOf(surrogate.split(" ")[1]);
                                sub_dataset.localParams.put("pct_surrogate", pct);
                            }
                            else {
                                sub_dataset.localParams.put("surrogate_type", surrogate);
                            }
                        }

                        datasets.add(sub_dataset);
                    }
                }
                else if (doFullSurrogate) {
                    String sur_spec = (String) paramsMap.get("surrogate");
                    String[] tokens = sur_spec.split(" ");
                    String sur_type = tokens[0];

                    if (sur_type.equals("col_swap_d")) {
                        List<Double> pcts = new ArrayList<Double>();
                        List<Double> dists = new ArrayList<Double>();
                        List<Double> current = null;

                        for (int i = 1; i < tokens.length; ++i) {
                            String token = tokens[i];
                            if (token.equals("p="))
                                current = pcts;
                            else if (token.equals("d="))
                                current = dists;
                            else
                                current.add(Double.valueOf(token));
                        }

                        int id = 1;
                        for (double pct : pcts) {
                            for (double dist : dists) {
                                Dataset sub_dataset = new Dataset(dataset);
                                sub_dataset.name = dataset.name + "_sur_" + sur_type + id++;
                                sub_dataset.localParams.put("pct_surrogate", pct);
                                sub_dataset.localParams.put("dist_surrogate", dist);
                                sub_dataset.localParams.put("surrogate_type", sur_type);

                                datasets.add(sub_dataset);
                            }
                        }
                    }
                    else {
                        for (int i = 1; i < tokens.length; ++i) {
                            double pct = Double.valueOf(tokens[i]);

                            Dataset sub_dataset = new Dataset(dataset);
                            sub_dataset.name = dataset.name + "_sur_" + sur_type + i;

                            if (sur_type.equals("poisson_d") || sur_type.equals("uniform_d") || sur_type.equals("spike_jitter"))
                                sub_dataset.localParams.put("dist_surrogate", pct);
                            else
                                sub_dataset.localParams.put("pct_surrogate", pct);

                            sub_dataset.localParams.put("surrogate_type", sur_type);
                            datasets.add(sub_dataset);
                        }
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
