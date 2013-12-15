package de.fraunhofer.sit.codescan.typestate.analysis;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import soot.SootMethod;
import soot.Unit;
import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisManager;
import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;

public class TypeStateAnalysisPlugin implements IIFDSAnalysisPlugin {



	public IFDSTabulationProblem<Unit, ?, SootMethod, InterproceduralCFG<Unit, SootMethod>> createAnalysisProblem(IIFDSAnalysisManager manager) {
		return new TypestateAnalysisProblem(manager);
	}

	
}
