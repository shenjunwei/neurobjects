package utils;

import java.io.Serializable;

public class InputData implements Serializable {
	
	
	private static final long serialVersionUID = -8156561169933934581L;
	AnimalSetup 		setup=null;
	Patterns			patterns=null;
	
	
	public InputData (AnimalSetup initial) {
		
		this.setup = initial;		
	}
	
	

}
