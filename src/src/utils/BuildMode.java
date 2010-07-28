package utils;

public enum BuildMode {
	
	EQUALS,RANDOM;
	
	public String toString() {
		
		
		switch (this){
		case EQUALS:
			return ("equals");
		case RANDOM:
			return ("random");
		default:
			return ("");
		}
		
		
	}

}
