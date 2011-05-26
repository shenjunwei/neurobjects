package app;

import static java.lang.System.out;

import nda.data.Interval;
import nda.data.SpikeHandlerI;
import nda.data.SpikeTrain;
import nda.data.text.TextSpikeHandler;
import static nda.util.ArrayUtils.*;


/**
 * This is a test application for the SpikeHandlerI and SpikeTrain components. It shows
 * how to use the TextSpikeHandler implementation of SpikeHandlerI to open a set of text
 * files containing spike train data. 
 * 
 * To open a set of spike trains we first need to provide:
 *  - Path to a directory of text files: {@code String path = ...;}
 *  - Filter to identify chosen neurons: {@code String filter = "V1";}
 *  - Desired interval of spike times: {@code Interval i = Interval.make(a, b);}
 * 
 * Next, we create a TextSpikeHandler with these parameters. It's the component that
 * reads the spike train raw data and provides a simple access interface (SpikeHandlerI). 
 * {@code SpikeHandlerI spikeHandler = new TextSpikeHandler(path, filter, i);}
 * 
 * To actually use the spike train data, we can loop through every spike train using:
 * {@code for (SpikeTrain spikeTrain : spikeHandler.getAllSpikes())} (There are other
 * ways to get spike trains, see SpikeHandlerI for details).
 * 
 * In this app, we use a SpikeTrain to access the activation times and the inter spike
 * intervals: {@code spikeTrain.getTimes()} and {@code spikeTrain.getInterSpikeInterval()}.
 * 
 * Finally, we print to the standard output a table containing basic statistics for every
 * spike train, such as: neuron name, time of first and last activations, average time of
 * activation, etc.
 *  
 * @author Giuliano Vilela.
 * @date May 19, 2011.
 */
public class ReadTextDataApp {
	public static void main(String[] args) throws Exception {
	    // Path to a directory with spike files
		String path = "setup/spikes";
		
		// Only open neurons whose names begin with "V1"
		String filter = "V1";
		
		// For each neuron, only load in memory the spikes that occur between
		// 0s and 5820s.
	    Interval interval = Interval.make(0, 5820);
	    
	    // Create a SpikeHandlerI to effectively open the files
	    SpikeHandlerI spikeHandler = new TextSpikeHandler(path, filter, interval);
		
	    // Print the table header
		out.println("Neuron | Spike min | Spike max | Spike avg | " +
				    "ISI min | ISI max | ISI avg");
		
		// For each spike train that matches the given criteria
		for (SpikeTrain spikeTrain : spikeHandler.getAllSpikeTrains()) {
		    // Load the spike train time series and inter spike intervals
			double[] times = spikeTrain.getTimes();
			double[] isi = spikeTrain.getInterspikeInterval();
				
			// Print a row of the table
			out.printf("%s %11.3f %11.3f %11.3f %9.3f %9.3f %9.3f\n",
					spikeTrain.getName(), spikeTrain.getFirst(), spikeTrain.getLast(),
					getAverage(times), getMin(isi), getMax(isi), getAverage(isi)
			);
		}
	}
}
