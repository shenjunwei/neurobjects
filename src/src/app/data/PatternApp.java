package app.data;

import java.util.Arrays;


import data.Pattern;

public class PatternApp {
	
	public static void main(String[] args) {
		
		double[] p = new double[10];
		String label = "teste";
		
		Pattern P = new Pattern (p,label,0);
		
		
		System.out.println(P);
		P = new Pattern (p,label,0);
		System.out.println(P);
		P.getPattern()[1] = 0;
		System.out.println(P);
		System.out.println(P.toWeka("t"));
		System.out.println(Arrays.toString(P.toWeka(2)));
		
		
		
		
		
	
	}

}
