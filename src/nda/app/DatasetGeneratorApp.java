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
import nda.analysis.generation.DatasetGenerator;
import nda.analysis.generation.GenerationException;
import nda.analysis.generation.GeneratorSetup;
import nda.analysis.generation.SimpleGenerator;
import nda.analysis.generation.SimpleParallelGenerator;


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


    private static DatasetGenerator getGenerator(GeneratorSetup setup, CommandLine cml) {
        DatasetGenerator generator;

        if (cml.hasOption("parallel"))
            generator = new SimpleParallelGenerator(setup);
        else
            generator = new SimpleGenerator(setup);

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
                GeneratorSetup setup = new GeneratorSetup(setupFilepath);
                DatasetGenerator generator = getGenerator(setup, cml);
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
            catch (GenerationException e) {
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
