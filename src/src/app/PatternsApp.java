package app;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.activity.InvalidActivityException;


import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import data.Pattern;
import data.Patterns;
import errors.InvalidArgumentException;

public class PatternsApp {

	/**
	 * @param args
	 * @throws InvalidActivityException 
	 * @throws InvalidArgumentException 
	 */
	public static void main(String[] args) throws InvalidActivityException, InvalidArgumentException {
		// TODO Auto-generated method stub
		
		double[] p = new double[20];
		double[] p2 = new double[20];
		double[] p3 = new double[20];
		String label = "teste";
		DenseDoubleMatrix1D Ptmp = new DenseDoubleMatrix1D(p);
		
		Pattern P = new Pattern (p,label,0);
		p2[0]=2;
		p3[0]=3;
		Pattern P2 = new Pattern (p2,label,0);
		Pattern P3 = new Pattern (p3,"novo",0);
		
		
		Patterns Ps = new Patterns ();
		
		Ps.addPattern(P);
		Ps.addPattern(P2);
		Ps.addPattern(P3);
		System.out.println(Ps);
		
		
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Integer> pos = new ArrayList<Integer>();
		pos.add(0);
		pos.add(1);
		
		names.add("HP_01a");
		names.add("HP_01b");
		names.add("HP_01c");
		names.add("HP_02a");
		Ps.setNeuronNames(names);
		
		System.out.println(Ps.buildWekaFile("teste", Ps.getPatterns("teste"), pos));
		

	}

}
