package nda.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.core.Instances;

import nda.analysis.PatternHandler;
import nda.data.CountMatrix;
import nda.data.SpikeHandlerI;
import nda.data.SpikeRateMatrixI;
import nda.data.text.TextSpikeHandler;


/**
 * Tests for the PatternHandler class
 * 
 * @author Giuliano Vilela
 */
public class PatternHandlerTest {
    private static Set<String> labels;
    private static SpikeHandlerI spikeHandler;

    private static SpikeRateMatrixI matrixA;
    private static SpikeRateMatrixI matrixB;

    private PatternHandler relationA;
    private PatternHandler relationB;


    @BeforeClass
    public static void setUpClass() throws Exception {
        spikeHandler = new TextSpikeHandler("data/test/spikes");

        spikeHandler.setFilter("HP");
        matrixA = new CountMatrix(spikeHandler, 0.250);
        matrixA.setWindowWidth(100);

        spikeHandler.setFilter("V1");
        matrixB = new CountMatrix(spikeHandler, 500);
        matrixB.setWindowWidth(499);

        labels = new HashSet<String>(Arrays.asList(
                "A", "B", "C",
                "test space", "\"test quote\""
        ));
    }

    @Before
    public void setUp() throws Exception {


        relationA = new PatternHandler("relationA", matrixA, labels);
        relationB = new PatternHandler("relationB", matrixB, labels);
    }


    @Test
    public void testInitialization() {
        assertEquals(labels, relationA.getLabelSet());
        assertEquals(labels, relationB.getLabelSet());

        for (String label : labels) {
            assertEquals(0, relationA.getPatterns(label).size());
            assertEquals(0, relationB.getPatterns(label).size());
        }
    }


    /**
     * Test method for {@link nda.analysis.PatternHandler#toString()}.
     */
    @Test
    public void testToString() {
        String str = relationA.toString();
        assertTrue(str.contains(relationA.getRelation().relationName()));

        for (String neuron : matrixA.getNeuronNames())
            assertTrue(str.contains(neuron));
    }


    /**
     * Test method for {@link nda.analysis.PatternHandler#toWekaFormat()}.
     */
    @Test
    public void testToWekaFormat() throws Exception {
        String relationA_arff = relationA.toWekaFormat();

        Instances relA = relationA.getRelation();
        Instances relB = new Instances(new StringReader(relationA_arff));

        assertEquals(relA.numAttributes(), relB.numAttributes());
        assertEquals(relA.toSummaryString(), relB.toSummaryString());
    }


    /**
     * Test method for {@link nda.analysis.PatternHandler#addPattern(double[], java.lang.String)}.
     */
    @Test
    public void testAddPattern() {
        matrixA.setCurrentTime(matrixA.getInterval().start());
        int n_patterns = matrixA.numPatterns(matrixA.getWindowWidth());

        for (double[] pattern : matrixA)
            relationA.addPattern(pattern, "test space");

        assertEquals(n_patterns, relationA.size());

        matrixB.setCurrentTime(matrixB.getInterval().start());
        n_patterns = matrixB.numPatterns(matrixB.getWindowWidth());

        for (double[] pattern : matrixB)
            relationB.addPattern(pattern, "\"test quote\"");

        assertEquals(n_patterns, relationB.size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPattern() {
        relationA.addPattern(new double[] {0, 0}, "B");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLabel() {
        matrixA.setCurrentTime(matrixA.getInterval().start());

        double[] pattern = matrixA.iterator().next();
        relationA.addPattern(pattern, "invalid_label");
    }


    /**
     * Test method for {@link nda.analysis.PatternHandler#getDimension()}.
     */
    @Test
    public void testGetDimension() {
        Instances relA = relationA.getRelation();
        assertEquals(relA.numAttributes(), relationA.getDimension()+1);
    }


    /**
     * Test method for {@link nda.analysis.PatternHandler#getPatterns(java.lang.String)}.
     */
    @Test
    public void testGetPatterns() {
        matrixA.setCurrentTime(matrixA.getInterval().start());
        int n_patterns = matrixA.numPatterns(matrixA.getWindowWidth());
        int na = n_patterns / 2;
        int nb = n_patterns - na;

        Iterator<double[]> iter = matrixA.iterator();

        for (int i = 0; i < na; ++i)
            relationA.addPattern(iter.next(), "A");

        for (int i = 0; i < nb; ++i)
            relationA.addPattern(iter.next(), "B");

        List<double[]> pa = relationA.getPatterns("A");
        assertEquals(na, pa.size());

        List<double[]> pb = relationA.getPatterns("B");
        assertEquals(nb, pb.size());
    }


    /**
     * Test method for {@link nda.analysis.PatternHandler#getLabelSet()}.
     */
    @Test
    public void testGetLabelSet() {
        assertEquals(labels, relationA.getLabelSet());
        assertEquals(labels, relationB.getLabelSet());
    }
}
