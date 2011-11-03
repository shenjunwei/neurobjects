package nda.analysis.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import weka.core.Instances;

import nda.analysis.PatternHandler;
import nda.analysis.generation.DatasetGenerator;
import nda.analysis.generation.GenerationException;
import nda.analysis.generation.GeneratorSetup;
import nda.analysis.generation.SimpleParallelGenerator;
import nda.util.Verbose;


/**
 * @author Giuliano Vilela
 */
public class FastDatasetEvaluator implements Verbose {
    protected boolean verbose;

    protected GeneratorSetup generatorSetup;
    protected EvaluatorSetup evaluatorSetup;

    protected DatasetGenerator generator;
    protected DatasetEvaluator evaluator;


    public FastDatasetEvaluator(GeneratorSetup gen_setup, EvaluatorSetup eva_setup) {
        generatorSetup = gen_setup;
        evaluatorSetup = eva_setup;

        generator = new SimpleParallelGenerator(gen_setup);
        evaluator = new SimpleEvaluator(gen_setup, eva_setup);
    }


    public List<EvaluationResult> run() throws GenerationException, EvaluationException {
        showMessage("Evaluating Datasets...\n");

        showMessage("Reading experiment data...");
        generator.loadHandlers();

        showMessage("Firing generator tasks...");
        List<Future<List<EvaluationResult>>> futureResults = buildAll(generatorSetup);

        showMessage("Gathering results...");
        List<EvaluationResult> results = new ArrayList<EvaluationResult>(
                futureResults.size()*30);

        for (Future<List<EvaluationResult>> future : futureResults) {
            try {
                // Block until the next task finishes
                List<EvaluationResult> datasetResults = future.get();
                results.addAll(datasetResults);

                GeneratorSetup.Dataset dataset = datasetResults.get(0).dataset;
                showMessage(" - " + dataset.getName());
            }
            catch (InterruptedException e) {
                showMessage("Main thread was interrupted: " + e.getMessage() + "...");
                break;
            }
            catch (ExecutionException e) {
                throw new EvaluationException(e.getCause());
            }
        }

        return results;
    }


    protected List<Future<List<EvaluationResult>>> buildAll(GeneratorSetup setup) {
        ExecutorService executor = buildExecutor();

        List<Future<List<EvaluationResult>>> results =
            new ArrayList<Future<List<EvaluationResult>>>();

        for (Callable<List<EvaluationResult>> task : buildTasks(setup)) {
            Future<List<EvaluationResult>> result = executor.submit(task);
            results.add(result);
        }

        executor.shutdown();
        return results;
    }


    protected List<Callable<List<EvaluationResult>>> buildTasks(GeneratorSetup setup) {
        int estimate = setup.getDatasets().size() * 30;

        List<Callable<List<EvaluationResult>>> tasks =
            new ArrayList<Callable<List<EvaluationResult>>>(estimate);

        for (GeneratorSetup.Dataset dataset : setup.getDatasets()) {
            FastEvaluatorTask task = new FastEvaluatorTask(dataset);
            tasks.add(task);
        }

        return tasks;
    }


    protected class FastEvaluatorTask implements Callable<List<EvaluationResult>> {
        private GeneratorSetup.Dataset dataset;

        public FastEvaluatorTask(GeneratorSetup.Dataset _dataset) {
            dataset = _dataset;
        }

        @Override
        public List<EvaluationResult> call() throws GenerationException, EvaluationException {
            // Generate
            List<PatternHandler> handlers = generator.buildDataset(dataset);

            // Evaluate
            List<EvaluationResult> datasetResults =
                new ArrayList<EvaluationResult>(handlers.size());

            for (int i = 0; i < handlers.size()-1; i += 2) {
                int round = i/2 + 1;
                Instances trainSet = handlers.get(i).getRelation();
                Instances testSet = handlers.get(i+1).getRelation();

                List<EvaluationResult> results = evaluator.evaluateTrainTest(
                        dataset, round, trainSet, testSet);

                datasetResults.addAll(results);
            }

            return datasetResults;
        }
    }


    protected ExecutorService buildExecutor() {
        int numCores = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(numCores);
    }


    protected void showMessage(String msg) {
        if (verbose)
            System.out.println(msg);
    }


    /* (non-Javadoc)
     * @see nda.util.Verbose#setVerbose(boolean)
     */
    @Override
    public void setVerbose(boolean _verbose) {
        verbose = _verbose;
    }


    /* (non-Javadoc)
     * @see nda.util.Verbose#getVerbose()
     */
    @Override
    public boolean getVerbose() {
        return verbose;
    }
}
