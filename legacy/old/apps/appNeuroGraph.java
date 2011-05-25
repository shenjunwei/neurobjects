package apps;

import utils.*;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix2D;

public class appNeuroGraph {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		double values [][] = {{1,  1,   1, 1,0} , {1,  1,   1, 0,1},{1,  1,   1, 1,1},{1,  0,   1, 1,0},{0,  1,   1, 0,1}};

		DoubleMatrix2D B = new DenseDoubleMatrix2D(values);
		NeuroGraph G = new NeuroGraph (B);
		//NeuroGraph G = new NeuroGraph (R,matrix.getNeuronsNames(),th);
		G.show();
		G.buildNTCList();
		G.showNTCList();


	}

}
