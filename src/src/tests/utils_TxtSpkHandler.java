package tests;

import java.io.IOException;
import java.util.ArrayList;

import errors.InvertedParameterException;
import errors.MissingDataFileException;

import utils.TxtSpkHandler;
//import utils.SpikeTrain;

public class utils_TxtSpkHandler {

	/**
	 * \brief This class tests the methods of utils.TxtSpkHandler class
	 * @throws IOException 
	 * @throws InvertedParameterException 
	 * @throws MissingDataFileException 
	 */
	public static void main(String[] args) throws MissingDataFileException, InvertedParameterException, IOException {
		String dataSourcePath = "/tmp/";
		String filter = "S1_02";
		double a=0.3;
		double b=7950;
		TxtSpkHandler txtSpkH = new TxtSpkHandler(dataSourcePath, filter, a, b);
		
		txtSpkH.beginInterval();
		txtSpkH.endInterval();
		txtSpkH.firstSpike();
		
	    //ArrayList<>	txtSpkH.getAllSpikes();

	}

}
