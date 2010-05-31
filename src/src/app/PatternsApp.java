package app;

import java.util.Hashtable;

import utils.Pattern;
import utils.Patterns;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

public class PatternsApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		double[] p = new double[1000];
		double[] p2 = new double[1000];
		double[] p3 = new double[1000];
		String label = "teste";
		DenseDoubleMatrix1D Ptmp = new DenseDoubleMatrix1D(p);
		
		Pattern P = new Pattern (p,label,0);
		p2[0]=2;
		p3[0]=3;
		Pattern P2 = new Pattern (p2,label,0);
		Pattern P3 = new Pattern (p3,"novo",0);
		
		
		Patterns Ps = new Patterns ("testes");
		
		Ps.addPattern(P);
		Ps.addPattern(P2);
		Ps.addPattern(P3);
		System.out.println(Ps);

	}

}
