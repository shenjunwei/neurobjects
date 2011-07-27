package nda.analysis.evaluation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import au.com.bytecode.opencsv.CSVWriter;

import nda.analysis.generation.GeneratorSetup;


/**
 * Generate reports for an evaluation using a CSV file.
 *
 * @author Giuliano Vilela
 */
public class CSVReport implements EvaluationReportI {
    private String outputFilePath;

    public CSVReport(EvaluatorSetup setup) {
        outputFilePath = (String) setup.getReportParameter("file");
    }


    /**
     * @see nda.analysis.evaluation.EvaluationReportI#makeReport(nda.analysis.evaluation.EvaluationResult)
     */
    @Override
    public void makeReport(List<EvaluationResult> results) throws EvaluationException {
        CSVWriter csv;

        try {
            FileWriter writer = new FileWriter(outputFilePath);
            csv = new CSVWriter(writer);
        } catch (IOException e) {
            throw new EvaluationException(
                    "Can't write to output file: " + outputFilePath, e);
        }

        List<String> headers = new ArrayList<String>();
        headers.add("dataset");
        headers.add("round");
        headers.add("classifier_id");
        headers.add("classifiers_options");
        headers.add("correct");
        headers.add("incorrect");
        headers.add("correct_pct");

        GeneratorSetup.Dataset dataset = results.get(0).getDataset();
        int numClasses = dataset.getClasses().size();

        for (GeneratorSetup.Class cls : dataset.getClasses()) {
            String name = cls.getName();
            headers.add(name + "_area_under_roc");
            headers.add(name + "_false_positives");
            headers.add(name + "_false_negatives");
        }

        writeLine(csv, headers);

        for (EvaluationResult result : results) {
            String trainSetName = result.getTrainSetName();
            String datasetName = getDatasetName(trainSetName);
            String roundNumber = getRoundNumber(trainSetName);

            for (int i = 0; i < result.numEvaluations(); ++i) {
                NamedClassifier n_classifier = result.getClassifiers().get(i);
                Classifier classifier = n_classifier.getClassifier();
                Evaluation evaluation = result.getModelEvaluations().get(i);

                List<String> line = new ArrayList<String>(headers.size());

                line.add(datasetName);
                line.add(roundNumber);
                line.add(n_classifier.getName());
                line.add(StringUtils.join(classifier.getOptions(), ' ').trim());
                line.add("" + ((int) evaluation.correct()));
                line.add("" + ((int) evaluation.incorrect()));
                line.add("" + evaluation.pctCorrect());

                for (int j = 0; j < numClasses; ++j) {
                    line.add("" + evaluation.areaUnderROC(j));
                    line.add("" + (int) evaluation.numFalsePositives(j));
                    line.add("" + (int) evaluation.numFalseNegatives(j));
                }

                writeLine(csv, line);
            }
        }

        try {
            csv.close();
        } catch (IOException e) {
            throw new EvaluationException(e);
        }
    }


    private static void writeLine(CSVWriter csv, List<String> line) {
        csv.writeNext(line.toArray(new String[0]));
    }


    private static String getDatasetName(String  trainSetName) {
        int last_underscore = trainSetName.lastIndexOf('_');
        int p_underscore = trainSetName.lastIndexOf('_', last_underscore - 1);

        String datasetName = trainSetName.substring(0, p_underscore);
        return datasetName;
    }


    private static String getRoundNumber(String trainSetName) {
        int last_underscore = trainSetName.lastIndexOf('_');
        int p_underscore = trainSetName.lastIndexOf('_', last_underscore - 1);

        String roundString = trainSetName.substring(p_underscore+1, last_underscore);
        return roundString;
    }
}
