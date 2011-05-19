package data;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;


/** \brief Models a neuronal response patterns */
public class Pattern {
	
	/** vector with the patterns elements*/
	DoubleMatrix1D pattern=null;
	
	/** Pattern label to pattern */
	String label="";
	
	/** Pattern begin time */ 
	double time=0.0;
	
	/** Pattern dimension */
	int dim = 0;
	
	
	public Pattern (DoubleMatrix1D p, String label, double time) {
		this.pattern = p.copy();
		this.basicSetup(label, time); 
	}
	
	public Pattern (double p[], String label, double time) {
		this.pattern = new DenseDoubleMatrix1D(p);
		this.basicSetup(label, time);
	
	}
	
	private void basicSetup (String label, double time) {
	
		this.label = label;
		this.time = time;
		this.dim = this.pattern.size();
	}
	public String toString () {
		return ("\nTime(s): "+time+"\tLabel: "+label+"\n"+this.pattern.toString() );
	}
	
	public double getTime() {
		return (this.time);
	}
	
	public boolean equals (Pattern p) {
		if (this.dim!=p.getDimension()) {
			return (false);
		}
		if (!this.label.equals(p.getLabel())) {
			return (false);
		}
		if (this.time!=p.getTime()) {
			return (false);
		}
		if (!this.pattern.equals(p.pattern)) {
			return (false);
		}
		return (true);
	}
	
	public double[] toWeka (double classValue) {
		double values[] = new double [this.dim+1];
		
		
		this.pattern.toArray(values);
		values[this.dim] = classValue;
		
		return (values);
	}
	
	public double[] toArray () {
		return (this.pattern.toArray());
	}
	
	public String toWeka (String classValue) {
		String result="";
		for (int i=0; i<this.pattern.size(); i++) {
			result+=this.pattern.get(i)+",";
		}
		result +=classValue;
		return (result);
	}
	
	public String getLabel() {
		return (this.label);
	}
	
	public int getDimension() {
		return this.dim;
	}
	
	
	public DoubleMatrix1D getPattern() {
		return (this.pattern);
	}

}
