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
import nda.analysis.Setup;
import nda.analysis.evaluation.EvaluationException;
import nda.analysis.evaluation.EvaluationResult;
import nda.analysis.evaluation.EvaluatorSetup;
import nda.analysis.evaluation.SimpleEvaluator;


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
                Setup gen_setup = new Setup(gen_setup_path);
                EvaluatorSetup eva_setup = new EvaluatorSetup(eva_setup_path);

                SimpleEvaluator evaluator = new SimpleEvaluator(gen_setup, eva_setup);
                evaluator.setVerbose(cml.hasOption('v'));

                List<EvaluationResult> results = evaluator.evaluate();

                for (EvaluationResult result : results)
                    System.out.println(result);
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
