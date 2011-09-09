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
import nda.data.SpikeTrain;


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
        assertEquals(10, handler.getNumberOfSpikeTrains());

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
        assertEquals(3, handler.getNumberOfSpikeTrains());

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
        assertEquals(3, handler.getNumberOfSpikeTrains());

        List<String> neuronNames = handler.getNeuronNames();
        assertTrue(neuronNames.contains("S1_03a"));
        assertTrue(neuronNames.contains("S1_08c"));

        Interval global = handler.getGlobalSpikeInterval();
        assertTrue(intervalS1.contains(global));

        SpikeHandlerI emptyHandler = new TextSpikeHandler(
                spikeDirPath, filterHP, Interval.EMPTY);

        assertEquals(3, emptyHandler.getNumberOfSpikeTrains());
        assertTrue(emptyHandler.getGlobalSpikeInterval().isEmpty());
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
     * Test method for {@link nda.data.text.TextSpikeHandler#getSourceType()}.
     */
    @Test
    public void testGetSourceType() {
        assertEquals("txt", s1Neurons.getSourceType());
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
     * Test method for {@link nda.data.text.TextSpikeHandler#getSpikeTrain(int)}.
     */
    @Test
    public void testGetSpikeTrainInt() {
        assertEquals("V1_04a", v1Neurons.getSpikeTrain(0).getName());
        assertEquals("V1_09a", v1Neurons.getSpikeTrain(1).getName());
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#getSpikeTrain(java.lang.String)}.
     */
    @Test
    public void testGetSpikeTrainString() {
        assertNotNull(hpNeurons.getSpikeTrain("HP_12b"));
        assertNotNull(hpNeurons.getSpikeTrain("HP_12a"));
        assertNotNull(v1Neurons.getSpikeTrain("V1_09a"));
        assertNull(hpNeurons.getSpikeTrain("HP_12b_missing"));
        assertNull(v1Neurons.getSpikeTrain("V1_12a"));
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#withFilter(java.lang.String)}.
     */
    @Test
    public void testWithFilter() throws Exception {
        assertEquals(10, allNeurons.getNumberOfSpikeTrains());

        SpikeHandlerI f1 = allNeurons.withFilter("HP_0");
        assertEquals("hp_0", f1.getFilter());
        assertEquals(1, f1.getNumberOfSpikeTrains());
        assertEquals("HP_02a", f1.getNeuronNames().get(0));

        SpikeHandlerI f2 = allNeurons.withFilter("S1");
        assertEquals("s1", f2.getFilter());
        assertEquals(3, f2.getNumberOfSpikeTrains());
        assertTrue(f2.getNeuronNames().contains("S1_03a"));
        assertTrue(f2.getNeuronNames().contains("S1_08c"));

        SpikeHandlerI f3 = allNeurons.withFilter("");
        assertEquals("", f3.getFilter());
        assertEquals(10, f3.getNumberOfSpikeTrains());

        SpikeHandlerI f4 = allNeurons.withFilter("*");
        assertEquals("*", f4.getFilter());
        assertEquals(10, f4.getNumberOfSpikeTrains());
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
        assertEquals(hpNeurons.getNumberOfSpikeTrains(), hpNames.size());
        assertTrue(hpNames.contains("HP_12b"));

        List<String> v1Names = v1Neurons.getNeuronNames();
        assertEquals(v1Neurons.getNumberOfSpikeTrains(), v1Names.size());
        assertTrue(v1Names.contains("V1_09a"));
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#getAllSpikeTrains()}.
     */
    @Test
    public void testGetAllSpikeTrains() {
        List<SpikeTrain> spikeTrains = hpNeurons.getAllSpikeTrains();
        SpikeTrain hp12a = spikeTrains.get(1);

        assertEquals(hpNeurons.getNumberOfSpikeTrains(), spikeTrains.size());
        assertEquals("HP_12a", hp12a.getName());
        assertTrue(hpNeurons.getGlobalSpikeInterval().contains(hp12a.getInterval()));
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#getAllSpikeTrains(nda.data.Interval)}.
     */
    @Test
    public void testGetAllSpikeTrainsInterval() {
        List<SpikeTrain> trains = hpNeurons.getAllSpikeTrains(Interval.EMPTY);
        assertTrue(trains.isEmpty());

        trains = hpNeurons.getAllSpikeTrains(Interval.make(5811.5, 5811.9));
        assertEquals(2, trains.size());
        assertEquals(1, trains.get(0).getNumberOfSpikes());
        assertEquals(1, trains.get(1).getNumberOfSpikes());
    }


    /**
     * Test method for {@link nda.data.text.TextSpikeHandler#getGlobalSpikeInterval()}.
     */
    @Test
    public void testGetGlobalSpikeInterval() throws Exception {
        SpikeHandlerI allNeurons = new TextSpikeHandler(spikeDirPath);
        Interval globalInterval = allNeurons.getGlobalSpikeInterval();

        for (SpikeTrain st : allNeurons.getAllSpikeTrains())
            assertTrue(globalInterval.contains(st.getInterval()));
    }


    @Test(expected = InvalidDataDirectoryException.class)
    public void testInvalidDirectory() throws Exception {
        new TextSpikeHandler(invalidDirPath);
    }


    @Test
    public void testCompositeFilter() throws Exception {
        SpikeHandlerI handler = new TextSpikeHandler(spikeDirPath, "HP , S1");

        assertEquals(6, handler.getAllSpikeTrains().size());
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
