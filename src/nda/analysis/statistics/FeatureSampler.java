package nda.analysis.statistics;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;

import nda.analysis.InvalidSetupFileException;
import nda.data.BehaviorHandlerI;
import nda.data.CountMatrix;
import nda.data.SpikeHandlerI;
import nda.data.text.TextBehaviorHandler;
import nda.data.text.TextSpikeHandler;
import nda.util.RandomUtils;
import nda.util.Verbose;


/**
 * FeatureSampler.
 * 
 * @author Giuliano Vilela
 */
public class FeatureSampler implements Verbose {

    private static RandomData random = new RandomDataImpl();

    protected boolean verbose;
    protected FeatureSamplerSetup setup;

    protected CountMatrix countMatrix;
    protected SpikeHandlerI spikeHandler;
    protected BehaviorHandlerI behaviorHandler;


    public FeatureSampler(String setupFilepath)
    throws FileNotFoundException, InvalidSetupFileException {
        this(new FeatureSamplerSetup(setupFilepath));
    }


    public FeatureSampler(FeatureSamplerSetup _setup) {
        setup = _setup;
        setVerbose(false);
    }


    public Map<String,Map<String,double[]>> extractFeatures()
    throws FeatureExtractionException {

        showMessage("Reading spike data...");
        loadHandlers();

        List<String> neurons = spikeHandler.getNeuronNames();
        Map<String,Map<String,double[]>> features = new HashMap<String, Map<String,double[]>>();

        String feature = setup.getFeature();

        Map<String,Object> params = setup.getParams();

        @SuppressWarnings("unchecked")
        List<String> behaviors = (List<String>) params.get("labels");

        if (feature.equals("population_firing_rate")) {
            showMessage("Extracting " + feature + " from population");

            Map<String,double[]> samples = SpikeFeatures.populationFiringRateSamples(
                    countMatrix, behaviorHandler, behaviors);

            features.put("*", samples);
        }
        else if (feature.equals("pattern_distance")) {
            showMessage("Extracting " + feature + " from population");

            Map<String,double[]> samples = SpikeFeatures.patternDistancesSamples(
                    countMatrix, behaviorHandler, behaviors);

            features = expandPatternDistanceSamples(samples);
        }
        else {
            for (String neuron : neurons) {
                showMessage("Extracting " + feature + " from neuron " + neuron);

                Map<String,double[]> samples;

                if (feature.equals("firing_rate"))
                    samples = SpikeFeatures.firingRateSamples(
                            countMatrix, behaviorHandler, behaviors, neuron);
                else if (feature.equals("isi"))
                    samples = SpikeFeatures.interSpikeIntervalSamples(
                            spikeHandler, behaviorHandler, behaviors, neuron);
                else
                    throw new FeatureExtractionException("Unknown feature: " + feature);

                features.put(neuron, samples);
            }
        }

        if (params.containsKey("num_samples")) {
            int numSamples = (Integer) params.get("num_samples");
            for (Map<String,double[]> map : features.values()) {
                for (String key : map.keySet()) {
                    double[] samples = map.get(key);
                    if (numSamples < samples.length) {
                        samples = RandomUtils.randomSample(random, samples, numSamples);
                        map.put(key, samples);
                    }
                }
            }
        }

        return features;
    }


    private void loadHandlers() throws FeatureExtractionException {
        try {
            Map<String,Object> params = setup.getParams();
            String neuronFilter = (String) params.get("neurons");

            String spikeDir = setup.getSpikesDirectory();
            spikeHandler = new TextSpikeHandler(spikeDir);
            spikeHandler = spikeHandler.withFilter(neuronFilter);

            String behaviorFilepath = setup.getContactsFilepath();
            behaviorHandler = new TextBehaviorHandler(behaviorFilepath);

            double binSize = (Double) params.get("bin_size");
            int windowWidth = 10;
            if (params.containsKey("window_width"))
                windowWidth = (Integer) params.get("window_width");

            countMatrix = new CountMatrix(spikeHandler, binSize);
            countMatrix.setWindowWidth(windowWidth);
        } catch (Exception e) {
            throw new FeatureExtractionException(e);
        }
    }


    /*
     * dirty hack to see the results earlier! fix this later
     */
    private static Map<String,Map<String,double[]>> expandPatternDistanceSamples(
            Map<String,double[]> samples) {

        Map<String,Map<String,double[]>> features =
            new HashMap<String, Map<String,double[]>>();

        for (String behaviorPair : samples.keySet()) {
            Map<String,double[]> map = new HashMap<String, double[]>();
            map.put(behaviorPair, samples.get(behaviorPair));
            features.put(behaviorPair, map);
        }

        return features;
    }


    @Override
    public void setVerbose(boolean _verbose) {
        verbose = _verbose;
    }


    @Override
    public boolean getVerbose() {
        return verbose;
    }


    private void showMessage(String str) {
        if (verbose)
            System.out.println(str);
    }
}
