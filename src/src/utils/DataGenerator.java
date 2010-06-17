package utils;

public class DataGenerator  extends Thread{
	
	DataSetBuilder 		data = null;
	DatasetBuffer 		buffer = null;
	AnimalSetup	   		animal = null;
	int					numberOfSamples = 0;
	boolean 			done=false;
	 
	
	public DataGenerator (AnimalSetup s, DatasetBuffer buffer, int numSamples) {
		
		this.animal = s;
		this.buffer = buffer;
		this.numberOfSamples = numSamples;
		this.data = new DataSetBuilder(s);
		
	}
	
	public void run () {
		try {
			this.data.run(this.buffer, this.numberOfSamples);
			this.done = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public boolean isDone() {
		return done;
	}
	

}
