package apps;
import utils.*;
import java.io.*;
//import cern.colt.matrix.doublealgo.Statistic;
//import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix2D;

public class appCountMatrix {

	/**
	 * @param args
	
	 *
	 */
	
	public static BufferedWriter createOutFile (String filename) {
		BufferedWriter out = null;
		try { out = new BufferedWriter(new FileWriter(filename)); ; 
		// Create file if it does not exist 
		
		} catch (IOException e) {
			
		} 
		return out;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
	//	final double pre_a = 3473;
	//	final double intervalos[] = { 3473-1200, 3473, 3473+1200};
				
		
		
		final double a = 1000;
		final double b = 3000;
		final double binSize = .25; //seconds
		final double th = 0.4;
		final double width = 10; // seconds
		String dataSetPath = "/home/nivaldo/projects/crnets/data/spikes/ge5/01/s1";
		String fileNTC = "ntc"+a+"_"+b+".txt";
		String fileQuery = "query"+a+"_"+b+".sql";
		
		BufferedWriter outNTC = createOutFile(fileNTC);
		BufferedWriter outQuery = createOutFile(fileQuery);
		
	//	double values [][] = {{1,  1,   1, 1} , {1,  1,   1, 0},{1,  1,   1, 1},{1,  0,   1, 1}};

		CountMatrix matrix = new CountMatrix (dataSetPath,binSize,a,b);
		if (matrix.isValid()) {
		
			//matrix.show();
		//	matrix.showMatrix2D();
		//	matrix.showNeuronNames();
			
		}
		else {
			System.out.println("Problems in spike count !!\n"+matrix.getLog());
			return;
		}
		double time = a;
		int numberOfNTC = 0;
		int totalNTC = 0;
		String query = "";
		
		while (matrix.windowPossible(time, width)){
			DoubleMatrix2D R = CountRuler.measure(matrix.matrix, matrix.getIdx(time), matrix.getIdx(a+width), CountRuler.PEARSON);
			NeuroGraph G = new NeuroGraph (R,matrix.getNeuronsNames(),th,time);
			G.buildNTCList();
			numberOfNTC = G.getSizeNTC();
			totalNTC += numberOfNTC;
			query = query + G.ntcList2SQL("ioc_results_ntc");
			//System.out.println(numberOfNTC);
			time+=binSize;
			outNTC.write(time+"\t"+numberOfNTC+"\n");
			System.out.println(time+"\t"+numberOfNTC);
		}
		System.out.println("\n\nTotal: "+ totalNTC);
		//System.out.println(query);
		outQuery.write(query);
		outNTC.close();
		outQuery.close();
		
		//DoubleMatrix2D R = CountRuler.measure(matrix.matrix, 0, 3, CountRuler.PEARSON);
		//System.out.println(R.toString());
		//DoubleMatrix2D B = new DenseDoubleMatrix2D(values);
		//NeuroGraph G = new NeuroGraph (B);
//		/NeuroGraph G = new NeuroGraph (R,matrix.getNeuronsNames(),th);
		//G.show();
		//G.buildNTCList();
	//	G.showNTCList();
		
		
		
		
	}
	
	

}
