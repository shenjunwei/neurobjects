package nda.analysis.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import nda.analysis.InvalidSetupFileException;
import nda.analysis.PatternHandler;


/**
 * 
 * @author Giuliano Vilela
 */
public class SimpleGenerator extends DatasetGenerator {

    public SimpleGenerator(String setupFilepath)
    throws FileNotFoundException, InvalidSetupFileException {
        super(setupFilepath);
        setVerbose(false);
    }


    public SimpleGenerator(GeneratorSetup setup) {
        super(setup);
        setVerbose(false);
    }


    /**
     * Create Weka ARFF files in the output directory.
     * 
     * @see nda.analysis.generation.DatasetGenerator#generate()
     */
    @Override
    public void generate() throws GenerationException {
        showMessage("Generating datasets...\n");

        showMessage("Reading spike data and behavior file...");
        loadHandlers();

        File outputDir = new File(setup.getOutputDirectory());
        if (!outputDir.exists() && !outputDir.mkdir()) {
            throw new GenerationException("Cant create dir: " + outputDir);
        }

        for (GeneratorSetup.Dataset dataset : setup.getDatasets()) {
            showMessage("Building dataset " + dataset.getName() + " ...");

            showMessage(" - extracting patterns...");
            List<PatternHandler> list = buildDataset(dataset);

            for (PatternHandler patternHandler : list) {
                String weka_str = patternHandler.toWekaFormat();
                String file_str = formatWekaStr(weka_str);
                String filename = patternHandler.getRelation().relationName() + ".arff";

                try {
                    File file = new File(outputDir, filename);
                    showMessage(" - writing " + file.getPath() + " ...");

                    FileWriter out = new FileWriter(file);
                    out.write(file_str);
                    out.close();
                } catch (IOException e) {
                    throw new GenerationException(e);
                }
            }

            showMessage("");
        }
    }


    private void showMessage(String msg) {
        if (verbose)
            System.out.println(msg);
    }
}
