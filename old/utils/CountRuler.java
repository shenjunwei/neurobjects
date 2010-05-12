package utils;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;

public class CountRuler {
	
	public static final byte PEARSON=1;
	
	
	public static DoubleMatrix2D measure (DoubleMatrix2D A, byte method) {
		DoubleMatrix2D R = null;
		
		if (method==PEARSON) {
			R = cern.colt.matrix.doublealgo.Statistic.correlation(cern.colt.matrix.doublealgo.Statistic.covariance(A.viewDice()));
		}	
		//DoubleMatrix2D R = cern.colt.matrix.doublealgo.Statistic.correlation(cern.colt.matrix.doublealgo.Statistic.covariance(A.viewDice()));
		return (R);
		
	}
	public static DoubleMatrix2D measure (DoubleMatrix2D A, int firstCol, int width, byte method) {
		
		return (measure (A.viewPart(0, firstCol, A.rows(), width),method));
	}
	
	

}
