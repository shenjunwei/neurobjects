package nda.analysis.evaluation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import au.com.bytecode.opencsv.CSVWriter;


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

        writeLine(csv,
                "dataset", "classifier_id", "classifier_options",
                "correct", "incorrect", "correct_pct", "incorrect_pct");

        for (EvaluationResult result : results) {
            String trainSetName = result.getTrainSetName();
            String datasetName = getDatasetName(trainSetName);

            for (int i = 0; i < result.numEvaluations(); ++i) {
                NamedClassifier n_classifier = result.getClassifiers().get(i);
                Classifier classifier = n_classifier.getClassifier();
                Evaluation evaluation = result.getModelEvaluations().get(i);

                writeLine(csv,
                        datasetName, n_classifier.getName(),
                        StringUtils.join(classifier.getOptions(), ' ').trim(),
                        (int) evaluation.correct(), (int) evaluation.incorrect(),
                        evaluation.pctCorrect(), evaluation.pctIncorrect());
            }
        }

        try {
            csv.close();
        } catch (IOException e) {
            throw new EvaluationException(e);
        }
    }


    private static void writeLine(CSVWriter csv, Object... args) {
        String[] line = new String[args.length];

        for (int i = 0; i < line.length; ++i)
            line[i] = args[i].toString();

        csv.writeNext(line);
    }


    private static String getDatasetName(String  trainSetName) {
        int pos = trainSetName.lastIndexOf('_');
        String datasetName = trainSetName.substring(0, pos);
        return datasetName;
    }
}
