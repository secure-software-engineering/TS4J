package de.fraunhofer.sit.codescan.framework;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.IMethod;

import de.fraunhofer.sit.codescan.framework.internal.Constants;

public abstract class AnalysisConfiguration {
	
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
	
	
	protected void markMethodAsVulnerable(IAnalysisContext context) {
		IMethod method = context.getMethod();		
		IResource erroneousFile = method.getResource();
		IMarker marker;
		try {
			marker = erroneousFile.createMarker(Constants.MARKER_TYPE);
			marker.setAttribute(IMarker.SEVERITY,IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			marker.setAttribute(IMarker.LINE_NUMBER, context.getSootMethod().getJavaSourceStartLineNumber());
			marker.setAttribute(IMarker.USER_EDITABLE, false);
			marker.setAttribute(IMarker.MESSAGE, getErrorMessage());
			marker.setAttribute(Constants.MARKER_ATTRIBUTE_ANALYSIS_ID, getID());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void registerAnalysis(IAnalysisContext context);

}
