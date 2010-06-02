package app;

import java.lang.reflect.Array;
import java.util.Arrays;

import utils.BehavHandlerFile;
public class BehavHandlerFileApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		BehavHandlerFile BHF = new BehavHandlerFile ("/tmp/behave.txt");
		
		
		BHF.sort();
		System.out.println(BHF);
		
		System.out.println("Big Interval for 'A,B': "+Arrays.toString(BHF.getBigInterval("A,B")));

	}

}
