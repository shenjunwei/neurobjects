package nda.analysis.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nda.data.BehaviorHandlerI;
import nda.data.CountMatrix;
import nda.data.Interval;
import nda.data.SpikeHandlerI;
import nda.data.SpikeTrainI;
import nda.util.ArrayUtils;


/**
 * @author Giuliano Vilela
 */
public class SpikeFeatures {

    public static Map<String,double[]> firingRateSamples(
            CountMatrix countMatrix,
            BehaviorHandlerI behaviorHandler,
            List<String> behaviors,
            String neuron) {

        Map<String,double[]> behaviorSamples = new HashMap<String, double[]>();

        for (String label : behaviors) {
            double[] samples = getBehaviorFiringRates(
                    countMatrix, behaviorHandler, neuron, label);
            behaviorSamples.put(label, samples);
        }

        return behaviorSamples;
    }


    public static Map<String,double[]> populationFiringRateSamples(
            CountMatrix countMatrix,
            BehaviorHandlerI behaviorHandler,
            List<String> behaviors) {

        Map<String,double[]> behaviorSamples = new HashMap<String, double[]>();

        for (String label : behaviors) {
            double[] samples = getBehaviorPopulationFiringRates(
                    countMatrix, behaviorHandler, label);

            behaviorSamples.put(label, samples);
        }

        return behaviorSamples;
    }


    public static Map<String,double[]> interSpikeIntervalSamples(
            SpikeHandlerI spikeHandler,
            BehaviorHandlerI behaviorHandler,
            List<String> behaviors,
            String neuron) {

        Map<String,double[]> isiSamples = new HashMap<String,double[]>();

        for (String label : behaviors) {
            double[] samples = getBehaviorISIs(spikeHandler, behaviorHandler, neuron, label);
            isiSamples.put(label, samples);
        }

        return isiSamples;
    }


    /**
     * window_width and bin_size must be set on the countMatrix by the caller.
     */
    public static Map<String,double[]> patternDistancesSamples(
            CountMatrix countMatrix,
            BehaviorHandlerI behaviorHandler,
            List<String> behaviors) {

        Map<String,double[]> behaviorSamples = new HashMap<String,double[]>();

        for (int i = 0; i < behaviors.size(); ++i) {
            for (int j = i; j < behaviors.size(); ++j) {
                String labelA = behaviors.get(i);
                String labelB = behaviors.get(j);

                List<double[]> patternsA = getBehaviorPatterns(countMatrix, behaviorHandler, labelA);
                List<double[]> patternsB = getBehaviorPatterns(countMatrix, behaviorHandler, labelB);

                int estimateSize = patternsA.size() * patternsB.size() + 2;
                List<Double> distanceSamples = new ArrayList<Double>(estimateSize);

                if (!labelA.equals(labelB)) {
                    for (double[] pA : patternsA) {
                        for (double[] pB : patternsB) {
                            double dist = ArrayUtils.euclideanDistance(pA, pB);
                            distanceSamples.add(dist);
                        }
                    }
                }
                else {
                    for (int p = 0; p < patternsA.size(); ++p) {
                        for (int q = p+1; q < patternsB.size(); ++q) {
                            double[] pA = patternsA.get(p);
                            double[] pB = patternsB.get(q);
                            double dist = ArrayUtils.euclideanDistance(pA, pB);
                            distanceSamples.add(dist);
                        }
                    }
                }

                String labelAB = labelA + '_' + labelB;
                behaviorSamples.put(labelAB, ArrayUtils.toPrimitiveArray(distanceSamples));
            }
        }

        return behaviorSamples;
    }



    /*
     * Helper functions
     */

    private static double[] getBehaviorFiringRates(
            CountMatrix countMatrix,
            BehaviorHandlerI behaviorHandler,
            String neuron,
            String behavior) {

        int[] neuron_rates = countMatrix.getRow(neuron);
        List<Double> behavior_rates = new ArrayList<Double>();

        for (Interval interval : behaviorHandler.getContactIntervals(behavior)) {
            interval = interval.intersection(countMatrix.getInterval());
            if (interval.isEmpty()) continue;

            int st = countMatrix.getBinForTime(interval.start());
            int end = countMatrix.getBinForTime(interval.end());

            for (int i = st; i <= end; ++i)
                behavior_rates.add((double)neuron_rates[i]);
        }

        return ArrayUtils.toPrimitiveArray(behavior_rates);
    }


    private static double[] getBehaviorPopulationFiringRates(
            CountMatrix countMatrix,
            BehaviorHandlerI behaviorHandler,
            String behavior) {

        List<Double> behavior_rates = new ArrayList<Double>();

        for (Interval interval : behaviorHandler.getContactIntervals(behavior)) {
            interval = interval.intersection(countMatrix.getInterval());
            if (interval.isEmpty()) continue;

            int st = countMatrix.getBinForTime(interval.start());
            int end = countMatrix.getBinForTime(interval.end());

            for (int i = st; i <= end; ++i) {
                int[] population_rates = countMatrix.getColumn(i);
                double avg_rate = ArrayUtils.average(population_rates);
                behavior_rates.add(avg_rate);
            }
        }

        return ArrayUtils.toPrimitiveArray(behavior_rates);
    }


    private static double[] getBehaviorISIs(
            SpikeHandlerI spikeHandler,
            BehaviorHandlerI behaviorHandler,
            String neuron,
            String behavior) {

        SpikeTrainI spikeTrain = spikeHandler.get(neuron);
        List<Double> isi_samples_l = new ArrayList<Double>();

        for (Interval interval : behaviorHandler.getContactIntervals(behavior)) {
            SpikeTrainI intervalTrain = spikeTrain.extractInterval(interval);
            if (intervalTrain.isEmpty())
                continue;

            double[] isi = getInterSpikeIntervals(intervalTrain);
            for (double isi_sample : isi)
                isi_samples_l.add(isi_sample);
        }

        double[] isi_samples = org.apache.commons.lang3.ArrayUtils.toPrimitive(
                isi_samples_l.toArray(new Double[] { }));

        return isi_samples;
    }


    private static double[] getInterSpikeIntervals(SpikeTrainI spikeTrain) {
        double[] isi = new double[spikeTrain.size()-1];

        for (int i = 1; i < spikeTrain.size(); ++i)
            isi[i-1] = spikeTrain.get(i) - spikeTrain.get(i-1);

        return isi;
    }


    private static List<double[]> getBehaviorPatterns(
            CountMatrix countMatrix,
            BehaviorHandlerI behaviorHandler,
            String behavior) {

        List<double[]> patterns = new ArrayList<double[]>();

        for (Interval interval : behaviorHandler.getContactIntervals(behavior)) {
            List<double[]> intervalPatterns = countMatrix.getPatterns(interval);
            patterns.addAll(intervalPatterns);
        }

        return patterns;
    }
}
