package de.fraunhofer.sit.codescan.framework;

import org.eclipse.core.runtime.IConfigurationElement;

import de.fraunhofer.sit.codescan.sootbridge.IAnalysisConfiguration;
import de.fraunhofer.sit.codescan.sootbridge.IAnalysisContext;

public abstract class AnalysisConfiguration implements IAnalysisConfiguration {
	
	protected IConfigurationElement analysisConfigElement;

	public AnalysisConfiguration(IConfigurationElement analysisConfigElement) {
		this.analysisConfigElement = analysisConfigElement;
	}

	public IConfigurationElement[] getFilters() {		
		return analysisConfigElement.getChildren("filter");
	}
	
	public String getID() {
		return analysisConfigElement.getAttribute("id");
	}
	
	public String getErrorMessage() {
		return analysisConfigElement.getAttribute("errorMessage");
	}

	public String getAnalysisClass() {
		return analysisConfigElement.getAttribute("class");
	}
	public abstract void runAnalysis(IAnalysisContext context);

}
