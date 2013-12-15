package de.fraunhofer.sit.codescan.framework;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import soot.SootMethod;
import soot.Unit;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

/**
 * An analysis plugin creates a flow-function factory which is given access to an {@link IIFDSAnalysisManager}.
 */
public interface IIFDSAnalysisPlugin {

	/**
	 * Creates a novel flow-function factory.
	 */
	public IFDSTabulationProblem<Unit, ?, SootMethod, InterproceduralCFG<Unit, SootMethod>> createAnalysisProblem(IIFDSAnalysisContext context);
	
}