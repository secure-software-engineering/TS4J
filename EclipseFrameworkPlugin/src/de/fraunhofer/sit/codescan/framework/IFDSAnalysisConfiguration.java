package de.fraunhofer.sit.codescan.framework;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class IFDSAnalysisConfiguration extends AnalysisConfiguration{

	public IFDSAnalysisConfiguration(
			IConfigurationElement analysisConfigElement) {
		super(analysisConfigElement);
	}
	
	public IIFDSAnalysisPlugin createIFDSPlugin() {
		try {
			return (IIFDSAnalysisPlugin) analysisConfigElement.createExecutableExtension("class");
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

}
