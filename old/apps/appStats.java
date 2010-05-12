package apps;

import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix2D;


public class appStats {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		double values [][] = {{1,  2,   3} ,{2,  4,   6}, {3 , 6 ,  9}, {4, -8 ,-10 }}; 
		DoubleMatrix2D A = new DenseDoubleMatrix2D (values);
		
		//cern.colt.matrix.doublealgo.Statistic S = new cern.colt.matrix.doublealgo.Statistic();
		DoubleMatrix2D C =  cern.colt.matrix.doublealgo.Statistic.covariance(A);
		DoubleMatrix2D R =  cern.colt.matrix.doublealgo.Statistic.correlation(C);
		R = cern.colt.matrix.doublealgo.Statistic.distance(A,cern.colt.matrix.doublealgo.Statistic.MANHATTAN);
		
		for (int row=0; row<R.rows(); row++ ) {
			System.out.println("\n");
			for (int cols=0; cols<R.columns(); cols++ ) {
				System.out.print(R.get(row, cols)+"\t\t\t\t");
			}
		}
 		

	}

}
