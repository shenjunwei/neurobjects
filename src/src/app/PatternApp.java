package app;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import utils.Pattern;

public class PatternApp {
	
	public static void main(String[] args) {
		
		double[] p = new double[10];
		String label = "teste";
		DenseDoubleMatrix1D Ptmp = new DenseDoubleMatrix1D(p);
		
		Pattern P = new Pattern (p,label,0);
		
		
		System.out.println(P);
		P = new Pattern (Ptmp,label,0);
		System.out.println(P);
		Ptmp.setQuick(1, 0);
		System.out.println(P);
		System.out.println(P.toWeka("t"));
		
		
		
	
	}

}
