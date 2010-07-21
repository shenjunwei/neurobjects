package tests;


import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import data.CountMatrix;
import data.TxtSpkHandler;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

public class CountMatrixTest extends TestCase {
	
	TxtSpkHandler 	spikes= null;
	CountMatrix		matrix = null;
	String 			dataSourcePath = "./setup/spikes";
	String 			filter="";
	double 			a=5818, b=5837;
	double 			binSize=0.250;
	//String			neuronNames[] = {"HP_02a","HP_12a", "HP_12b", "S1_03a", "S1_07a", "S1_08c", "V1_04a"
	
	

	@Before
	public void setUp() throws Exception {
		spikes = new TxtSpkHandler(dataSourcePath, filter, a, b);
		matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(10);
	}
	
	/** Tests the getIdx methods 
	 * 
	 * Case: returns 0 if is given a as time;
	 * Case: returns 1 if is given a+binSize as time;
	 * Case: returns -1 if is given (a-0.001) as time;
	 * Case: returns -1 if is given (b+0.001) as time;
	 * Case: returns -1 if is given (a-100) as time;
	 * Case: returns -1 if is given (b+100) as time;
	 * Case: returns 0 if is given a as time;
	 * Case: returns 0 if is given a as time;
	 * */
	public void testGetIdx() {
		Assert.assertEquals(0, matrix.getIdx(a));
		Assert.assertEquals(1, matrix.getIdx(a+binSize));
		Assert.assertEquals(-1, matrix.getIdx(a - 0.001));
		Assert.assertEquals(-1, matrix.getIdx(b + 0.001));
		Assert.assertEquals(-1, matrix.getIdx(a - 100));
		Assert.assertEquals(-1, matrix.getIdx(b + 100));
	}
	
	/** Tests the getLen methods 
	 * 
	 * Case: returns -1 if is given -1 as time;
	 * Case: returns 0 if is given 0 as time;
	 * Case: returns 1 if is given binSize as time;
	 * Case: returns matrix.numCols() if is given (b-a) as time;
	 * Case: returns -1 if is given (a-100) as time;
	 * Case: returns -1 if is given (b+100) as time;
	 * Case: returns 0 if is given a as time;
	 * Case: returns 0 if is given a as time;
	 * */
	public void testGetLen() {
		Assert.assertEquals(-1, matrix.getLen(-1));
		Assert.assertEquals(0, matrix.getLen(0));
		Assert.assertEquals(1, matrix.getLen(binSize));
		Assert.assertEquals(matrix.numCols(), matrix.getLen(b-a));
		
	}
	
	public void testGetPattern () {
		
		matrix.resetCursor();
		DoubleMatrix1D p = matrix.getPattern();
		Assert.assertEquals(3.1,p.zSum()/p.size());
		Assert.assertTrue(matrix.hasNext());
		Assert.assertTrue(matrix.incCursor(1));
		p = matrix.getPattern();
		Assert.assertEquals(3.07,p.zSum()/p.size());
		Assert.assertTrue(matrix.hasNext());
		
		
		
	}
	
	

	@After
	public void tearDown() throws Exception {
		spikes = null;
		matrix = null;
	}

}
