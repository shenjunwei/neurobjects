package utils;

import java.util.ArrayList;
import cern.colt.matrix.DoubleMatrix1D;

public interface SpkHandlerI {

	public DoubleMatrix1D 						getSpikes(String name);
	public  DoubleMatrix1D 						getSpikes(String name, double a, double b);
	public void 								setFilter (String filter);
	public String 								getFilter();
	public ArrayList<String> 					getNeuronNames();
	public ArrayList<DoubleMatrix1D> 			getAllSpikes();
	public ArrayList<DoubleMatrix1D> 			getAllSpikes(double a, double b);
	public int 									getNumberOfNeurons();
	 	
}

