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
import nda.data.text.TextBehaviorHandler;
import nda.util.RandomUtils;


/**
 * Contact split within contact
 * 
 * @author Giuliano Vilela
 */
public class AssembliesApp {
    private static Options options;

    private static String animalName;
    private static String spikesDir;
    private static String contactsFilepath;
    private static String outputFilepath;
    private static String matrixFilename;

    private static List<String> spikeFilters;
    private static double binSize;
    private static String area;
    private static int windowSize;

    private static List<String> labels;
    private static int numRounds;
    //  private static int minTotalPatterns;
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
            matrixFilename = (String) setup.get("matrix_file");
            outputFilepath = (String) setup.get("output_file");


            Map<String,Object> params = (Map<String,Object>) setup.get("params");
            //spikeFilters = (List<String>) params.get("areas");
            binSize = (Double) params.get("bin_size");
            area = (String) params.get("area");
            windowSize = (Integer) params.get("window_width");

            labels = (List<String>) setup.get("labels");
            numRounds = (Integer) setup.get("rounds");
            //   minTotalPatterns = (Integer) setup.get("min_total_patterns");
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

        BehaviorHandlerI behaviorHandler = new TextBehaviorHandler(contactsFilepath);
        CountMatrix countMatrix = new CountMatrix(matrixFilename, binSize);
        countMatrix.setWindowWidth(windowSize);
        // Would be interesting validate the labels supplied from behavior file and from the setup file.
        // For example, the labels supplied by the setup file can be found in the behavior file

        List<double[]> other = new ArrayList<double[]>();
        List<double[]> target = new ArrayList<double[]>();
        BufferedWriter out=null;
        try {
            out = new BufferedWriter(new FileWriter(outputFilepath));
        }
        catch (IOException e)
        {
            System.out.println("Exception ");

        }
        for (String label : labels) {
            //  List<Interval> intervalList = behaviorHandler.getContactIntervals(label);

            other.clear();
            target.clear();
            for (String str : labels) {
                for (Interval interval : behaviorHandler.getContactIntervals(str))
                    if (!str.equals(label)) {
                        other.addAll(countMatrix.getPatterns(interval));

                    }
                    else {
                        target.addAll(countMatrix.getPatterns(interval));
                    }
            }

            for (int round = 0; round < numRounds; ++round) {
                Object[] sampleTarget = RandomUtils.randomSample(random, target, numPatterns);
                Object[] sampleOther = RandomUtils.randomSample(random, other, numPatterns);

                List<Evaluation> results = evaluatePatternClassification(sampleTarget, sampleOther);
                for (int cv_fold = 0; cv_fold < results.size(); ++cv_fold) {
                    Evaluation result = results.get(cv_fold);
                    double pctCorrect = result.pctCorrect();
                    double auroc = result.weightedAreaUnderROC();
                    double kappa = result.kappa();
                    Map<String, String> csvResults =  new HashMap<String, String>();;

                    csvResults.put("animal", animalName);
                    csvResults.put("area", area);
                    csvResults.put("label", label);
                    csvResults.put("classifierName", classifierName);
                    csvResults.put("binSize", String.valueOf(binSize));

                    csvResults.put("windowSize", String.valueOf(windowSize));
                    csvResults.put("numPatterns", String.valueOf(numPatterns));
                    // csvResults.put("label", String.valueOf(binSize));

                    csvResults.put("round", String.valueOf(round));
                    csvResults.put("cv_fold", String.valueOf(cv_fold));
                    csvResults.put("pctCorrect", String.valueOf(pctCorrect));
                    csvResults.put("auroc", String.valueOf(auroc));
                    csvResults.put("kappa", String.valueOf(kappa));

                    //System.out.println (csvResults.values());

                    if (out!=null) {
                        out.write(buildSQL(csvResults,"ioc_assemblies")+"\n");
                    }
                    else {
                        System.out.println (buildSQL(csvResults,"ioc_assemblies"));
                    }


                    /*  System.out.printf(
                            "0, %s, %s, %s, %s, %f, %d, %d, %d, %d, %d, %f, %f, %f\n",
                            animalName, area, label, classifierName, binSize,
                            windowSize, minTotalPatterns, numPatterns,
                            round, cv_fold, pctCorrect, auroc, kappa); */
                }
            }
        }
        out.close();

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
            Object[] sampleA, Object[] sampleB) throws Exception {

        Instances datasetA = buildInstances(sampleA, "A");
        Instances datasetB = buildInstances(sampleB, "B");

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

        Instances dataset = new Instances("split_within_contact", attributes, patterns.length);
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