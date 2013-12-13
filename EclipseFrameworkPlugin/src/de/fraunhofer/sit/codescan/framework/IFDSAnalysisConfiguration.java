package de.fraunhofer.sit.codescan.framework;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import heros.solver.IFDSSolver;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
import de.fraunhofer.sit.codescan.framework.internal.analysis.IFDSAdapter;

public class IFDSAnalysisConfiguration extends AnalysisConfiguration {

	public IFDSAnalysisConfiguration(
			IConfigurationElement analysisConfigElement) {
		super(analysisConfigElement);
	}
	
	public IIFDSAnalysisPlugin createIFDSAnalysisPlugin() {
		try {
			return (IIFDSAnalysisPlugin) analysisConfigElement.createExecutableExtension("class");
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void registerAnalysis(IAnalysisContext context) {
		IFDSAnalysisConfiguration analysisConfiguration = (IFDSAnalysisConfiguration) context.getAnalysisConfiguration();
		IIFDSAnalysisPlugin ifdsAnalysisPlugin = analysisConfiguration.createIFDSAnalysisPlugin();
		IFDSAdapter adapter = new IFDSAdapter(context);
		IFDSTabulationProblem<Unit, ?, SootMethod, InterproceduralCFG<Unit, SootMethod>> ifdsProblem =
				ifdsAnalysisPlugin.createAnalysisProblem(adapter);
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		IFDSSolver<Unit, ?, SootMethod, InterproceduralCFG<Unit, SootMethod>> solver = new JimpleIFDSSolver(ifdsProblem);
		solver.solve();
		
		if(adapter.isMethodVulnerable()) {
			markMethodAsVulnerable(context);
		}		
	}
}
