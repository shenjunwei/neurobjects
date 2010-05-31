package utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import cern.colt.matrix.DoubleMatrix1D;

/** Defines a set of numeric patterns */
public class Patterns {
	
	Hashtable<String, ArrayList<Pattern>> pats = null; 
	int size;
	String label;
	
	public Patterns () { 
		this.basicSetup();
		
	}
	
	public Patterns (String title) {
		this.label = title;
		this.basicSetup();
	}
	
	public Patterns (ArrayList<DoubleMatrix1D> ps, String labels, double time, double timeStep) {
		this.basicSetup();
		this.addPatterns(ps, labels, time, timeStep);
		
	}
	
	public void addPatterns (ArrayList<DoubleMatrix1D> ps, String labels, double time, double timeStep) {
		Pattern p = null;
		for (int i=0; i<ps.size();i++) {
			p = new Pattern(ps.get(i), labels, time);
			time += timeStep;
			this.addPattern(p);
		}
	}
	
	private void basicSetup() {
		pats = new Hashtable<String, ArrayList<Pattern>> ();				
	}
	
	public void addPattern (Pattern pat) {
		//String currentLabel = ; 
		ArrayList<Pattern> list = pats.get(pat.getLabel()); 
		
		if (list==null) {
			list = new ArrayList<Pattern>();
			list.add(pat);
			pats.put(pat.getLabel(), list);
		}
		else {
			list.add(pat);
		}
				
	}
	
	public String toString () {
		ArrayList<Pattern> list = null;
		Enumeration labels = this.pats.keys();
		String result="";
		String currentLabel="";
		
		
		for (; labels.hasMoreElements() ;) {
			currentLabel = (String) labels.nextElement();
			list = pats.get(currentLabel );
			result+=currentLabel+"\n";
	        for (int i=0; i<list.size();i++){
	        	result+=list.get(i).toString();
	        }

	     }
		
		return (result);
	}

}
