package nda.analysis.evaluation;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import weka.core.Instances;

import nda.analysis.generation.GeneratorSetup;


/**
 * Simple AbstractEvaluator implementation that reads datasets from a given
 * directory, evaluates them in a single thread.
 * 
 * @author Giuliano Vilela
 */
public class SimpleEvaluator extends DatasetEvaluator {
    private GeneratorSetup generatorSetup;


    public SimpleEvaluator(GeneratorSetup gen_setup, EvaluatorSetup eva_setup) {
        super(eva_setup);
        generatorSetup = gen_setup;
    }


    @Override
    public List<EvaluationResult> evaluate() throws EvaluationException {
        showMessage("Evaluating datasets...\n");

        List<EvaluationResult> results = new ArrayList<EvaluationResult>();
        File outputDir = new File(generatorSetup.getOutputDirectory());

        for (GeneratorSetup.Dataset dataset : generatorSetup.getDatasets()) {
            showMessage("Evaluating dataset " + dataset.getName() + " ...");

            List<String> fileNames = dataset.getGeneratedFileNames();

            for (int i = 0; i < fileNames.size()-1; i += 2) {
                int round = i/2 + 1;
                String trainDataFileName = fileNames.get(i);
                String testDataFileName = fileNames.get(i+1);

                File trainDataFile = new File(outputDir, trainDataFileName);
                File testDataFile = new File(outputDir, testDataFileName);

                try {
                    showMessage(" - reading file " + trainDataFile + "...");
                    FileReader trainDataReader = new FileReader(trainDataFile);

                    showMessage(" - reading file " + testDataFile + "...");
                    FileReader testDataReader = new FileReader(testDataFile);

                    Instances trainData = new Instances(trainDataReader);
                    Instances testData = new Instances(testDataReader);

                    showMessage(" - evaluating rounds...");
                    List<EvaluationResult> roundResults = evaluateRound(
                            dataset, round, trainData, testData);
                    results.addAll(roundResults);

                    trainDataReader.close();
                    testDataReader.close();
                } catch (Exception e) {
                    throw new EvaluationException(e);
                }
            }

            showMessage("");
        }

        return results;
    }


    private void showMessage(String msg) {
        if (verbose)
            System.out.println(msg);
    }
}
