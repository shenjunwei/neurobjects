package app;

import DataGenerator.Properties;

public class PropertiesApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String str="animal=ge5 \n" +
				   "area=hp\n"+
				   "label=ball\n"+
				   "bin_size=250.0\n"+
				   "window_width=10";
		
		Properties P = new Properties (str);
		System.out.println (P.keys()+" VALUES "+P.values());
		P.setProperty("testeFloat", Double.toString(0.85));
		//P.setProperty("testeNull", null);
		System.out.println ("SQL: "+P.toSQLString("ioc_results_basic2"));
		P.delProperty("testeFloat");
		System.out.println ("SQL: "+P.toSQLString("ioc_results_basic2"));
		
	}

}
