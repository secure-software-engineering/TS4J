package de.fraunhofer.sit.codescan.framework;

import de.fraunhofer.sit.codescan.sootbridge.IAnalysisContext;
import soot.SootMethod;

public interface IMethodBasedAnalysisPlugin {
	
	/**
	 * Analyzes the given method in isolation. The method is assumed to be vulnerable by default.
	 * Implementors should call {@link IAnalysisContext#markMethodAsBenign()} on the manager
	 * to mark a method as benign when it was found to be benign.
	 */
	public void analyzeMethod(SootMethod m, IAnalysisContext manager);

}
