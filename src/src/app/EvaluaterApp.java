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

		String filename=args[0];
		Evaluater eval = new Evaluater (filename);
		eval.runAll();
		
		
	}

}
