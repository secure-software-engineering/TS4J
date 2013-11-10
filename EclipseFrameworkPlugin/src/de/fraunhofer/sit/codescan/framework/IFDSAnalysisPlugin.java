package de.fraunhofer.sit.codescan.framework;

import heros.FlowFunctions;
import soot.Local;
import soot.SootMethod;
import soot.Unit;

/**
 * An analysis plugin creates a flow-function factory which is given access to an {@link IFDSAnalysisManager}.
 */
public interface IFDSAnalysisPlugin  {

	/**
	 * Creates a novel flow-function factory.
	 */
	public FlowFunctions<Unit, Local, SootMethod> createFlowFunctionsFactory(IFDSAnalysisManager manager);
	
}