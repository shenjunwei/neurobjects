package tests;

import java.io.FileNotFoundException;
import java.io.IOException;

import DataGenerator.TxtSpikeTrain;

import cern.colt.matrix.DoubleMatrix1D;
import errors.InvertedParameterException;

public class utils_TxtSpikeTrainTest {

	/**
	 * \brief This class tests the methods of utils.TxtSpikeTrain class
	 * 
	 * 
	 * @throws InvertedParameterException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws InvertedParameterException, FileNotFoundException, IOException {
		
				String filePath="/tmp/S1_02a.txt"; //A true neuron file (one timestamp per line)
/**
				//Testing in normal situation
				TxtSpikeTrain spkTrain_1 = new TxtSpikeTrain(filePath);
				DoubleMatrix1D spikes_1 = spkTrain_1.getTimes();
				System.out.println("spikes size: "+filePath+": "+spkTrain_1.getNumberOfSpikes());
				
				//Testing in normal situation
				TxtSpikeTrain spkTrain_2 = new TxtSpikeTrain(filePath, "neuron_S1_02a");
				DoubleMatrix1D spikes_2 = spkTrain_2.getTimes();
				System.out.println("spikes size: "+filePath+": "+spkTrain_2.getNumberOfSpikes());

				//Testing in normal situation
				TxtSpikeTrain spkTrain_3 = new TxtSpikeTrain(filePath, "neuron_S1_02a", 0, 100);
				DoubleMatrix1D spikes_3 = spkTrain_3.getTimes();
				System.out.println("spikes size: "+filePath+": "+spkTrain_3.getNumberOfSpikes());

				//Testing in normal situation with a negative firsTime parameter
				TxtSpikeTrain spkTrain_4 = new TxtSpikeTrain(filePath, "neuron_S1_02a", -10, 100.00000000000000000009);
				DoubleMatrix1D spikes_4 = spkTrain_4.getTimes();
				System.out.println("spikes size: "+filePath+": "+spkTrain_4.getNumberOfSpikes());
				
				//Testing in an erroneous situation with firstTime > lastTime
				TxtSpikeTrain spkTrain_5 = new TxtSpikeTrain(filePath, "neuron_S1_02a", 100.00000000000000000009, 0);
				DoubleMatrix1D spikes_5 = spkTrain_5.getTimes();
				System.out.println("spikes size: "+filePath+": "+spkTrain_5.getNumberOfSpikes());

				//Testing in normal situation with equals parameters 'firstTime' and 'LastTime'
				TxtSpikeTrain spkTrain_6 = new TxtSpikeTrain(filePath, "neuron_S1_02a", 0, 0);
				DoubleMatrix1D spikes_6 = spkTrain_6.getTimes();
				System.out.println("spikes size: "+spkTrain_6.getNumberOfSpikes());


				//Testing in an abnormal situation with a sourceFile that don't is a neuronFile but an Gif binary image file.
				TxtSpikeTrain spkTrain_7 = new TxtSpikeTrain("/tmp/talesmileto01.gif", "neuron_S1_02a", 0, 500);
				DoubleMatrix1D spikes_7 = spkTrain_7.getTimes();
				System.out.println("spikes size: "+spkTrain_7.getNumberOfSpikes());

				//Testing in an abnormal situation with a sourceFile that don't exists
				TxtSpikeTrain spkTrain_8 = new TxtSpikeTrain("/tmp/huiahsuihsi", "neuron_S1_02a", 0, 200);
				DoubleMatrix1D spikes_8 = spkTrain_8.getTimes();
				System.out.println("spikes size: "+spkTrain_8.getNumberOfSpikes());
				
				//Testing in an abnormal situation with a source file that comes from /dev/random
				TxtSpikeTrain spkTrain_9 = new TxtSpikeTrain("/dev/random", "neuron_S1_02a", 0, 100);
				DoubleMatrix1D spikes_9 = spkTrain_9.getTimes();
				System.out.println("spikes size: "+spkTrain_9.getNumberOfSpikes());
**/

				
				//Testing in an abnormal situation which the 'firstime' and 'lastTime' parameter is much larger than the existing in source file
				TxtSpikeTrain spkTrain_10 = new TxtSpikeTrain(filePath, "neuron_S1_02a", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
				DoubleMatrix1D spikes_10 = spkTrain_10.getTimes();
				System.out.println("spikes size: "+spkTrain_10.getNumberOfSpikes());
				/**				
				**/
	}
}
