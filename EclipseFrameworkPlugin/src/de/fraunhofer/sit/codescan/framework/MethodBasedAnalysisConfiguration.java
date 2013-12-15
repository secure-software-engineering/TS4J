package de.fraunhofer.sit.codescan.framework;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import de.fraunhofer.sit.codescan.sootbridge.IAnalysisContext;

public class MethodBasedAnalysisConfiguration extends AnalysisConfiguration {

	public MethodBasedAnalysisConfiguration(
			IConfigurationElement analysisConfigElement) {
		super(analysisConfigElement);
	}
	
	public IMethodBasedAnalysisPlugin createMethodBasedAnalysisPlugin() {
		try {
			return (IMethodBasedAnalysisPlugin) analysisConfigElement.createExecutableExtension("class");
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void runAnalysis(final IAnalysisContext context) {
		IMethodBasedAnalysisPlugin plugin = createMethodBasedAnalysisPlugin();
		plugin.analyzeMethod(context.getSootMethod(), context);
	}


}
