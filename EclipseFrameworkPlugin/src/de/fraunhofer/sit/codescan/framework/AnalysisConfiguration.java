package de.fraunhofer.sit.codescan.framework;

public interface AnalysisConfiguration {
	
	String getMethodSubSignature();
	String getSuperClassName();
	AnalysisPlugin getAnalysisPlugin();
}
