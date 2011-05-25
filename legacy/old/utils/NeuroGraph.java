package utils;

import cern.colt.matrix.DoubleMatrix2D;
import java.util.ArrayList;
import java.util.Iterator;

public class NeuroGraph {
	
	
	public 	DoubleMatrix2D			A=null;
	public 	DoubleMatrix2D			R=null;
	private double					th=0.0;
	private int						numNodes;
	private String					nodeNames[];
	private ArrayList<String> 		ntcList = new ArrayList<String>();
	private double 					time =0.0;
	
	
	
	public NeuroGraph (DoubleMatrix2D R, String names[], double th){
		
		this.A = (DoubleMatrix2D) R.clone();
		//this.R = (DoubleMatrix2D) R.clone();
		this.th = th;
		this.nodeNames = names.clone();
		//this.A.assign(0);
		double value;
		int lastRow = (this.A.rows()-1);
		int lastCol = this.A.columns();
		for (int row=0; row<lastRow; row++) {
			for (int col=row+1; col<lastCol; col++) {
				value = Math.abs(this.A.getQuick(row, col))>th?1:0;
				this.A.setQuick(row, col, value);
				A.setQuick(col,row, value);
			}
		}
		this.numNodes = A.rows();
		
		
	}
	
	public NeuroGraph (DoubleMatrix2D R, String names[], double th,double time){
		
		this.A = (DoubleMatrix2D) R.clone();
		//this.R = (DoubleMatrix2D) R.clone();
		this.th = th;
		this.nodeNames = names.clone();
		this.time = time;
		//this.A.assign(0);
		double value;
		int lastRow = (this.A.rows()-1);
		int lastCol = this.A.columns();
		/* for (int row=0; row<lastRow; row++) {
			for (int col=row+1; col<lastCol; col++) {
				value = Math.abs(this.A.getQuick(row, col))>th?1:0;
				this.A.setQuick(row, col, value);
				A.setQuick(col,row, value);
			}
		} */
		this.numNodes = A.rows();
		
		
	}
	public NeuroGraph (DoubleMatrix2D R){
		
		this.A = (DoubleMatrix2D) R.clone();
		//this.A.assign(0);
		this.numNodes = A.rows();
		
		
	}
	
	public String toString () {
		
		return (A.toString());
	}
	
	public void show(){
		System.out.println (this.A.toString());
	}
	
	public boolean isNeighbor(int x, int y) {
		if ( Math.abs(this.A.getQuick(x, y))>this.th) {
			return true;
		}
		return false;
	}
	
	public void buildNTCList() {
		
		boolean i_j = false;
		for (int i=0; i<this.numNodes; i++){
			for (int j=0; j<this.numNodes; j++){
				i_j = false;
				if (i!=j) {
					i_j = this.isNeighbor(i,j);
				}
				for (int k=0; k<this.numNodes; k++){
					if ( (i_j) && (this.isNeighbor(i, k)) && (this.isNeighbor(k,j)) && (k!=i) && (k!=j) ) {
						this.addNTCList(i, j, k);
					}
				}
			}
		}
	}
	
	public void showNTCList() {
		System.out.println("Size:"+this.ntcList.size());
		System.out.println(this.ntcList.toString());
	}
	
	// Insert NEW triads into NTCList  
	private void addNTCList (int i,int j, int k) {
		String 					node[]={"","",""};
		String 					triad = "";
		
		node[0] = Integer.toString(i);
		node[1] = Integer.toString(j);
		node[2] = Integer.toString(k);
		java.util.Arrays.sort(node);
		triad = node[0]+";"+node[1]+";"+node[2];
		
		if (!this.ntcList.contains(triad)) {
			this.ntcList.add(triad);
		}
		
		
	}
	
	private String ntc2SQL (String tableName, int i,int j, int k) {
		
		  
        String query = "INSERT INTO " +tableName + " (time,th,a,b,c) "+
                        "VALUES ("+this.time+",'"+this.th+"','"+
                        this.nodeNames[i]+"','"+
                        this.nodeNames[j]+"','"+
                        this.nodeNames[k]+"')";

    //    System.out.println(query);
        return(query);
		
	}
	
	public String ntcList2SQL (String tableName) {
		
		Iterator<String> itr = this.ntcList.iterator();
		
		String query = "";
		String triad = "";
		String[] nodes = new String[3];
		
		while (itr.hasNext()) {
			  triad =  itr.next();;
			  nodes = triad.split(";");
		      query = query +";\n"+this.ntc2SQL(tableName, Integer.parseInt(nodes[0]),Integer.parseInt(nodes[1]), Integer.parseInt(nodes[2]));
		      
		    }
		
		return (query);
	} 
	
	
	public int getSizeNTC() {
		return (this.ntcList.size());
	}
	

}
