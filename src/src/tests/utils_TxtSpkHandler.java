package tests;

import java.io.IOException;
import java.util.ArrayList;

import DataGenerator.TxtSpikeTrain;
import DataGenerator.TxtSpkHandler;

import cern.colt.matrix.DoubleMatrix1D;

import errors.InvertedParameterException;
import errors.MissingDataFileException;


public class utils_TxtSpkHandler {

	/**
	 * \brief This class tests the methods of utils.TxtSpkHandler class
	 * @throws IOException 
	 * @throws InvertedParameterException 
	 * @throws MissingDataFileException 
	 */
	public static void main(String[] args) throws MissingDataFileException, InvertedParameterException, IOException {
		String dataSourcePath = "/tmp/";
		String filter = "S1";
		double a=0.3;
		double b=7950;
		TxtSpkHandler txtSpkH = new TxtSpkHandler(dataSourcePath, filter, a, b);
		
		txtSpkH.beginInterval();
		txtSpkH.endInterval();
		txtSpkH.firstSpike();
		
		TxtSpikeTrain spike = (TxtSpikeTrain) txtSpkH.getSpikes("s1_02a"); 
		
		spike.isValid();
		spike.getName();
		System.out.println(spike.getNumberOfSpikes());
		
		
		
	    //ArrayList<>	txtSpkH.getAllSpikes();

	}

}
