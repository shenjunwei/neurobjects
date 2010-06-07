package app;

import java.lang.reflect.Array;
import java.util.Arrays;

import utils.BehavHandlerFile;
import utils.CountMatrix;
import utils.TxtSpkHandler;
public class BehavHandlerFileApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		BehavHandlerFile BHF = new BehavHandlerFile ("/home/nivaldo/tmp/nda/ge5_contacts.txt");
		//String labels = "ball,brush,food,urchin";
		String labels = "ball,brush,food,urchin";
		double bigInterval[]={0,0};
		String path = "/home/nivaldo/projects/crnets/data/spikes/ge5/01/all";
		String filter = "s1";
		
		
		
		if (!BHF.isValid()) {
			return;
		}
		BHF.sort();
		System.out.println(BHF);
		bigInterval = BHF.getBigInterval(labels);
		System.out.println("Big Interval for '"+labels+"': "+Arrays.toString(bigInterval));
		TxtSpkHandler spikes = new TxtSpkHandler (path, filter, bigInterval[0], bigInterval[1]);
		CountMatrix   matrix = new CountMatrix (spikes,0.25);
		//Patterns P = new Patterns ()
		//System.out.println(matrix);
		
		

	}

}
