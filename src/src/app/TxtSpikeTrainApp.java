package app;

import java.io.FilePermission;

import cern.colt.matrix.DoubleMatrix1D;
import utils.TxtSpikeTrain;

public class TxtSpikeTrainApp {
	

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String filePath="/tmp/S1_02a.txt";
		TxtSpikeTrain spkTrain_1 = new TxtSpikeTrain(filePath);
		DoubleMatrix1D spikes_1 = spkTrain_1.getTimes();
		System.out.println("spikes size: "+filePath+": "+spkTrain_1.getNumberOfSpikes());
		

		TxtSpikeTrain spkTrain_2 = new TxtSpikeTrain(filePath, "neuron_S1_02a");
		DoubleMatrix1D spikes_2 = spkTrain_2.getTimes();
		System.out.println("spikes size: "+filePath+": "+spkTrain_2.getNumberOfSpikes());

		
		TxtSpikeTrain spkTrain_3 = new TxtSpikeTrain(filePath, "neuron_S1_02a", 0, 100);
		DoubleMatrix1D spikes_3 = spkTrain_3.getTimes();
		System.out.println("spikes size: "+filePath+": "+spkTrain_3.getNumberOfSpikes());
		
		TxtSpikeTrain spkTrain_4 = new TxtSpikeTrain(filePath, "neuron_S1_02a", -10, 100.00000000000000000009);
		DoubleMatrix1D spikes_4 = spkTrain_4.getTimes();
		System.out.println("spikes size: "+filePath+": "+spkTrain_4.getNumberOfSpikes());
		
		TxtSpikeTrain spkTrain_6 = new TxtSpikeTrain(null, "neuron_S1_02a", 0, 0);
		DoubleMatrix1D spikes_6 = spkTrain_6.getTimes();
		System.out.println("spikes size: "+filePath+": "+spkTrain_6.getNumberOfSpikes());

		TxtSpikeTrain spkTrain_7 = new TxtSpikeTrain("/tmp/talesmileto01.gif", "neuron_S1_02a", 0, 0);
		DoubleMatrix1D spikes_7 = spkTrain_7.getTimes();
		System.out.println("spikes size: "+filePath+": "+spkTrain_6.getNumberOfSpikes());
		
		TxtSpikeTrain spkTrain_5 = new TxtSpikeTrain(filePath, "neuron_S1_02a", 100.00000000000000000009, 0);
		DoubleMatrix1D spikes_5 = spkTrain_5.getTimes();
		System.out.println("spikes size: "+filePath+": "+spkTrain_5.getNumberOfSpikes());

	}

}
