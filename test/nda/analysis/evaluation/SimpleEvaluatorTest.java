package nda.analysis.evaluation;

import static org.junit.Assert.assertTrue;

import java.security.MessageDigest;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.Evaluation;

import nda.analysis.generation.GeneratorSetup;
import nda.util.ArrayUtils;


/**
 * Tests for the SimpleEvaluator class.
 * 
 * @author Giuliano Vilela
 */
public class SimpleEvaluatorTest {
    // Make the test reproducible
    private static long RANDOM_SEED = -1928057635574070491L;

    // Uncomment the following block to test with a new seed
    /*static {
        RANDOM_SEED = new Random().nextLong();
        System.out.println("RANDOM_SEED = " + RANDOM_SEED);
    }*/


    private static final String testDirpath = "data/test/train_test_eval";
    private static final String genSetupFilepath = testDirpath + "/setup.yml";
    private static final String evalSetupFilepath = testDirpath + "/evaluator.yml";
    private static final String cvEvalSetupFilepath = testDirpath + "/cv_evaluator.yml";

    private SimpleEvaluator evaluator;
    private SimpleEvaluator cv_evaluator;

    // Integrity hashes. Should be modified when RANDOM_SEED changes
    private static final byte[] HASH_TRAIN_TEST = {-29,92,11,29,-21,-86,-120,-3,-85,87,-108,47,37,-12,-53,51};
    private static final byte[] HASH_CROSS_VALIDATION = {-56,-12,120,-110,-121,102,85,28,127,16,-34,13,121,-68,-121,76};


    @Before
    public void setUp() throws Exception {
        GeneratorSetup generatorSetup = new GeneratorSetup(genSetupFilepath);

        EvaluatorSetup evaluatorSetup = new EvaluatorSetup(evalSetupFilepath);
        evaluator = new SimpleEvaluator(generatorSetup, evaluatorSetup);

        EvaluatorSetup cvEvaluatorSetup = new EvaluatorSetup(cvEvalSetupFilepath);
        cv_evaluator = new SimpleEvaluator(generatorSetup, cvEvaluatorSetup);
        cv_evaluator.reSeed(RANDOM_SEED);
    }


    /**
     * Test method for {@link nda.analysis.evaluation.SimpleEvaluator#evaluate()}.
     */
    @Test
    public void testEvaluateTrainTest() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        List<EvaluationResult> results = evaluator.evaluate();
        for (EvaluationResult result : results) {
            Evaluation evaluation = result.evaluation;
            digest.update(evaluation.toSummaryString().getBytes("UTF-8"));
        }

        byte[] hash = digest.digest();
        assertTrue(ArrayUtils.equals(HASH_TRAIN_TEST, hash));
    }


    @Test
    public void testEvaluateCrossValidation() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        List<EvaluationResult> results = cv_evaluator.evaluate();
        for (EvaluationResult result : results) {
            Evaluation evaluation = result.evaluation;
            digest.update(evaluation.toSummaryString().getBytes("UTF-8"));
        }

        byte[] hash = digest.digest();
        assertTrue(ArrayUtils.equals(HASH_CROSS_VALIDATION, hash));
    }
}
