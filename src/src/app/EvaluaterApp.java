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

		String filename = "";
		String jobId = "";
		String taskId = "";
		for (int i = 0; i < args.length; i++) {
			System.out.println(i+" :"+args[i]);
		}
		if (args.length >= 3) {
		
			filename = args[0].trim();
			jobId = args[1].trim();
			taskId = args[2].trim();
		}
		// String filename="/tmp/tmp/ge5.hp.1551156138.0.zip";
		Evaluater eval = new Evaluater (filename);
		eval.runAll(jobId,taskId);

	}

}
