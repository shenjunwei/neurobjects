package tests;

import java.io.IOException;

import DataGenerator.CountMatrix;
import DataGenerator.TxtSpkHandler;

import errors.InvertedParameterException;
import errors.MissingDataFileException;


public class utils_CountMatrix  {

	/**
	 * \brief This class tests the methods of utils.CountMatrix class
	 * @throws IOException 
	 * @throws InvertedParameterException 
	 * @throws MissingDataFileException 
	 */
	public static void main(String[] args) throws MissingDataFileException, InvertedParameterException, IOException {
		double binSize = 2.5; //in seconds
		String dataSourcePath = "/tmp/";
		String neuronName = "";
		double a=0.3;
		double b=7950;
		
		TxtSpkHandler	spikeHandler = new TxtSpkHandler(dataSourcePath, neuronName, a, b);
//		TxtSpkHandler	spikeHandler = new TxtSpkHandler(dataSourcePath, neuronName);

		
		CountMatrix cmatrix = new CountMatrix(spikeHandler, binSize);
		
		if (cmatrix.isValid())
		{
			cmatrix.avgColumn(3);
			cmatrix.avgRow(0);      
			cmatrix.binSize();
			cmatrix.firstTime(); 
			cmatrix.lastTime();
			cmatrix.getIdx(3.14);
			cmatrix.getLen(800);
			cmatrix.getLog();
			cmatrix.getNeuronsNames();
	
			cmatrix.getWindowWidth();		
			cmatrix.setWindowWidth(3);
			cmatrix.getPattern();
			cmatrix.numPatterns(500);
			cmatrix.getPatterns(100, 600);
			cmatrix.numPatterns(500, 3.14);   
			cmatrix.windowPossible(800, 500);
	
			
			cmatrix.getTitle();
			cmatrix.setTitle("Teste....");
	
			cmatrix.isValid();
			cmatrix.numberOfNeurons();
			cmatrix.numCols();
			cmatrix.numRows();
		}
		else
		{
			System.out.println("Error: Cmatrix not valid");
		}
		
		
	//	cmatrix.show();
	//	cmatrix.showMatrix2D();
	//	cmatrix.showNeuronNames();

	}

}
