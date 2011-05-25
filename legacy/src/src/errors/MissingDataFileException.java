package errors;

import java.io.FileNotFoundException;

public class MissingDataFileException extends FileNotFoundException {

	private static final long serialVersionUID = -5376828832252075504L;
	
	private static final String msg = "There's no data file(s) to read";

	public MissingDataFileException() {
		super(msg);
	}
	
	public MissingDataFileException(String msg) {
		super(msg);
	}

}
