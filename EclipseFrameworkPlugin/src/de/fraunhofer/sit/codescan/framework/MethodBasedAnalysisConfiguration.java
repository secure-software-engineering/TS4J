package de.fraunhofer.sit.codescan.framework;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import soot.Local;
import soot.jimple.Stmt;

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
	public void registerAnalysis(final IAnalysisContext context) {
		final boolean[] vulnerable = new boolean[] { true };
		IMethodBasedAnalysisPlugin plugin = createMethodBasedAnalysisPlugin();
		plugin.analyzeMethod(context.getSootMethod(), new MethodBasedAnalysisManager() {			
			public boolean mustAlias(Stmt stmt, Local l1, Stmt stmt2, Local l2) {
				return context.getMustAliasManager().mustAlias(stmt, l1, stmt2, l2);
			}
			
			public void markMethodAsBenign() {
				vulnerable[0] = false; 
			}
		});
		if(vulnerable[0]) {
			markMethodAsVulnerable(context);
		}		
	}


}
