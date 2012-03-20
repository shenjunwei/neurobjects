package nda.analysis.statistics;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nda.analysis.InvalidSetupFileException;
import nda.data.BehaviorHandlerI;
import nda.data.CountMatrix;
import nda.data.SpikeHandlerI;
import nda.data.text.TextBehaviorHandler;
import nda.data.text.TextSpikeHandler;
import nda.util.Verbose;


/**
 * FeatureSampler.
 * 
 * @author Giuliano Vilela
 */
public class FeatureSampler implements Verbose {
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


    public Map<String,Map<String,double[]>> extractFeatures() throws FeatureExtractionException {
        showMessage("Reading spike data...");
        loadHandlers();

        List<String> neurons = spikeHandler.getNeuronNames();
        Map<String,Map<String,double[]>> features = new HashMap<String, Map<String,double[]>>();

        String feature = setup.getFeature();

        if (feature.equals("population_firing_rate")) {
            showMessage("Extracting " + feature + " from population");

            Map<String,double[]> samples = SpikeFeatures.populationFiringRateSamples(
                    countMatrix, behaviorHandler);

            features.put("*", samples);
        }
        else {
            for (String neuron : neurons) {
                showMessage("Extracting " + feature + " from neuron " + neuron);

                Map<String,double[]> samples;

                if (feature.equals("firing_rate"))
                    samples = SpikeFeatures.firingRateSamples(
                            countMatrix, behaviorHandler, neuron);
                else if (feature.equals("isi"))
                    samples = SpikeFeatures.interSpikeIntervalSamples(
                            spikeHandler, behaviorHandler, neuron);
                else
                    throw new FeatureExtractionException("Unknown feature: " + feature);

                features.put(neuron, samples);
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
            countMatrix = new CountMatrix(spikeHandler, binSize);
        } catch (Exception e) {
            throw new FeatureExtractionException(e);
        }
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
