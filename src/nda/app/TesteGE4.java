package nda.app;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.cli.Options;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.yaml.snakeyaml.Yaml;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import nda.data.BehaviorHandlerI;
import nda.data.CountMatrix;
import nda.data.Interval;
import nda.data.SpikeHandlerI;
import nda.data.text.TextBehaviorHandler;
import nda.data.text.TextSpikeHandler;
import nda.util.RandomUtils;


/**
 * Contact split within contact
 * 
 * @author Giuliano Vilela
 */
public class TesteGE4 {
    private static Options options;

    private static String animalName;
    private static String spikesDir;
    private static String contactsFilepath;
    private static String outputFilepath;

    private static List<String> spikeFilters;
    private static double binSize;
    private static int windowSize;
    private static Integer minTotalPatterns;
    private static List<String> labels;
    private static int numRounds;

    private static int numPatterns;

    private static String classifierName;
    private static Classifier classifier;
    private static int numCrossValidationFolds;

    private static Random random = new Random();


    @SuppressWarnings("unchecked")
    private static void readSetupFile(String setupFilepath) throws Exception {
        Yaml yaml = new Yaml();
        FileReader reader = null;

        try {
            reader = new FileReader(setupFilepath);

            Map<String,Object> setup = (Map<String,Object>) yaml.load(reader);
            animalName = (String) setup.get("name");
            spikesDir = (String) setup.get("spikes_dir");
            contactsFilepath = (String) setup.get("contacts_file");
            outputFilepath = (String) setup.get("output_file");

            Map<String,Object> params = (Map<String,Object>) setup.get("params");
            spikeFilters = (List<String>) params.get("areas");
            binSize = (Double) params.get("bin_size");
            windowSize = (Integer) params.get("window_width");

            labels = (List<String>) setup.get("labels");
            numRounds = (Integer) setup.get("rounds");
            minTotalPatterns = (Integer) setup.get("min_total_patterns");
            numPatterns = (Integer) setup.get("num_patterns");

            classifierName = (String) setup.get("classifier");
            classifier = Classifier.forName(classifierName, null);
            numCrossValidationFolds = (Integer) setup.get("num_cv_folds");

        } finally {
            if (reader != null) reader.close();
        }
    }


    public static void main(String[] args) throws Exception {
        RandomData random = new RandomDataImpl();

        boolean simulateOnly;
        String setupFilepath;

        if (args[0].equals("-t")) {
            simulateOnly = true;
            setupFilepath = args[1];
        }
        else {
            simulateOnly = false;
            setupFilepath = args[0];
        }

        readSetupFile(setupFilepath);

        List<double[]> firstPattern = new ArrayList<double[]>();
        List<double[]> secondPattern = new ArrayList<double[]>();

        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(outputFilepath));
        }
        catch (IOException e)
        {
            System.out.println("Exception!");

        }

        BehaviorHandlerI behaviorHandler = new TextBehaviorHandler(contactsFilepath);

        int interval_id = 0;

        String firstLabel = labels.get(0);
        String secondLabel = labels.get(1);


        for (String filter : spikeFilters) {

            SpikeHandlerI spikeHandler = new TextSpikeHandler(spikesDir, filter, animalName);
            CountMatrix countMatrix = new CountMatrix(spikeHandler, binSize);
            countMatrix.setWindowWidth(windowSize);

            for (String str : labels){

                firstPattern.clear();
                secondPattern.clear();

                for (String label : labels) {

                    for (Interval interval : behaviorHandler.getContactIntervals(label)){
                        if (label.equals(firstLabel)){
                            firstPattern.addAll(countMatrix.getPatterns(interval));

                        }

                        if (label.equals(secondLabel)){
                            secondPattern.addAll(countMatrix.getPatterns(interval));
                        }
                    }

                }

                for (int round = 0; round < numRounds; ++round) {
                    Object[] sampleBall = RandomUtils.randomSample(random, firstPattern, numPatterns);
                    Object[] sampleFood = RandomUtils.randomSample(random, secondPattern, numPatterns);

                    List<Evaluation> results = evaluatePatternClassification(sampleBall, sampleFood, firstLabel,
                            secondLabel);

                    for (int cv_fold = 0; cv_fold < results.size(); ++cv_fold) {
                        Evaluation result = results.get(cv_fold);
                        double pctCorrect = result.pctCorrect();
                        double auroc = result.weightedAreaUnderROC();
                        double kappa = result.kappa();

                        Map<String, String> csvResults =  new HashMap<String, String>();;

                        csvResults.put("animal", animalName);
                        csvResults.put("area", filter);
                        csvResults.put("peers", firstLabel + "x" +secondLabel);
                        //csvResults.put("peer2", secondLabel);
                        csvResults.put("model", classifierName);
                        csvResults.put("bin_size", String.valueOf(binSize));

                        csvResults.put("window_size", String.valueOf(windowSize));
                        csvResults.put("num_patterns", String.valueOf(numPatterns));
                        // csvResults.put("label", String.valueOf(binSize));

                        csvResults.put("round", String.valueOf(round));
                        csvResults.put("cv_fold", String.valueOf(cv_fold));
                        csvResults.put("pct_correct", String.valueOf(pctCorrect));
                        csvResults.put("auroc", String.valueOf(auroc));
                        csvResults.put("kappa", String.valueOf(kappa));

                        //System.out.println (csvResults.values());

                        if (out!=null) {
                            // System.out.println("ok");
                            out.write(buildSQL(csvResults,"results_peers")+"\n");
                        }
                        else {
                            System.out.println (buildSQL(csvResults,"results_peers"));
                        }

                        ++interval_id;
                    }

                }

            }

        }
        out.close();
        System.out.println("File generated successfully.");

    }

    private static String buildSQL (Map<String, String> csvResults, String tableName) {
        String sqlQuery="INSERT INTO "+tableName+" ";
        //  String param[] = (String[]) csvResults.keySet().toArray();
        Object params[] = csvResults.keySet().toArray();
        sqlQuery+=" (";
        for (Object param: params) {
            sqlQuery+=""+param+",";

        }
        sqlQuery = sqlQuery.substring(0, sqlQuery.length()-1 );
        sqlQuery += ") VALUES (";
        Object values[] = csvResults.values().toArray();
        for (Object value: values) {
            sqlQuery+="'"+value+"',";
        }
        sqlQuery = sqlQuery.substring(0, sqlQuery.length()-1 );
        sqlQuery += "); ";


        return sqlQuery;
    }


    private static List<Evaluation> evaluatePatternClassification(
            Object[] sampleA, Object[] sampleB, String firstLabel, String secondLabel) throws Exception {

        firstLabel = "A";
        secondLabel = "B";

        Instances datasetA = buildInstances(sampleA, firstLabel);
        Instances datasetB = buildInstances(sampleB, secondLabel);

        Instances dataset = new Instances(datasetA);
        for (int i = 0; i < datasetB.numInstances(); ++i)
            dataset.add(datasetB.instance(i));

        List<Evaluation> evaluations = getCrossValidationEvaluations(
                classifier, dataset, numCrossValidationFolds);

        return evaluations;
    }


    private static Instances buildInstances(Object[] patterns, String classValue) {

        int patternSize = ((double[]) patterns[0]).length;

        FastVector attributes = new FastVector();
        for (int i = 0; i < patternSize; ++i)
            attributes.addElement(new Attribute("bin_" + i));

        FastVector classValues = new FastVector();
        classValues.addElement("A");
        classValues.addElement("B");

        Attribute classAttribute = new Attribute("class", classValues);
        attributes.addElement(classAttribute);

        Instances dataset = new Instances("Peers", attributes, patterns.length);
        dataset.setClass(classAttribute);

        for (Object patternObj : patterns) {
            double[] pattern = (double[]) patternObj;

            Instance instance = new Instance(dataset.numAttributes());
            instance.setDataset(dataset);

            for (int i = 0; i < pattern.length; ++i)
                instance.setValue(i, pattern[i]);
            instance.setValue(pattern.length, classValue);

            dataset.add(instance);
        }

        return dataset;
    }


    /**
     * This is a duplicate of the logic from EvaluationUtils.getCVPredictions.
     * 
     * The replication is due to the need for results in the form Evaluation
     * instances instead of Prediction's.
     */
    private static List<Evaluation> getCrossValidationEvaluations(
            Classifier model, Instances dataset, int numFolds) throws Exception {

        List<Evaluation> results = new ArrayList<Evaluation>();
        Instances cvData = new Instances(dataset);
        cvData.randomize(random);

        if (cvData.classAttribute().isNominal() && (numFolds > 1))
            cvData.stratify(numFolds);

        for (int fold = 0; fold < numFolds; ++fold) {
            Instances trainData = cvData.trainCV(numFolds, fold, random);
            Instances testData = cvData.testCV(numFolds, fold);

            Evaluation evaluation = evaluateTrainTest(model, trainData, testData);

            // Store
            results.add(evaluation);
        }

        return results;
    }


    protected static Evaluation evaluateTrainTest(
            Classifier model, Instances trainData, Instances testData) throws Exception {

        // Defensive copy
        model = Classifier.makeCopy(model);

        // Train
        model.buildClassifier(trainData);

        // Classify
        Evaluation evaluation = new Evaluation(trainData);
        evaluation.evaluateModel(model, testData);

        return evaluation;
    }
}