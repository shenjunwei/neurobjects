package nda.analysis.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import nda.data.BehaviorHandlerI;
import nda.data.CountMatrix;
import nda.data.Interval;


/**
 * @author Giuliano Vilela
 */
public class SpikeFeatures {

    public static Map<String,double[]> firingRateSamples(
            CountMatrix countMatrix,
            BehaviorHandlerI behaviorHandler,
            String neuron) {

        Map<String,double[]> behaviorSamples = new HashMap<String, double[]>();

        for (String label : behaviorHandler.getLabelSet()) {
            double[] samples = getBehaviorFiringRates(
                    countMatrix, behaviorHandler, neuron, label);
            behaviorSamples.put(label, samples);
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

        double[] behavior_rates_a = ArrayUtils.toPrimitive(
                behavior_rates.toArray(new Double[] { }));

        return behavior_rates_a;
    }
}
