package nda.analysis.evaluation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Evaluation;

import nda.analysis.generation.GeneratorSetup;


/**
 * Generate reports from an evaluation in the form of a SQL script
 * that can be directly input into a DBMS.
 * 
 * @author Giuliano Vilela
 */
public class SQLScriptReport implements EvaluationReportI {
    private String outputFilePath;
    private String tableName;

    public SQLScriptReport(EvaluatorSetup setup) {
        outputFilePath = (String) setup.getReportParameter("file");
        tableName = (String) setup.getReportParameter("table");
    }


    /**
     * @see nda.analysis.evaluation.EvaluationReportI#makeReport(java.util.List)
     */
    @Override
    public void makeReport(List<EvaluationResult> results) throws EvaluationException {
        FileWriter scriptFile;

        try {
            scriptFile = new FileWriter(outputFilePath);
        } catch (IOException e) {
            throw new EvaluationException("Can't open file " + outputFilePath, e);
        }

        try {
            scriptFile.append("delete from " + escapeId(tableName) + ";\n");

            GeneratorSetup.Dataset sampleDataset = results.get(0).getDataset();
            int numClasses = sampleDataset.getClasses().size();

            // One result for each row of the table
            for (EvaluationResult result : results) {
                GeneratorSetup.Dataset dataset = result.getDataset();

                String trainSetName = result.getTrainSetName();
                String positiveLabel = getPositiveLabel(trainSetName);
                String roundNumberStr = getRoundNumber(trainSetName);

                for (int i = 0; i < result.numEvaluations(); ++i) {
                    NamedClassifier n_classifier = result.getClassifiers().get(i);
                    Evaluation evaluation = result.getModelEvaluations().get(i);

                    scriptFile.append("insert into " + escapeId(tableName) + " values ");

                    List<String> row = new ArrayList<String>();

                    /* id */
                    row.add("auto");
                    /* area */
                    row.add(result.getParameter("areas").toString());
                    /* animal */
                    row.add(dataset.getSetup().getName());
                    /* object */
                    row.add(positiveLabel);
                    /* round */
                    row.add(roundNumberStr);
                    /* classifier */
                    row.add(n_classifier.getName());
                    /* num_instances */
                    row.add("" + (int) evaluation.numInstances());
                    /* correct */
                    row.add("" + (int) evaluation.correct());
                    /* bin_size */
                    row.add("" + (int) (1000*(Double)result.getParameter("bin_size")));
                    /* window_size */
                    row.add("" + result.getParameter("window_width"));
                    /* auroc */
                    row.add("" + evaluation.weightedAreaUnderROC());
                    /* kappa */
                    row.add("" + evaluation.kappa());

                    for (int j = 0; j < numClasses; ++j) {
                        /* _fmeasure */ row.add("" + evaluation.fMeasure(j));
                        /* _fp */ row.add("" + (int) evaluation.numFalsePositives(j));
                        /* _fn */ row.add("" + (int) evaluation.numFalseNegatives(j));
                    }

                    scriptFile.append(toSqlRow(row) + ";\n");
                }
            }

            scriptFile.close();
        } catch (IOException e) {
            throw new EvaluationException("Can't write sql script", e);
        }
    }


    /**
     * TODO this is horribly wrong. we should downgrade apache
     * commons to use StringEscapeUtils.escapeSql. But it is good enough for now.
     */
    private static String escapeId(String str) {
        return "`" + str.replace("'", "''") + "`";
    }


    private static String escapeValue(String str) {
        return "'" + str.replace("'", "''") + "'";
    }


    private static String toSqlRow(List<String> values) {
        String row = "(";

        for (int i = 0; i < values.size(); ++i) {
            if (i > 0) row += ", ";
            row += escapeValue(values.get(i));
        }

        row += ")";
        return row;
    }

    private static String getRoundNumber(String trainSetName) {
        int last_underscore = trainSetName.lastIndexOf('_');
        int p_underscore = trainSetName.lastIndexOf('_', last_underscore - 1);

        String roundString = trainSetName.substring(p_underscore+2, last_underscore);
        return roundString;
    }


    private static String getPositiveLabel(String trainSetName) {
        int start = trainSetName.indexOf('_') + 1;
        int end = trainSetName.indexOf('_', start);
        return trainSetName.substring(start, end);
    }
}
