package nda.app;

import java.io.FileNotFoundException;
import java.util.List;

import nda.analysis.EvaluationException;
import nda.analysis.EvaluationResult;
import nda.analysis.InvalidSetupFileException;
import nda.analysis.SimpleEvaluator;


/**
 * SimpleEvaluatorApp
 * 
 * @author Giuliano Vilela
 */
public class SimpleEvaluatorApp {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar simple-evaluator.jar [-q] setup_files...");
            System.exit(1);
        }

        int args_p = 0;

        boolean verbose = true;
        if (args[0].equals("-q")) {
            verbose = false;
            args_p = 1;
        }

        for (int i = args_p; i < args.length; ++i) {
            String setupFilepath = args[i];

            try {
                SimpleEvaluator evaluator = new SimpleEvaluator(setupFilepath);
                evaluator.setVerbose(verbose);

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

        if (verbose)
            System.out.println("Done! Exiting...");
    }
}
