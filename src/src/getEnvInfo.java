
public class getEnvInfo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Runtime runtime = Runtime.getRuntime();
	        
	        int nrOfProcessors = runtime.availableProcessors();
	        
	        System.out.println("Number of processors available to the Java Virtual Machine: " + nrOfProcessors);

	}

}
