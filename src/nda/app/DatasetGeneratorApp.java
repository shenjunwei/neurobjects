package nda.app;

import java.io.FileNotFoundException;

import nda.analysis.DatasetGenerationException;
import nda.analysis.InvalidSetupFileException;
import nda.analysis.text.TextDatasetGenerator;


/**
 * DatasetGeneratorApp
 * 
 * @author Giuliano Vilela
 */
public class DatasetGeneratorApp {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar dataset-generator.jar [-q] setup_files...");
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
                TextDatasetGenerator generator = new TextDatasetGenerator(setupFilepath);
                generator.setVerbose(verbose);
                generator.generate();
            }
            catch (FileNotFoundException e) {
                System.out.println("File " + setupFilepath + " doesn't exist");
                System.out.println(e.getMessage());
            }
            catch (InvalidSetupFileException e) {
                System.out.println("The setup file " + setupFilepath + " is invalid.");
                System.out.println(e.getMessage());
            }
            catch (DatasetGenerationException e) {
                System.out.println(
                        "An error ocurred when generating the dataset files"
                        + " for file " + setupFilepath + " (some files may "
                        + "have been created).");
                System.out.println(e.getMessage());
            }
        }
    }
}
