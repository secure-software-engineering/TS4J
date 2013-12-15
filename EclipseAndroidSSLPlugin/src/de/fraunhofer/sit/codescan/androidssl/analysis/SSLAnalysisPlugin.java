package de.fraunhofer.sit.codescan.androidssl.analysis;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import soot.SootMethod;
import soot.Unit;
import de.fraunhofer.sit.codescan.framework.IFDSAnalysisManager;
import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;

public class SSLAnalysisPlugin implements IIFDSAnalysisPlugin {

	public IFDSTabulationProblem<Unit, ?, SootMethod, InterproceduralCFG<Unit, SootMethod>> createAnalysisProblem(IFDSAnalysisManager manager) {
		return new SSLAnalysisProblem(manager);
	}

}
