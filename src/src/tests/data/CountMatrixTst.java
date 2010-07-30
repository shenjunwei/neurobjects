package tests.data;


import java.util.ArrayList;
import java.util.Arrays;

import javax.activity.InvalidActivityException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.colt.matrix.DoubleMatrix1D;

import data.CountMatrix;
import data.TxtSpkHandler;
import errors.InvalidArgumentException;

public class CountMatrixTst extends TestCase {
	
	TxtSpkHandler 	spikes= null;
	//CountMatrix		matrix = null;
	String 			dataSourcePath = "./setup/spikes";
	String 			filter="";
	double 			a=5818, b=5820;
	
	
	
	

	@Before
	public void setUp() throws Exception {
		spikes = new TxtSpkHandler(dataSourcePath, filter, a, b);
		
		//System.out.println(this.matrix);
	}
	
	
	@Test (expected=InvalidArgumentException.class) 
	public void testSpikesNull () throws InvalidActivityException, InvalidArgumentException {
		spikes = null;
		double 			binSize=0.050;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
	}
	
	public void test50ms() throws InvalidArgumentException, InvalidActivityException {
		double 			binSize=0.050;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		

		int matrixRef[][] = { {00,00,00,00,00,00,00,00,00,00,00,00,01,00,01,00,00,00,00,00,00,00,00,00,00,00,00,01,00,00,00,00,00,00,00,00,01,00,00,00,00},
		{00,01,02,01,00,01,01,00,00,00,00,00,00,00,03,00,00,01,01,00,00,01,00,02,00,00,01,00,00,00,02,00,00,02,00,01,02,00,01,01,00},
		{00,00,01,00,00,00,00,00,00,00,00,02,00,02,01,00,00,02,00,01,01,00,00,00,02,01,01,00,00,00,00,00,00,01,00,00,00,00,01,00,00},
		{01,00,00,00,00,00,01,00,00,00,01,00,00,01,00,00,00,00,01,00,00,00,00,00,00,02,00,00,01,01,00,00,00,01,01,00,00,00,00,01,00},
		{01,02,00,02,00,03,01,02,02,03,02,00,00,00,02,01,04,02,02,02,03,00,02,02,03,00,01,03,02,02,00,01,02,00,00,02,01,01,00,02,00},
		{01,01,01,00,00,02,02,00,01,00,00,01,00,01,01,00,00,00,00,00,01,00,00,01,00,00,00,00,00,02,00,00,00,00,01,00,00,00,00,00,00},
		{00,01,04,01,02,05,00,00,00,01,01,00,00,00,01,00,00,01,00,01,02,02,00,01,02,01,01,01,01,01,02,02,00,01,01,01,02,00,00,02,00},
		{00,00,02,01,00,01,01,00,01,00,02,01,01,00,01,00,00,01,02,00,02,02,00,01,00,01,00,00,03,00,00,02,00,01,02,02,02,01,04,00,00},
		{00,00,00,00,00,02,01,00,00,00,00,01,00,01,00,00,00,00,00,01,01,00,01,00,00,00,00,00,00,00,00,00,01,02,00,00,00,01,00,01,00},
		{02,00,01,00,00,02,01,00,00,00,00,00,01,00,00,01,00,00,01,00,02,01,01,02,00,00,00,00,00,00,00,00,00,00,00,01,01,01,01,02,00}};

		for (int i = 0; i < matrix.numRows(); i++) {
			for (int j = 0; j < matrix.numCols(); j++) {
				if (matrixRef[i][j] != matrix.get(i, j)) {
					System.out.println("Difference in: (" + i + "," + j + ")");
					
					for (int row=1;row<matrix.getNeuronsNames().length; row++) {
						System.out.println(Arrays.toString(matrixRef[row]));
					}
					System.out.println(matrix);
				}
				assertEquals(matrixRef[i][j], matrix.get(i, j));
			}
		}
	}
	
	public void test100ms() throws InvalidArgumentException, InvalidActivityException {
		double 			binSize=0.100;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		

		int matrixRef[][] = { {00,00,00,00,00,00,01,01,00,00,00,00,00,01,00,00,00,00,01,00,00},
				{01,03,01,01,00,00,00,03,01,01,01,02,00,01,00,02,02,01,02,02,00},
				{00,01,00,00,00,02,02,01,02,01,01,00,03,01,00,00,01,00,00,01,00},
				{01,00,00,01,00,01,01,00,00,01,00,00,02,00,02,00,01,01,00,01,00},
				{03,02,03,03,05,02,00,03,06,04,03,04,03,04,04,01,02,02,02,02,00},
				{02,01,02,02,01,01,01,01,00,00,01,01,00,00,02,00,00,01,00,00,00},
				{01,05,07,00,01,01,00,01,01,01,04,01,03,02,02,04,01,02,02,02,00},
				{00,03,01,01,01,03,01,01,01,02,04,01,01,00,03,02,01,04,03,04,00},
				{00,00,02,01,00,01,01,00,00,01,01,01,00,00,00,00,03,00,01,01,00},
				{02,01,02,01,00,00,01,01,00,01,03,03,00,00,00,00,00,01,02,03,00}};

		for (int i = 0; i < matrix.numRows(); i++) {
			for (int j = 0; j < matrix.numCols(); j++) {
				if (matrixRef[i][j] != matrix.get(i, j)) {
					System.out.println("Difference in: (" + i + "," + j + ")");
					
					for (int row=1;row<matrix.getNeuronsNames().length; row++) {
						System.out.println(Arrays.toString(matrixRef[row]));
					}
					System.out.println(matrix);
				}
				assertEquals(matrixRef[i][j], matrix.get(i, j));
			}
		}
	}
	
	
	public void test200ms() throws InvalidArgumentException, InvalidActivityException {
		double 			binSize=0.200;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		

		int matrixRef[][] = { {00,00,00,02,00,00,01,00,00,01,00},
				{04,02,00,03,02,03,01,02,03,04,00},
				{01,00,02,03,03,01,04,00,01,01,00},
				{01,01,01,01,01,00,02,02,02,01,00},
				{05,06,07,03,10,07,07,05,04,04,00},
				{03,04,02,02,00,02,00,02,01,00,00},
				{06,07,02,01,02,05,05,06,03,04,00},
				{03,02,04,02,03,05,01,05,05,07,00},
				{00,03,01,01,01,02,00,00,03,02,00},
				{03,03,00,02,01,06,00,00,01,05,00}};

		for (int i = 0; i < matrix.numRows(); i++) {
			for (int j = 0; j < matrix.numCols(); j++) {
				if (matrixRef[i][j] != matrix.get(i, j)) {
					System.out.println("Difference in: (" + i + "," + j + ")");
					
					for (int row=1;row<matrix.getNeuronsNames().length; row++) {
						System.out.println(Arrays.toString(matrixRef[row]));
					}
					System.out.println(matrix);
				}
				assertEquals(matrixRef[i][j], matrix.get(i, j));
			}
		}
	}

	
	public void test250ms() throws InvalidArgumentException, InvalidActivityException {
		double 			binSize=0.250;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		
		
		int matrixRef[][] = {{00,00,02,00,00,01,00,01,00},
				{04,02,03,02,03,01,04,05,00},
				{01,00,05,03,03,02,01,01,00},
				{01,01,02,01,00,04,02,01,00},
				{05,11,04,11,10,8,03,06,00},
				{03,05,03,00,02,02,01,00,00},
				{8,06,02,02,07,05,06,05,00},
				{03,03,05,03,05,04,05,9,00},
				{00,03,02,01,02,00,03,02,00},
				{03,03,01,02,06,00,00,06,00}};
		
		
		for (int i=0; i<matrix.numRows(); i++) {
			for (int j=0; j<matrix.numCols(); j++) {
				if (matrixRef[i][j]!=matrix.get (i,j)) {
					System.out.println ("Difference in: ("+i+","+j+")");
				}
				assertEquals(matrixRef[i][j], matrix.get (i,j));
			}
		}
			
	}
	
	public void test500ms() throws InvalidArgumentException, InvalidActivityException {
		double 			binSize=0.500;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		
		
		int matrixRef[][] = {{00,02,01,01,00},
				{06,05,04,9,00},
				{01,8,05,02,00},
				{02,03,04,03,00},
				{16,15,18,9,00},
				{8,03,04,01,00},
				{14,04,12,11,00},
				{06,8,9,14,00},
				{03,03,02,05,00},
				{06,03,06,06,00}};
		
		
		for (int i=0; i<matrix.numRows(); i++) {
			for (int j=0; j<matrix.numCols(); j++) {
				if (matrixRef[i][j]!=matrix.get (i,j)) {
					System.out.println ("Difference in: ("+i+","+j+")");
				}
				assertEquals(matrixRef[i][j], matrix.get (i,j));
			}
		}
		//ArrayList<DoubleMatrix1D> data=	this.matrix.getPatterns(a, b);
		
		
			
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
	 * @throws InvalidArgumentException 
	 * @throws InvalidActivityException 
	 * */
	public void testGetIdx() throws InvalidActivityException, InvalidArgumentException {
		double 			binSize=0.050;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		Assert.assertEquals(0, matrix.getIdx(a));
		Assert.assertEquals(1, matrix.getIdx(a+binSize));
		Assert.assertEquals(-1, matrix.getIdx(a - 0.001));
		Assert.assertEquals(-1, matrix.getIdx(b + 0.001));
		Assert.assertEquals(-1, matrix.getIdx(a - 100));
		Assert.assertEquals(-1, matrix.getIdx(b + 100)); 
		
	}
	
	public void testGetNeuronsNames() throws InvalidActivityException, InvalidArgumentException {
		double 			binSize=0.050;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		String names[]={"hp_02a","hp_12a", "hp_12b", "s1_03a", "s1_07a", "s1_08c", "v1_04a","v1_09a","v1_12a","v1_16a"};
		
		String actual[] = matrix.getNeuronsNames();
		for (int i=0; i<names.length; i++) {
			Assert.assertTrue(actual[i].equals(names[i]));
		}
	}
	
	public void testGetPatterns () throws InvalidActivityException, InvalidArgumentException {
		double 			binSize=0.250;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		double patts[][] = {{00,00,04,02,01,00,01,01,05,11,03,05,8,06,03,03,00,03,03,03},
				{00,02,02,03,00,05,01,02,11,04,05,03,06,02,03,05,03,02,03,01},
				{02,00,03,02,05,03,02,01,04,11,03,00,02,02,05,03,02,01,01,02},
				{00,00,02,03,03,03,01,00,11,10,00,02,02,07,03,05,01,02,02,06},
				{00,01,03,01,03,02,00,04,10,8,02,02,07,05,05,04,02,00,06,00},
				{01,00,01,04,02,01,04,02,8,03,02,01,05,06,04,05,00,03,00,00},
				{00,01,04,05,01,01,02,01,03,06,01,00,06,05,05,9,03,02,00,06},
				{01,00,05,00,01,00,01,00,06,00,00,00,05,00,9,00,02,00,06,00}};
		
		ArrayList<DoubleMatrix1D> actual = matrix.getPatterns(a, b);
		double tmp[] = null;
		int patts_num_rows = patts.length;
		if (actual.size()!=(patts.length)) {
			System.out.println ("Different sizes");
			Assert.assertEquals(actual.size(), patts.length);
			
		}
		for (int i=0; i<actual.size(); i++ ) {
			tmp = actual.get(i).toArray();
			if (!Arrays.equals(tmp, patts[i])) {
				System.out.println ("Different in: "+i);
				System.out.println ("Test patterns: "+Arrays.toString(patts[i]));
				System.out.println ("Current patterns: "+Arrays.toString(tmp));
				Assert.assertTrue(Arrays.equals(actual.get(i).toArray(), patts[i]));
			}
			
			
		}
		
		
		
	}
	public void testWindowPossible() throws InvalidActivityException, InvalidArgumentException {

		double 			binSize=0.250;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		
		Assert.assertEquals(true,matrix.windowPossible(a, b-a));
		Assert.assertEquals(true,matrix.windowPossible(a, (b-a)/2));
		Assert.assertEquals(false,matrix.windowPossible(a, (b-a)+1));
		}
	
	
	public void testPossibleInterval () throws InvalidActivityException, InvalidArgumentException {
		
		double 			binSize=0.250;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		
		Assert.assertEquals(true,matrix.possibleInterval(a, b));
		Assert.assertEquals(false,matrix.possibleInterval(a-1, b));
		Assert.assertEquals(false,matrix.possibleInterval(a, b+1));
			
		
	}
	
	public void testIncCursor() throws InvalidActivityException, InvalidArgumentException {
		
		double 			binSize=0.250;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		
		Assert.assertEquals(true,matrix.incCursor(0));
		Assert.assertEquals(false,matrix.incCursor(-1));
		Assert.assertEquals(a,matrix.getCursor());
		Assert.assertEquals(false,matrix.incCursor(matrix.numCols()-1));
	}
	

	public void testAvgs() throws InvalidActivityException, InvalidArgumentException {
		
		double 			binSize=0.250;
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2);
		double avgCols[] = {2.8, 3.4, 2.9, 2.5, 3.8, 2.7, 2.5, 3.6, 0};
		double avgRows[]={0.4444,2.6667,1.7778,1.3333,6.4444,1.7778,4.5556,4.1111,1.4444,2.3333};
		int i=0;
		
		for (i=0; i<matrix.numCols(); i++) {
			Assert.assertEquals(true,Math.abs(avgCols[i]-matrix.avgColumn(i))<0.001);
		}
		for (i=0; i<matrix.numRows(); i++) {
			Assert.assertEquals(true,(Math.abs(matrix.avgRow(i)-avgRows[i])<0.001));
		}
	
}



	@After
	public void tearDown() throws Exception {
		spikes = null;
		
	}

}
