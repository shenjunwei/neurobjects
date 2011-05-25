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
		String ntpHost = "";
		int i=0;
		for (i = 0; i < args.length; i++) {
			System.out.println(i+" :"+args[i]);
		}
		
		if (args.length == 4) {
			i = 0;
			filename = args[i++].trim();
			ntpHost = args[i++].trim();
			jobId = args[i++].trim();
			taskId = args[i++].trim();			
		}
		else {
			System.err.println ("Syntax error: EvaluaterApp <filename> <ntpHost> <job> <task> ");
			return;
		}
		// String filename="/tmp/tmp/ge5.hp.1551156138.0.zip";
		Evaluater eval = new Evaluater (filename,ntpHost);
		eval.runAll(jobId,taskId);

	}

}
