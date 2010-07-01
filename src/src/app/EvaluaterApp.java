package app;
import java.io.IOException;

import eval.Evaluater;

public class EvaluaterApp {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		// ge5.hp.431546457.0.zip
		String filename=args[0];
		//String filename="/tmp/tmp/ge5.hp.1551156138.0.zip";
		Evaluater eval = new Evaluater (filename);
		eval.runAll();
		
		
	}

}
