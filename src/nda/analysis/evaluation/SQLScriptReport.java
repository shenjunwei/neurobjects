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
    private EvaluatorSetup setup;
    private String outputFilePath;
    private String tableName;
    private boolean truncateTable;

    public SQLScriptReport(EvaluatorSetup _setup) {
        setup = _setup;
        outputFilePath = (String) setup.getReportParameter("file");
        tableName = (String) setup.getReportParameter("table");
        truncateTable = setup.getReportParameter("truncate_table") != null;
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
            if (truncateTable)
                scriptFile.append("delete from " + escapeId(tableName) + ";\n");

            GeneratorSetup.Dataset sampleDataset = results.get(0).dataset;
            int numClasses = sampleDataset.getClasses().size();

            // One result for each row of the table
            for (EvaluationResult result : results) {
                GeneratorSetup.Dataset dataset = result.dataset;

                String trainSetName = result.trainSetName;
                String positiveLabel = getPositiveLabel(trainSetName);

                NamedClassifier n_classifier = result.classifier;
                Evaluation evaluation = result.evaluation;

                scriptFile.append("insert into " + escapeId(tableName) + " values ");

                List<String> row = new ArrayList<String>();

                /* id */
                row.add("0");
                /* subject */
                row.add(dataset.getSetup().getName());
                /* neurons */
                row.add(result.getParameter("areas").toString());
                /* object */
                row.add(positiveLabel);
                /* round */
                row.add("" + result.roundNumber);
                /* cv_fold */
                if (setup.isCrossValidation())
                    row.add("" + result.cvFoldNumber);
                else
                    row.add(null);
                /* classifier */
                row.add(n_classifier.getName());
                /* bin_size */
                row.add("" + (int) (1000*(Double)result.getParameter("bin_size")));
                /* window_size */
                row.add("" + result.getParameter("window_width"));

                /* neuron_drop */
                if (result.getParameter("neuron_drop") != null)
                    row.add("" + result.getParameter("num_drop"));
                else
                    row.add(null);

                /* surrogate, num_surrogate, pct_surrogate, dist_surrogate */
                if (result.getParameter("surrogate") != null) {
                    String type = (String) result.getParameter("surrogate_type");
                    row.add("" + type);

                    if (type.equals("uniform") || type.equals("poisson") ||
                            type.equals("contact_split") || type.equals("exposition_split") ||
                            type.equals("label_split")) {
                        row.add("" + result.getParameter("num_surrogate"));
                        row.add(null);
                        row.add(null);
                    }
                    else if (type.equals("col_swap") || type.equals("matrix_swap") ||
                            type.equals("contact_swap")) {
                        row.add(null);
                        row.add("" + result.getParameter("pct_surrogate"));
                        row.add(null);
                    }
                    else if (type.equals("neuron_swap")) {
                        row.add("" + result.getParameter("num_surrogate"));
                        row.add("" + result.getParameter("pct_surrogate"));
                        row.add(null);
                    }
                    else if (type.equals("col_swap_d")) {
                        row.add(null);
                        row.add("" + result.getParameter("pct_surrogate"));
                        row.add("" + result.getParameter("dist_surrogate"));
                    }
                    else if (type.equals("poisson_d") || type.equals("uniform_d") ||
                            type.equals("spike_jitter") || type.equals("mean_d") ||
                            type.equals("contact_shift")) {
                        row.add(null);
                        row.add(null);
                        row.add("" + result.getParameter("dist_surrogate"));
                    }
                    else if (type.equals("var_contacts")) {
                        int method;

                        if(result.getParameter("method_surrogate").equals("ab")) method = 1;
                        else if(result.getParameter("method_surrogate").equals("a")) method = 2;
                        else method = 3;

                        row.add("" + method);
                        row.add(null);
                        row.add("" + result.getParameter("val_surrogate"));
                    }
                }
                else {
                    row.add(null);
                    row.add(null);
                    row.add(null);
                    row.add(null);
                }

                /* num_instances */
                row.add("" + (int) evaluation.numInstances());
                /* correct */
                row.add("" + (int) evaluation.correct());
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
        if (str != null)
            return "'" + str.replace("'", "''") + "'";
        else
            return "null";
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


    private static String getPositiveLabel(String trainSetName) {
        int start = trainSetName.indexOf('_') + 1;
        int end = trainSetName.indexOf('_', start);
        return trainSetName.substring(start, end);
    }
}
