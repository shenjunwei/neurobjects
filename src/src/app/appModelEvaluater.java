package app;

import utils.ModelEvaluater;

public class appModelEvaluater {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		new ModelEvaluater("NBayes", null, null).start();
		new ModelEvaluater("J48", null, null).start();
		new ModelEvaluater("", null, null).start();

	}

}
