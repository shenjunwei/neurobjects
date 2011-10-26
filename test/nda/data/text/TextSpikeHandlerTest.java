package nda.data.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import nda.data.Interval;
import nda.data.SpikeHandlerI;
import nda.data.SpikeTrainI;


/**
 * Tests for the TextSpikeHandler class.
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class TextSpikeHandlerTest {
    private String spikeDirPath = "data/test/spikes/";
    private String invalidDirPath = "data/test/_INVALID_";

    private String filterHP = "HP";
    private SpikeHandlerI hpNeurons;

    private String filterV1_0 = "V1_0";
    private SpikeHandlerI v1Neurons;

    private String filterS1 = "S1";
    private Interval intervalS1 = Interval.make(5811, 5820);
    private SpikeHandlerI s1Neurons;

    private SpikeHandlerI allNeurons;


    @Before
    public void setUp() throws Exception {
        hpNeurons = new TextSpikeHandler(spikeDirPath, filterHP);
        v1Neurons = new TextSpikeHandler(spikeDirPath, filterV1_0);
        s1Neurons = new TextSpikeHandler(spikeDirPath, filterS1, intervalS1);
        allNeurons = new TextSpikeHandler(spikeDirPath);
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#TextSpikeHandler(java.lang.String)}.
     */
    @Test
    public void testTextSpikeHandlerString() throws Exception {
        SpikeHandlerI handler = new TextSpikeHandler(spikeDirPath);
        assertEquals(10, handler.size());

        List<String> neuronNames = handler.getNeuronNames();
        assertTrue(neuronNames.contains("HP_02a"));
        assertTrue(neuronNames.contains("S1_07a"));
        assertTrue(neuronNames.contains("V1_16a"));
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#TextSpikeHandler(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testTextSpikeHandlerStringString() throws Exception {
        SpikeHandlerI handler = new TextSpikeHandler(spikeDirPath, filterHP);
        assertEquals(3, handler.size());

        List<String> neuronNames = handler.getNeuronNames();
        assertTrue(neuronNames.contains("HP_02a"));
        assertTrue(neuronNames.contains("HP_12b"));
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#TextSpikeHandler(java.lang.String, java.lang.String, nda.data.Interval)}.
     */
    @Test
    public void testTextSpikeHandlerStringStringInterval() throws Exception {
        SpikeHandlerI handler = new TextSpikeHandler(spikeDirPath, filterS1, intervalS1);
        assertEquals(3, handler.size());

        List<String> neuronNames = handler.getNeuronNames();
        assertTrue(neuronNames.contains("S1_03a"));
        assertTrue(neuronNames.contains("S1_08c"));

        Interval global = handler.getRecordingInterval();
        assertTrue(intervalS1.contains(global));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInterval() throws Exception {
        SpikeHandlerI emptyHandler = new TextSpikeHandler(
                spikeDirPath, filterHP, Interval.EMPTY);

        assertEquals(3, emptyHandler.size());
        assertTrue(emptyHandler.getRecordingInterval().isEmpty());
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#toString()}.
     */
    @Test
    public void testToString() {
        String str = hpNeurons.toString();
        for (String neuronName : hpNeurons.getNeuronNames())
            assertTrue(str.contains(neuronName));
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#getDataSourceType()}.
     */
    @Test
    public void testGetSourceType() {
        assertEquals("txt", s1Neurons.getDataSourceType());
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#getAnimalName()}.
     */
    @Test
    public void testGetAnimalName() {
        assertEquals("<unknown>", hpNeurons.getAnimalName());
        assertEquals("<unknown>", v1Neurons.getAnimalName());
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#get(int)}.
     */
    @Test
    public void testgetInt() {
        assertEquals("V1_04a", v1Neurons.get(0).getNeuronName());
        assertEquals("V1_09a", v1Neurons.get(1).getNeuronName());
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#get(java.lang.String)}.
     */
    @Test
    public void testgetString() {
        assertNotNull(hpNeurons.get("HP_12b"));
        assertNotNull(hpNeurons.get("HP_12a"));
        assertNotNull(v1Neurons.get("V1_09a"));
        assertNull(hpNeurons.get("HP_12b_missing"));
        assertNull(v1Neurons.get("V1_12a"));
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#withFilter(java.lang.String)}.
     */
    @Test
    public void testWithFilter() throws Exception {
        assertEquals(10, allNeurons.size());

        SpikeHandlerI f1 = allNeurons.withFilter("HP_0");
        assertEquals("hp_0", f1.getFilter());
        assertEquals(1, f1.size());
        assertEquals("HP_02a", f1.getNeuronNames().get(0));

        SpikeHandlerI f2 = allNeurons.withFilter("S1");
        assertEquals("s1", f2.getFilter());
        assertEquals(3, f2.size());
        assertTrue(f2.getNeuronNames().contains("S1_03a"));
        assertTrue(f2.getNeuronNames().contains("S1_08c"));

        SpikeHandlerI f3 = allNeurons.withFilter("");
        assertEquals("", f3.getFilter());
        assertEquals(10, f3.size());

        SpikeHandlerI f4 = allNeurons.withFilter("*");
        assertEquals("*", f4.getFilter());
        assertEquals(10, f4.size());
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#getFilter()}.
     */
    @Test
    public void testGetFilter() {
        assertEquals("hp", hpNeurons.getFilter());
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#getNeuronNames()}.
     */
    @Test
    public void testGetNeuronNames() {
        List<String> hpNames = hpNeurons.getNeuronNames();
        assertEquals(hpNeurons.size(), hpNames.size());
        assertTrue(hpNames.contains("HP_12b"));

        List<String> v1Names = v1Neurons.getNeuronNames();
        assertEquals(v1Neurons.size(), v1Names.size());
        assertTrue(v1Names.contains("V1_09a"));
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#getAllSpikeTrains()}.
     */
    @Test
    public void testgetAllSpikeTrains() {
        SpikeTrainI hp12a = hpNeurons.get(1);

        assertEquals(hpNeurons.size(), hpNeurons.size());
        assertEquals("HP_12a", hp12a.getNeuronName());
        assertTrue(hpNeurons.getRecordingInterval().contains(hp12a.getInterval()));
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#getAllSpikeTrains(nda.data.Interval)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExtractEmptyInterval() throws Exception {
        hpNeurons.extractInterval(Interval.EMPTY);
    }


    @Test
    public void testExtractInterval() {
        SpikeHandlerI extracted = hpNeurons.extractInterval(Interval.make(5811.5, 5811.9));
        assertEquals(2, extracted.size());
        assertEquals(1, extracted.get(0).size());
        assertEquals(1, extracted.get(1).size());
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#getRecordingInterval()}.
     */
    @Test
    public void testGetRecordingInterval() throws Exception {
        SpikeHandlerI allNeurons = new TextSpikeHandler(spikeDirPath);
        Interval globalInterval = allNeurons.getRecordingInterval();

        for (SpikeTrainI st : allNeurons)
            assertTrue(globalInterval.contains(st.getInterval()));
    }


    @Test(expected = InvalidDataDirectoryException.class)
    public void testInvalidDirectory() throws Exception {
        new TextSpikeHandler(invalidDirPath);
    }


    @Test
    public void testCompositeFilter() throws Exception {
        SpikeHandlerI handler = new TextSpikeHandler(spikeDirPath, "HP , S1");

        assertEquals(6, handler.size());
        assertTrue(handler.getNeuronNames().contains("HP_02a"));
        assertTrue(handler.getNeuronNames().contains("S1_07a"));
        assertFalse(handler.getNeuronNames().contains("V1_04a"));
        assertFalse(handler.getNeuronNames().contains("V1_16a"));
    }


    @Test
    public void testCount() throws Exception {
        assertEquals(3, TextSpikeHandler.spikeTrainCount(spikeDirPath, "HP"));
        assertEquals(4, TextSpikeHandler.spikeTrainCount(spikeDirPath, "V1"));
        assertEquals(2, TextSpikeHandler.spikeTrainCount(spikeDirPath, "V1_1"));
        assertEquals(10, TextSpikeHandler.spikeTrainCount(spikeDirPath, "*"));
        assertEquals(10, TextSpikeHandler.spikeTrainCount(spikeDirPath, ""));
        assertEquals(6, TextSpikeHandler.spikeTrainCount(spikeDirPath, "hp, s1"));
    }
}
