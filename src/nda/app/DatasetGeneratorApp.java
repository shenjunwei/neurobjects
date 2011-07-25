package nda.app;

import java.io.FileNotFoundException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import nda.analysis.InvalidSetupFileException;
import nda.analysis.Setup;
import nda.analysis.generation.AbstractDatasetGenerator;
import nda.analysis.generation.DatasetGenerationException;
import nda.analysis.generation.SimpleDatasetGenerator;
import nda.analysis.generation.SimpleParallelDatasetGenerator;


/**
 * DatasetGeneratorApp
 * 
 * @author Giuliano Vilela
 */
public class DatasetGeneratorApp {
    private static Options options;

    static {
        options = new Options();
        options.addOption("h", "help", false, "print help information");
        options.addOption("v", "verbose", false, "output progress information to stdout");

        OptionGroup modeOption = new OptionGroup();
        modeOption.setRequired(false);
        modeOption.addOption(new Option("simple", "simple single threaded generator"));
        modeOption.addOption(new Option("parallel", "parallel dataset generator"));

        options.addOptionGroup(modeOption);
    }


    private static void usage() {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("java -jar dataset-generator.jar [OPTIONS] setup [setup...]", options);
        System.exit(1);
    }


    private static AbstractDatasetGenerator getGenerator(Setup setup, CommandLine cml) {
        AbstractDatasetGenerator generator;

        if (cml.hasOption("parallel"))
            generator = new SimpleParallelDatasetGenerator(setup);
        else
            generator = new SimpleDatasetGenerator(setup);

        if (cml.hasOption("verbose"))
            generator.setVerbose(true);
        else
            generator.setVerbose(false);

        return generator;
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
                Setup setup = new Setup(setupFilepath);
                AbstractDatasetGenerator generator = getGenerator(setup, cml);
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

        System.out.println("Done! Exiting...");
    }
}
