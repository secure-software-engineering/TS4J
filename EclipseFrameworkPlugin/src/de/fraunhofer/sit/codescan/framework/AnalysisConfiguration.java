package de.fraunhofer.sit.codescan.framework;

import org.eclipse.core.runtime.IConfigurationElement;

public abstract class AnalysisConfiguration {
	
	protected IConfigurationElement analysisConfigElement;

	public AnalysisConfiguration(IConfigurationElement analysisConfigElement) {
		this.analysisConfigElement = analysisConfigElement;
	}

	public IConfigurationElement[] getFilters() {		
		return analysisConfigElement.getChildren("filter");
	}
	
}
