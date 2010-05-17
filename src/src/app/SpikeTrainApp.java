package app;

import cern.colt.matrix.DoubleMatrix1D;


import utils.SpikeTrain;

public class SpikeTrainApp {

	private static SpikeTrain spkTrain;
	private static String filePath;
	
	
	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//filePath = args[0];
		filePath = "/tmp/S1_02a.txt";
		spkTrain =  new SpikeTrain(filePath);
		DoubleMatrix1D spikes = spkTrain.getTimes();
		
		SpikeTrain newSpikeTrain = new SpikeTrain(spikes, "S1_02a");
		DoubleMatrix1D newSpikes = newSpikeTrain.getTimes(); 

		SpikeTrain spkTrain2 = new SpikeTrain(filePath, "S1_02a");
		DoubleMatrix1D spk2 = spkTrain2.getTimes(); 
		
		SpikeTrain spkTrain3 = new SpikeTrain(filePath, 0, 100);
		DoubleMatrix1D spk3 = spkTrain3.getTimes(); 
		
		SpikeTrain spkTrain4 = new SpikeTrain(filePath, "S1_02a", 0, 100);
		DoubleMatrix1D spk4 = spkTrain4.getTimes(); 
		
		
		
		
//		for (int i=0; i<newSpikes.size(); i++){
//			if (i<=100)
//				System.out.println(spikes.get(i));
//		}
	}

}
