package nda.analysis.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.Classifier;


/**
 * Tests for the EvaluatorSetup class.
 *
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class EvaluatorSetupTest {
    private EvaluatorSetup setup;
    private static final String setupFilePath = "data/test/test_evaluator_setup.yml";


    @Before
    public void setUp() throws Exception {
        setup = new EvaluatorSetup(setupFilePath);
    }


    /**
     * Test method for {@link nda.analysis.evaluation.EvaluatorSetup#toString()}.
     */
    @Test
    public void testToString() {
        assertFalse(setup.toString().isEmpty());
    }


    /**
     * Test method for {@link nda.analysis.evaluation.EvaluatorSetup#getReportType()}.
     */
    @Test
    public void testGetReportType() {
        assertEquals("csv", setup.getReportType());
    }


    /**
     * Test method for {@link nda.analysis.evaluation.EvaluatorSetup#getReportParameter(String)}.
     */
    @Test
    public void testGetReportParameter() {
        assertEquals("data/real/results.csv", setup.getReportParameter("file"));
    }


    @Test
    public void testEvaluationType() throws Exception {
        String cvSetupFilepath = "data/test/test_evaluator_cv.yml";
        EvaluatorSetup cv_setup = new EvaluatorSetup(cvSetupFilepath);

        assertEquals(EvaluatorSetup.TRAIN_TEST, setup.getEvaluationType());
        assertEquals(EvaluatorSetup.CROSS_VALIDATION, cv_setup.getEvaluationType());

        assertFalse(setup.isCrossValidation());
        assertTrue(cv_setup.isCrossValidation());
    }


    /**
     * Test method for {@link nda.analysis.evaluation.EvaluatorSetup#getClassifiers()}.
     */
    @Test
    public void testGetClassifiers() {
        List<NamedClassifier> classifiers = setup.getClassifiers();

        assertEquals(23, classifiers.size());
        assertEquals("weka.classifiers.bayes.NaiveBayes", classifiers.get(0).getName());
        assertEquals("bayes.NaiveBayes", classifiers.get(1).getName());
        assertEquals("Arvore#2", classifiers.get(4).getName());
        assertEquals("Arvore#8", classifiers.get(10).getName());
        assertEquals("Arvore#11", classifiers.get(13).getName());
        assertEquals("functions.RBFNetwork#7", classifiers.get(22).getName());

        Classifier nb = classifiers.get(1).getClassifier();
        assertEquals("-K", nb.getOptions()[0]);

        Classifier arv1 = classifiers.get(2).getClassifier();
        assertEquals("-C", arv1.getOptions()[0]);
        assertEquals("0.25", arv1.getOptions()[1]);
        assertEquals("-M", arv1.getOptions()[2]);
        assertEquals("2", arv1.getOptions()[3]);

        Classifier arv3 = classifiers.get(4).getClassifier();
        assertEquals("-C", arv3.getOptions()[0]);
        assertEquals("0.25", arv3.getOptions()[1]);
        assertEquals("-M", arv3.getOptions()[2]);
        assertEquals("4", arv3.getOptions()[3]);

        Classifier arv5 = classifiers.get(6).getClassifier();
        assertEquals("-C", arv5.getOptions()[0]);
        assertEquals("0.5", arv5.getOptions()[1]);
        assertEquals("-M", arv5.getOptions()[2]);
        assertEquals("2", arv5.getOptions()[3]);
    }
}
