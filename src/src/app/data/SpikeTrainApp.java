package app.data;

import java.io.IOException;

import data.SpikeTrain;
import data.TxtSpkHandler;

import cern.jet.math.Functions;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;


/**
 * SpikeTrainApp - App test for the SpikeTrain class.
 * 
 * Constructs a table containing spike statistics for the V1_12a and V1_16a neurons and
 * writes it to the standard output.
 * 
 * @author giulianoxt
 *
 */
public class SpikeTrainApp {
	public static void main(String[] args) throws IOException {
		String path = "/home/giulianoxt/workspace/nda/setup/spikes";
		String filter = "HP";
		double a = 5810;
		double b = 5820;
		
		TxtSpkHandler spikeHandler = new TxtSpkHandler(path, filter, a, b);
		
		System.out.println("Neuron | Spike min | Spike max | Spike avg | " +
				           "ISI min | ISI max | ISI avg");
		
		for (SpikeTrain spikeTrain : spikeHandler.getAllSpikes()) {
			DoubleMatrix1D times = spikeTrain.getTimes();
			DoubleMatrix1D isi = getISI(times);
				
			System.out.printf("%s %.3f %.3f %.3f %.3f %.3f %.3f\n",
					spikeTrain.getName(),
					times.get(0), times.get(times.size()-1), getAverage(times),
					getMin(isi), getMax(isi), getAverage(isi)
			);
		}
	}
	
	public static double getMin(DoubleMatrix1D mat) {
		assert mat.size() != 0;
		
		double min = mat.get(0);
		for (int i = 1; i < mat.size(); ++i)
			min = Math.min(min, mat.get(i));
		
		return min;
	}
	
	public static double getMax(DoubleMatrix1D mat) {
		assert mat.size() != 0;
		
		double max = mat.get(0);
		for (int i = 1; i < mat.size(); ++i)
			max = Math.max(max, mat.get(i));
		
		return max;
	}
	
	public static double getAverage(DoubleMatrix1D mat) {
		assert mat.size() != 0;
		double sum = mat.aggregate(Functions.plus, Functions.identity);
		return sum / mat.size();
	}
	
	public static DoubleMatrix1D getISI(DoubleMatrix1D mat) {
		if (mat.size() == 0) {
			return mat;
		}
		
		DoubleMatrix1D isi = DoubleFactory1D.dense.make(mat.size()-1);
		for (int i = 1; i < mat.size(); ++i) {
			isi.set(i-1, mat.get(i) - mat.get(i-1));
		}
		
		return isi;
	}
}
