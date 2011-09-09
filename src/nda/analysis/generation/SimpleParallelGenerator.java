package nda.analysis.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import nda.analysis.InvalidSetupFileException;
import nda.analysis.PatternHandler;


/**
 * Simple implementation of ParallelDatasetGenerator
 * 
 * @author Giuliano Vilela
 */
public class SimpleParallelGenerator extends ParallelGenerator {
    private boolean verbose;


    public SimpleParallelGenerator(String setupFilepath)
    throws FileNotFoundException, InvalidSetupFileException {
        super(setupFilepath);
        setVerbose(false);
    }


    public SimpleParallelGenerator(GeneratorSetup _setup) {
        super(_setup);
        setVerbose(false);
    }


    @Override
    public void setVerbose(boolean _verbose) {
        verbose = _verbose;
    }


    @Override
    public void generate() throws GenerationException {
        showMessage("Generating datasets...\n");

        showMessage("Reading spike data and behavior file...");
        loadHandlers();

        File outputDir = new File(setup.getOutputDirectory());
        if (!outputDir.exists() && !outputDir.mkdir()) {
            throw new GenerationException("Cant create dir: " + outputDir);
        }

        showMessage("Firing generation tasks...");
        List<Future<List<PatternHandler>>> results = buildAll(setup);

        for (Future<List<PatternHandler>> futureResult : results) {
            try {
                // Block until the next task finishes
                List<PatternHandler> result = futureResult.get();

                for (PatternHandler data : result)
                    writeFile(data, outputDir);

                showMessage("");
            }
            catch (InterruptedException e) {
                showMessage("Main thread was interrupted: " + e.getMessage() + "...");
                break;
            }
            catch (ExecutionException e) {
                throw new GenerationException(e.getCause());
            }
        }
    }


    protected void writeFile(PatternHandler data, File outputDir)
    throws GenerationException {

        String weka_str = data.toWekaFormat();
        String file_str = formatWekaStr(weka_str);
        String filename = data.getRelation().relationName() + ".arff";

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


    private void showMessage(String msg) {
        if (verbose)
            System.out.println(msg);
    }
}
