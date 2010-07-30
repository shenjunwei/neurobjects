package tests.data;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import javax.activity.InvalidActivityException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

import data.CountMatrix;
import data.Pattern;
import data.Patterns;
import data.TxtSpkHandler;
import errors.InvalidArgumentException;

public class PatternsTst {
	
	TxtSpkHandler 	spikes= null;
	//CountMatrix		matrix = null;
	String 			dataSourcePath = "./setup/spikes";
	String 			filter="";
	double 			a=5818, b=5820;
	double 			binSize=0.250;
	double patts[][] = {{00,00,04,02,01,00,01,01,05,11,03,05,8,06,03,03,00,03,03,03},
			{00,02,02,03,00,05,01,02,11,04,05,03,06,02,03,05,03,02,03,01},
			{02,00,03,02,05,03,02,01,04,11,03,00,02,02,05,03,02,01,01,02},
			{00,00,02,03,03,03,01,00,11,10,00,02,02,07,03,05,01,02,02,06},
			{00,01,03,01,03,02,00,04,10,8,02,02,07,05,05,04,02,00,06,00},
			{01,00,01,04,02,01,04,02,8,03,02,01,05,06,04,05,00,03,00,00},
			{00,01,04,05,01,01,02,01,03,06,01,00,06,05,05,9,03,02,00,06},
			{01,00,05,00,01,00,01,00,06,00,00,00,05,00,9,00,02,00,06,00}};
	

	@Before
	public void setUp() throws Exception {
		spikes = new TxtSpkHandler(dataSourcePath, filter, a, b);
		CountMatrix     matrix = new CountMatrix(spikes, binSize);
		matrix.setWindowWidth(2); 
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPatterns() {
		data.Patterns P = new data.Patterns (); 
		P.setNeuronNames(this.spikes.getNeuronNames());
		P.setSingleDimension(true);
		Assert.assertEquals(0, P.size());
		Assert.assertEquals(0, P.size("teste"));
		Assert.assertEquals(0, P.size("ball"));
		System.out.println ("'Patterns()' constructor [OK]");
		
	}

	@Test  
	public void testAddPatternExcpNullPat() throws InvalidActivityException, InvalidArgumentException {
		Pattern pat = null;
		Patterns pats = new Patterns ();
		Assert.assertEquals(0, pats.size());
		try {
			pats.addPattern(pat);
		} catch (NullPointerException e) {
			System.out
					.println("NullPointerException Exception when a null pointer is given as input [OK]");
		}
		
	}
	
	@Test
	public void testAddPattern() throws InvalidActivityException, InvalidArgumentException {
		
		Pattern pat = null;
		Patterns pats = new Patterns (); 
		ArrayList<Pattern> list = null;
		double time = a;
		// Creates and fills a set of patterns 
		for (int i=0; i<patts.length; i++, time+=binSize) {
			pat = new Pattern (patts[i], "teste",time);
			pats.addPattern(pat);
		}
		// Gets a list of patterns
		list = pats.getPatterns("teste");

		// Checks size
		Assert.assertEquals(patts.length, pats.size());
		
		// Checks each pattern
		for (int i=0; i<patts.length; i++) {
			Assert.assertTrue (Arrays.equals (list.get(i).toArray(),patts[i]));
		}
		System.out.println ("'public void addPattern (Pattern pat)' method [OK]");
	}

	@Test
	public void testAddPatterns() throws InvalidActivityException, InvalidArgumentException {
		ArrayList<DoubleMatrix1D> list = new ArrayList<DoubleMatrix1D> ();
		for (int i=0; i<patts.length; i++) {
			list.add(new DenseDoubleMatrix1D (patts[i]));
		}
		Patterns pats = new Patterns ();
		pats.addPatterns(list,"teste",a,binSize);
		Assert.assertEquals(patts.length, pats.size());
		for (int i=0; i<patts.length; i++) {
			Assert.assertTrue (Arrays.equals (list.get(i).toArray(),patts[i]));
		}
		System.out.println ("'public void addPatterns (ArrayList<DoubleMatrix1D> ps, String labels, double time, double timeStep)' method [OK]");
	}

	@Test
	public void testGetPattern() throws InvalidActivityException, InvalidArgumentException {
		ArrayList<Pattern> list = new ArrayList<Pattern> ();
		ArrayList<DoubleMatrix1D> list1d = new ArrayList<DoubleMatrix1D> ();
		double time=a;
		// Creates a list of patterns 
		for (int i=0; i<patts.length; i++, time+=.25) {
			list.add(new Pattern (patts[i], "teste",time));
		}
		for (int i=0; i<patts.length; i++) {
			list1d.add(new DenseDoubleMatrix1D (patts[i]));
		}
		Patterns pats = new Patterns ();
		pats.addPatterns(list1d,"teste",a,binSize);
		
		Assert.assertEquals(patts.length, pats.size());
		for (int i=0; i<pats.size(); i++) {
			Assert.assertTrue(pats.getPattern("teste", i).equals(list.get(i)));
		}
		System.out.println ("'public Pattern getPattern (String label, int index)' method [OK]");
	}

	@Test
	public void testGetPatterns() throws InvalidActivityException, InvalidArgumentException {
		ArrayList<Pattern> list = new ArrayList<Pattern> ();
		ArrayList<Pattern> listTest = null;
		ArrayList<DoubleMatrix1D> list1d = new ArrayList<DoubleMatrix1D> ();
		double time=a;
		// Creates a list of patterns 
		for (int i=0; i<patts.length; i++, time+=.25) {
			list.add(new Pattern (patts[i], "teste",time));
		}
		for (int i=0; i<patts.length; i++) {
			list1d.add(new DenseDoubleMatrix1D (patts[i]));
		}
		Patterns pats = new Patterns ();
		pats.addPatterns(list1d,"teste",a,binSize);
		
		Assert.assertEquals(patts.length, pats.size());
		listTest = pats.getPatterns("teste");
		for (int i=0; i<pats.size(); i++) {
			Assert.assertTrue(listTest.get(i).equals(list.get(i)));
		}
		System.out.println ("'public ArrayList<Pattern> getPatterns (String label)' method [OK]");
	}

	

}
