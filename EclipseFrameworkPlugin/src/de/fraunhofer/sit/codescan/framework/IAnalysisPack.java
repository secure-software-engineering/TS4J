package de.fraunhofer.sit.codescan.framework;

/**
 * Configures a single analysis pass.
 */
public interface IAnalysisPack {
	
	IFDSAnalysisConfiguration[] getIFDSAnalysisConfigs();
	
	MethodBasedAnalysisConfiguration[] getMethodBasedAnalysisConfigs();
	
	String getNatureFilter(); 
	
}
