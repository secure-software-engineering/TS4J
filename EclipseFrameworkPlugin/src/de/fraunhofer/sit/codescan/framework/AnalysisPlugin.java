package de.fraunhofer.sit.codescan.framework;

import heros.FlowFunctions;
import soot.Local;
import soot.SootMethod;
import soot.Unit;


public interface AnalysisPlugin  {

	public FlowFunctions<Unit, Local, SootMethod> createFlowFunctionsFactory(AnalysisManager manager);
	
}