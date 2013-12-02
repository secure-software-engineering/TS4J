package de.fraunhofer.sit.codescan.framework;

import heros.InterproceduralCFG;
import heros.solver.IFDSSolver;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
import de.fraunhofer.sit.codescan.framework.internal.analysis.IFDSAdapter;

public class IFDSAnalysisConfiguration extends AnalysisConfiguration{

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
		IFDSAdapter ifdsProblem = new IFDSAdapter(context);
		IFDSSolver<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> solver =
				new JimpleIFDSSolver<Local, InterproceduralCFG<Unit,SootMethod>>(ifdsProblem);
		solver.solve();
		
		if(ifdsProblem.isMethodVulnerable()) {
			markMethodAsVulnerable(context);
		}		
	}

}
