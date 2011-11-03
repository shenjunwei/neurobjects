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
    private static final String testDirpath = "data/test/train_test_eval";
    private static final String genSetupFilepath = testDirpath + "/setup.yml";
    private static final String evalSetupFilepath = testDirpath + "/evaluator.yml";

    private SimpleEvaluator evaluator;

    // Integrity hashes
    private static final byte[] HASH_TRAIN_TEST = {-29,92,11,29,-21,-86,-120,-3,-85,87,-108,47,37,-12,-53,51};


    @Before
    public void setUp() throws Exception {
        GeneratorSetup generatorSetup = new GeneratorSetup(genSetupFilepath);
        EvaluatorSetup evaluatorSetup = new EvaluatorSetup(evalSetupFilepath);
        evaluator = new SimpleEvaluator(generatorSetup, evaluatorSetup);
    }


    /**
     * Test method for {@link nda.analysis.evaluation.SimpleEvaluator#evaluate()}.
     */
    @Test
    public void testEvaluate() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        List<EvaluationResult> results = evaluator.evaluate();
        for (EvaluationResult result : results) {
            Evaluation evaluation = result.evaluation;
            digest.update(evaluation.toSummaryString().getBytes("UTF-8"));
        }

        byte[] hash = digest.digest();
        assertTrue(ArrayUtils.equals(HASH_TRAIN_TEST, hash));
    }
}
