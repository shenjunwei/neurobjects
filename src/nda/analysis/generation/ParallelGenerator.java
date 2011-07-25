package nda.analysis.generation;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import nda.analysis.InvalidSetupFileException;
import nda.analysis.PatternHandler;
import nda.analysis.Setup;


/**
 * AbstractParallelDatasetGenerator
 * 
 * @author Giuliano Vilela
 */
public abstract class ParallelGenerator extends DatasetGenerator {
    private ExecutorService executor;

    public ParallelGenerator(String setupFilepath)
    throws FileNotFoundException, InvalidSetupFileException {
        super(setupFilepath);
        init();
    }


    public ParallelGenerator(Setup _setup) {
        super(_setup);
        init();
    }


    protected void init() {
        executor = null;
    }


    public void setExecutor(ExecutorService ex) {
        executor = ex;
    }


    protected ExecutorService getExecutor() {
        if (executor == null) {
            int numCores = Runtime.getRuntime().availableProcessors();
            executor = Executors.newFixedThreadPool(numCores);
        }

        return executor;
    }


    protected List<Future<List<PatternHandler>>> buildAll(Setup setup)
    throws GenerationException {
        ExecutorService executor = getExecutor();

        List<Future<List<PatternHandler>>> results =
            new ArrayList<Future<List<PatternHandler>>>();

        for (Callable<List<PatternHandler>> task : buildTasks(setup)) {
            Future<List<PatternHandler>> result = executor.submit(task);
            results.add(result);
        }

        executor.shutdown();
        return results;
    }


    protected List<Callable<List<PatternHandler>>> buildTasks(Setup setup) {
        int estimate = setup.getDatasets().size() * 6;

        List<Callable<List<PatternHandler>>> tasks =
            new ArrayList<Callable<List<PatternHandler>>>(estimate);

        for (Setup.Dataset dataset : setup.getDatasets()) {
            for (int round = 1; round <= dataset.getNumberRounds(); ++round) {
                GeneratorTask task = new GeneratorTask(dataset, round);
                tasks.add(task);
            }
        }

        return tasks;
    }


    protected class GeneratorTask implements Callable<List<PatternHandler>> {
        private Setup.Dataset dataset;
        private int round;

        public GeneratorTask(Setup.Dataset _dataset, int _round) {
            dataset = _dataset;
            round = _round;
        }

        @Override
        public List<PatternHandler> call() throws GenerationException {
            return buildDatasetSingleRound(dataset, round);
        }
    }
}
