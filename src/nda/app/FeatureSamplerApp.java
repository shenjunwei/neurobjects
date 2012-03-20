package nda.app;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;

import nda.analysis.InvalidSetupFileException;
import nda.analysis.statistics.FeatureSampler;
import nda.analysis.statistics.FeatureSamplerSetup;


/**
 * @author Giuliano Vilela
 */
public class FeatureSamplerApp {
    private static Options options;

    static {
        options = new Options();
        options.addOption("h", "help", false, "print help information");
        options.addOption("v", "verbose", false, "output progress information to stdout");
    }


    private static void usage() {
        HelpFormatter help = new HelpFormatter();

        help.printHelp(
                "java -jar feature-sampler.jar [OPTIONS] sampler_setup",
                options);

        System.exit(1);
    }


    private static FeatureSampler getSampler(FeatureSamplerSetup setup) {
        return new FeatureSampler(setup);
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

        String setup_path = filepaths[0];

        try {
            FeatureSamplerSetup setup = new FeatureSamplerSetup(setup_path);
            FeatureSampler sampler = getSampler(setup);

            FileWriter scriptFile = new FileWriter(setup.getOutputFile());

            sampler.setVerbose(cml.hasOption('v'));
            Map<String,Map<String,double[]>> samples = sampler.extractFeatures();

            @SuppressWarnings("unchecked")
            List<String> behaviors = (List<String>) setup.getParams().get("labels");

            for (String neuron : samples.keySet()) {
                scriptFile.append("insert into `results` values ");

                List<String> row = new ArrayList<String>();
                /* id */
                row.add("0");
                /* subject */
                row.add(setup.getName());
                /* neuron */
                row.add(neuron);
                /* bin_size */
                if (setup.getParams().get("bin_size") != null)
                    row.add("" + setup.getParams().get("bin_size"));
                else
                    row.add(null);
                /* feature */
                row.add(setup.getFeature());

                /* _samples */
                for (String behavior : behaviors) {
                    double[] behavior_samples = samples.get(neuron).get(behavior);
                    row.add(ArrayUtils.toString(behavior_samples));
                }

                /* wilcoxon_p */
                row.add(null);

                scriptFile.append(toSqlRow(row) + ";\n");
            }

            scriptFile.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Setup file doesn't exist: " + e.getMessage());
            System.exit(3);
        }
        catch (InvalidSetupFileException e) {
            System.out.println("Setup file is invalid: " + e.getMessage());
            System.exit(2);
        }
        catch (Exception e) {
            System.out.println(
                    "An error ocurred when evaluating the dataset files"
                    + " for file " + setup_path);
            e.printStackTrace();
            System.exit(1);
        }

        if (cml.hasOption('v'))
            System.out.println("Done! Exiting...");

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

}
