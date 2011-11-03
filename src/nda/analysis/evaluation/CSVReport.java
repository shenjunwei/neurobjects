package nda.analysis.evaluation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        headers.add("areas");
        headers.add("bin_size");
        headers.add("window_width");
        headers.add("classifier_id");
        headers.add("num_instances");
        headers.add("correct");
        headers.add("weighted_auroc");
        headers.add("kappa");

        GeneratorSetup.Dataset sampleDataset = results.get(0).dataset;
        int numClasses = sampleDataset.getClasses().size();

        for (GeneratorSetup.Class cls : sampleDataset.getClasses()) {
            String name = cls.getName();
            headers.add(name + "_fp");
            headers.add(name + "_fn");
            headers.add(name + "_fmeasure");
        }

        writeLine(csv, headers);

        for (EvaluationResult result : results) {
            GeneratorSetup.Dataset dataset = result.dataset;

            String datasetName = dataset.getSetName();
            String roundNumberStr = "" + result.roundNumber;

            NamedClassifier n_classifier = result.classifier;
            Evaluation evaluation = result.evaluation;

            List<String> line = new ArrayList<String>(headers.size());

            line.add(datasetName);
            line.add(roundNumberStr);
            line.add(result.getParameter("areas").toString());
            line.add(result.getParameter("bin_size").toString());
            line.add(result.getParameter("window_width").toString());
            line.add(n_classifier.getName());
            line.add("" + ((int) evaluation.numInstances()));
            line.add("" + ((int) evaluation.correct()));
            line.add("" + evaluation.weightedAreaUnderROC());
            line.add("" + evaluation.kappa());

            for (int j = 0; j < numClasses; ++j) {
                line.add("" + (int) evaluation.numFalsePositives(j));
                line.add("" + (int) evaluation.numFalseNegatives(j));
                line.add("" + evaluation.fMeasure(j));
            }

            writeLine(csv, line);
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
}
