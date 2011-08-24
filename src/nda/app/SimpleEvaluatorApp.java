package nda.app;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import nda.analysis.InvalidSetupFileException;
import nda.analysis.evaluation.CSVReport;
import nda.analysis.evaluation.EvaluationException;
import nda.analysis.evaluation.EvaluationReportI;
import nda.analysis.evaluation.EvaluationResult;
import nda.analysis.evaluation.EvaluatorSetup;
import nda.analysis.evaluation.SQLScriptReport;
import nda.analysis.evaluation.SimpleEvaluator;
import nda.analysis.generation.GeneratorSetup;


/**
 * SimpleEvaluatorApp
 * 
 * @author Giuliano Vilela
 */
public class SimpleEvaluatorApp {
    private static Options options;

    static {
        options = new Options();
        options.addOption("h", "help", false, "print help information");
        options.addOption("v", "verbose", false, "output progress information to stdout");
    }


    private static void usage() {
        HelpFormatter help = new HelpFormatter();

        help.printHelp(
                "java -jar dataset-evaluator.jar [OPTIONS] "
                + "generator_setup evaluator_setup [generator_setup evaluator_setup...]",
                options);

        System.exit(1);
    }


    private static EvaluationReportI getReport(EvaluatorSetup setup)
    throws InvalidSetupFileException {

        String reportType = setup.getReportType();

        if (reportType.equals("csv")) {
            return new CSVReport(setup);
        }
        else if (reportType.equals("sql_script")) {
            return new SQLScriptReport(setup);
        }
        else {
            throw new InvalidSetupFileException("Invalid report type: " + reportType);
        }
    }


    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();
        CommandLine cml = null;

        try {
            cml = parser.parse(options, args, true);
        }
        catch (ParseException e1) {
            usage();
        }

        if (cml.getArgs().length == 0 || cml.hasOption("h"))
            usage();

        String[] filepaths = cml.getArgs();

        for (int i = 0; i < filepaths.length-1; i += 2) {
            String gen_setup_path = filepaths[i];
            String eva_setup_path = filepaths[i+1];

            try {
                GeneratorSetup gen_setup = new GeneratorSetup(gen_setup_path);
                EvaluatorSetup eva_setup = new EvaluatorSetup(eva_setup_path);

                SimpleEvaluator evaluator = new SimpleEvaluator(gen_setup, eva_setup);
                evaluator.setVerbose(cml.hasOption('v'));

                List<EvaluationResult> results = evaluator.evaluate();

                if (cml.hasOption('v'))
                    System.out.println("Writing report...");

                EvaluationReportI report = getReport(eva_setup);
                report.makeReport(results);
            }
            catch (FileNotFoundException e) {
                System.out.println("Setup file doesn't exist: " + e.getMessage());
            }
            catch (InvalidSetupFileException e) {
                System.out.println("Setup file is invalid: " + e.getMessage());
            }
            catch (EvaluationException e) {
                System.out.println(
                        "An error ocurred when evaluating the dataset files"
                        + " for file " + eva_setup_path);
                System.out.println(e.getMessage());
            }
        }

        if (cml.hasOption('v'))
            System.out.println("Done! Exiting...");
    }
}
