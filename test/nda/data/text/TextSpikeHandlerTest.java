package nda.data.text;

import java.util.List;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import nda.data.Interval;
import nda.data.SpikeHandlerI;
import nda.data.SpikeTrain;


/**
 * Tests for the TextSpikeHandler class.
 * 
 * @author giulianoxt
 */
public class TextSpikeHandlerTest {
    private String spikeDirPath = "setup/spikes/";
    private String invalidDirPath = "setup/_INVALID_";
    
    private String filterHP = "HP";
    private SpikeHandlerI hpNeurons;
    
    private String filterV1_0 = "V1_0";
    private SpikeHandlerI v1Neurons;
    
    private String filterS1 = "S1";
    private Interval intervalS1 = Interval.make(5811, 5820);
    private SpikeHandlerI s1Neurons;
    
    
    @Before
    public void setUp() throws Exception {
        hpNeurons = new TextSpikeHandler(spikeDirPath, filterHP);
        v1Neurons = new TextSpikeHandler(spikeDirPath, filterV1_0);
        s1Neurons = new TextSpikeHandler(spikeDirPath, filterS1, intervalS1);
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
        fail("Not yet implemented");
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
     * Test method for {@link nda.data.text.TextSpikeHandler#setFilter(java.lang.String)}.
     */
    @Test
    public void testSetFilter() throws Exception {
        assertEquals(3, hpNeurons.getNumberOfSpikeTrains());
        
        hpNeurons.setFilter("HP_0");
        assertEquals(1, hpNeurons.getNumberOfSpikeTrains());
        
        hpNeurons.setFilter("S1");
        assertEquals(3, hpNeurons.getNumberOfSpikeTrains());
        
        hpNeurons.setFilter("");
        assertEquals(10, hpNeurons.getNumberOfSpikeTrains());
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
        assertEquals(3, trains.size());
        assertTrue(trains.get(1).getInterval().isEmpty());
       
        trains = hpNeurons.getAllSpikeTrains(Interval.make(5811.5, 5811.9));
        assertEquals(1, trains.get(0).getNumberOfSpikes());
        assertEquals(0, trains.get(1).getNumberOfSpikes());
        assertEquals(1, trains.get(2).getNumberOfSpikes());
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
}
