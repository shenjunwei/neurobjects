 	 	package app;
 	 	/**
 	 	 * Main.java
 	 	 *
 	 	 * @author www.javadb.com
 	 	 */
 	 	public class Processors {
 	 	    
 	 	    /**
 	 	     * Displays the number of processors available in the Java Virtual Machine
 	 	     */
 	 	    public void displayAvailableProcessors() {
 	 	        
 	 	        Runtime runtime = Runtime.getRuntime();
 	 	        
 	 	        int nrOfProcessors = runtime.availableProcessors();
 	 	        
 	 	        System.out.println("Number of processors available to the Java Virtual Machine: " + nrOfProcessors);
 	 	        
 	 	    }
 	 	    
 	 	    /**
 	 	     * Starts the program
 	 	     *
 	 	     * @param args the command line arguments
 	 	     */
 	 	    public static void main(String[] args) {
 	 	        new Processors().displayAvailableProcessors(); 
 	 	    }
 	 	}
