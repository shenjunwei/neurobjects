package nda.analysis.generation;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import nda.analysis.InvalidSetupFileException;
import nda.analysis.PatternHandler;


/**
 * AbstractParallelDatasetGenerator
 * 
 * @author Giuliano Vilela
 */
public abstract class ParallelGenerator extends DatasetGenerator {

    public ParallelGenerator(String setupFilepath)
    throws FileNotFoundException, InvalidSetupFileException {
        super(setupFilepath);
    }


    public ParallelGenerator(GeneratorSetup _setup) {
        super(_setup);
    }


    @Override
    protected void loadHandlers() throws GenerationException {
        super.loadHandlers();
        globalCountMatrixCache = Collections.synchronizedMap(globalCountMatrixCache);
    }


    protected ExecutorService buildExecutor() {
        int numCores = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(numCores);
    }


    protected List<Future<List<PatternHandler>>> buildAll(GeneratorSetup setup)
    throws GenerationException {

        ExecutorService executor = buildExecutor();

        List<Future<List<PatternHandler>>> results =
            new ArrayList<Future<List<PatternHandler>>>();

        for (Callable<List<PatternHandler>> task : buildTasks(setup)) {
            Future<List<PatternHandler>> result = executor.submit(task);
            results.add(result);
        }

        executor.shutdown();
        return results;
    }


    protected List<Callable<List<PatternHandler>>> buildTasks(GeneratorSetup setup) {
        int estimate = setup.getDatasets().size() * 30;

        List<Callable<List<PatternHandler>>> tasks =
            new ArrayList<Callable<List<PatternHandler>>>(estimate);

        for (GeneratorSetup.Dataset dataset : setup.getDatasets()) {
            GeneratorTask task = new GeneratorTask(dataset);
            tasks.add(task);
        }

        return tasks;
    }


    protected class GeneratorTask implements Callable<List<PatternHandler>> {
        private GeneratorSetup.Dataset dataset;

        public GeneratorTask(GeneratorSetup.Dataset _dataset) {
            dataset = _dataset;
        }

        @Override
        public List<PatternHandler> call() throws GenerationException {
            return buildDataset(dataset);
        }
    }
}
