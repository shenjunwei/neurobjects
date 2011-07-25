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
import nda.analysis.evaluation.EvaluationException;
import nda.analysis.evaluation.EvaluationResult;
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
        help.printHelp("java -jar dataset-evaluator.jar [OPTIONS] setup [setup...]", options);
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

        for (String setupFilepath : cml.getArgs()) {
            try {
                SimpleEvaluator evaluator = new SimpleEvaluator(setupFilepath);
                evaluator.setVerbose(cml.hasOption('v'));

                List<EvaluationResult> results = evaluator.evaluate();

                for (EvaluationResult result : results)
                    System.out.println(result);
            }
            catch (FileNotFoundException e) {
                System.out.println("File " + setupFilepath + " doesn't exist");
                System.out.println(e.getMessage());
            }
            catch (InvalidSetupFileException e) {
                System.out.println("The setup file " + setupFilepath + " is invalid.");
                System.out.println(e.getMessage());
            }
            catch (EvaluationException e) {
                System.out.println(
                        "An error ocurred when evaluating the dataset files"
                        + " for file " + setupFilepath);
                System.out.println(e.getMessage());
            }
        }

        if (cml.hasOption('v'))
            System.out.println("Done! Exiting...");
    }
}
