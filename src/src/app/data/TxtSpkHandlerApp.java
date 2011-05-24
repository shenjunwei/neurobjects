package app.data;

import java.io.IOException;
import java.util.ArrayList;

import data.CountMatrix;
import data.TxtSpkHandler;
import errors.MissingDataFileException;


public class TxtSpkHandlerApp {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MissingDataFileException 
	 * @throws IllegalArgumentException 
	 */
	public static void main(String[] args) throws MissingDataFileException, IOException {
		// TODO Auto-generated method stub
		
		String path = "/home/nivaldo/projects/crnets/data/spikes/ge5/01/hp";
		String filter = "";
		double a=0;
		double b=10;
		
		TxtSpkHandler spikes = new TxtSpkHandler (path, filter, a, b );
		CountMatrix   matrix = new CountMatrix (spikes,0.25);
		matrix.setWindowWidth(4);
		System.out.println(spikes.getNeuronNames());
		System.out.println(matrix);
		
		
		ArrayList <double[]> patterns = matrix.getPatterns(0,5);
		double[] p;
		
		if (patterns!=null) {
		for (int i=0; i<patterns.size();i++) {
			p = patterns.get(i);
			System.out.println(p);
			
		}
		}
		else {
			System.out.println("Seems that there is no pattern. Please check intervals, path, files, etc");
		}
		
		

	}

}
