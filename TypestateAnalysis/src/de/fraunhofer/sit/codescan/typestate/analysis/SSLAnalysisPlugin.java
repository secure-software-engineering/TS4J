package de.fraunhofer.sit.codescan.typestate.analysis;

import heros.FlowFunctions;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import de.fraunhofer.sit.codescan.framework.IFDSAnalysisManager;
import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;

public class SSLAnalysisPlugin implements IIFDSAnalysisPlugin {

	public FlowFunctions<Unit, Local, SootMethod> createFlowFunctionsFactory(
			IFDSAnalysisManager manager) {
		return null;
	}

	
}
