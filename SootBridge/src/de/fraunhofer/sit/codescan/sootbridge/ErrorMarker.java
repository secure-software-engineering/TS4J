package de.fraunhofer.sit.codescan.sootbridge;

public class ErrorMarker {
	
	protected final String errorMessage;		
	protected final String className;	
	protected final int lineNumber;	

	public ErrorMarker(String errorMessage, String className, int lineNumber) {
		this.errorMessage = errorMessage;
		this.className = className;
		this.lineNumber = lineNumber;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getClassName() {
		return className;
	}

	public int getLineNumber() {
		return lineNumber;
	}

}
