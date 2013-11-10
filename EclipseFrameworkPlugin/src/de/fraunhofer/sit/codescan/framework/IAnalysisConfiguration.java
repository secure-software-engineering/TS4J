package de.fraunhofer.sit.codescan.framework;

/**
 * Configures a single analysis pass.
 */
public interface IAnalysisConfiguration {
	
	/**
	 * Returns the IFDS analysis plugin that actually conducts the analysis.
	 */
	IIFDSAnalysisPlugin[] getIFDSAnalysisPlugins();
	
	/**
	 * Returns the method-based analysis plugin that actually conducts the analysis.
	 */
	IMethodBasedAnalysisPlugin[] getMethodBasedAnalysisPlugins();
	
	
}
