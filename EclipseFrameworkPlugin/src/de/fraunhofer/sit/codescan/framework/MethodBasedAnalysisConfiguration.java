package de.fraunhofer.sit.codescan.framework;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class MethodBasedAnalysisConfiguration extends AnalysisConfiguration {

	public MethodBasedAnalysisConfiguration(
			IConfigurationElement analysisConfigElement) {
		super(analysisConfigElement);
	}
	
	public IMethodBasedAnalysisPlugin createIFDSPlugin() {
		try {
			return (IMethodBasedAnalysisPlugin) analysisConfigElement.createExecutableExtension("class");
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}


}
