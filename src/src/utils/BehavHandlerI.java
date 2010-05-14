package utils;

import java.util.ArrayList;
import cern.colt.matrix.DoubleMatrix1D;

public interface BehavHandlerI {
	
	public ArrayList<DoubleMatrix1D> 		getIntervals(String label);
	public double[] 						getBigInterval(ArrayList<String> labels);
 
}
