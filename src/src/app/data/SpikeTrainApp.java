package app.data;

import java.io.IOException;
import java.util.Collections;

import data.*;


/**
 * \brief App test for the SpikeTrain class.
 * 
 * This is a test application for the SpkHandlerI and SpikeTrain components. It shows how
 * to use the TxtSpkHandler implementation of SpkHandlerI to open a set of text files
 * containing spike train data. 
 * 
 * To open a set of spike trains we first need to provide:
 *  - Path to a directory of text files: \code String path = "/home/..."; \endcode
 *  - Filter to identify chosen neurons: \code String filter = "V1"; \endcode
 *  - Desired interval of spike times: \code double a = 0, b = 5820; \endcode
 * 
 * Next, we create a TxtSpkHandler with these parameters. It's the component that
 * reads the spike train raw data and provides a simple access interface (SpkHandlerI). 
 * \code SpkHandlerI spikeHandler = new TxtSpkHandler(path, filter, a, b); \endcode
 * 
 * To actually use the spike train data, we can loop through every spike train using:
 * \code for (SpikeTrain spikeTrain : spikeHandler.getAllSpikes()) \endcode \encode
 * (There are other ways to get spike trains, see SpkHandlerI for details)
 * 
 * In this app, we use a SpikeTrain to access the activation times and the inter spike
 * intervals: \code spikeTrain.getTimes() \endcode and \code spikeTrain.getISI() \endcode.
 * 
 * Finally, we print to the standard output a table containing basic statistics for every
 * spike train, such as: neuron name, time of first and last activations, average time of
 * activation, etc.
 *  
 * @author giulianoxt
 * @date May 19, 2011.
 */
public class SpikeTrainApp {
	public static void main(String[] args) throws IOException {
		String path = "/home/giulianoxt/workspace/nda/setup/spikes";
		String filter = "V1";
		double a = 0;
		double b = 5820;
		
		SpkHandlerI spikeHandler = new TxtSpkHandler(path, filter, a, b);
		
		System.out.println("Neuron | Spike min | Spike max | Spike avg | " +
				           "ISI min | ISI max | ISI avg");
		
		for (SpikeTrain spikeTrain : spikeHandler.getAllSpikes()) {
			double[] times = spikeTrain.getTimes();
			double[] isi = spikeTrain.getISI();
				
			System.out.printf("%s %10.3f %11.3f %11.3f %9.3f %10.3f %10.3f\n",
					spikeTrain.getName(), spikeTrain.getFirst(), spikeTrain.getLast(),
					getAverage(times), getMin(isi), getMax(isi), getAverage(isi)
			);
		}
	}
	
	public static double getMin(double[] mat) {
		assert mat.length != 0;
		
		double min = mat[0];
		for (int i = 1; i < mat.length; ++i)
			min = Math.min(min, mat[i]);
		
		return min;
	}
	
	public static double getMax(double[] mat) {
		assert mat.length != 0;
		
		double max = mat[0];
		for (int i = 1; i < mat.length; ++i)
			max = Math.max(max, mat[i]);
		
		return max;
	}
	
	public static double getAverage(double[] mat) {
		assert mat.length != 0;
		double sum = 0;
		for (double x : mat) sum += x;
		
		return sum / mat.length;
	}
}
