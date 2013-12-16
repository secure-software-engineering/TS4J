package de.fraunhofer.sit.codescan.framework;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import heros.solver.IFDSSolver;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
import de.fraunhofer.sit.codescan.sootbridge.IAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class IFDSAnalysisConfiguration extends AnalysisConfiguration {

	public IFDSAnalysisConfiguration(
			IConfigurationElement analysisConfigElement) {
		super(analysisConfigElement);
	}
	
	@SuppressWarnings("unchecked")
	public <P extends IFDSTabulationProblem<Unit, ?, SootMethod, InterproceduralCFG<Unit, SootMethod>>>
		IIFDSAnalysisPlugin<P> createIFDSAnalysisPlugin() {
		try {
			return (IIFDSAnalysisPlugin<P>) analysisConfigElement.createExecutableExtension("class");
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void runAnalysis(IAnalysisContext context) {
		//TODO avoid downcast by using generic types (if possible)
		IIFDSAnalysisContext ifdsContext = (IIFDSAnalysisContext) context;
		
		IFDSAnalysisConfiguration analysisConfiguration = (IFDSAnalysisConfiguration) context.getAnalysisConfiguration();
		IIFDSAnalysisPlugin<IFDSTabulationProblem<Unit, ?, SootMethod, InterproceduralCFG<Unit, SootMethod>>> ifdsAnalysisPlugin =
				analysisConfiguration.createIFDSAnalysisPlugin();
		IFDSTabulationProblem<Unit, ?, SootMethod, InterproceduralCFG<Unit, SootMethod>> ifdsProblem =
				ifdsAnalysisPlugin.createAnalysisProblem(ifdsContext);
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		IFDSSolver<Unit, ?, SootMethod, InterproceduralCFG<Unit, SootMethod>> solver = new JimpleIFDSSolver(ifdsProblem);
		solver.solve();
		
		ifdsAnalysisPlugin.afterAnalysis(ifdsProblem);
	}
}
