package de.fraunhofer.sit.codescan.framework;

import soot.SootMethod;

public interface MethodBasedAnalysisPlugin {
	
	/**
	 * Analyzes the given method in isolation. The method is assumed to be vulnerable by default.
	 * Implementors should call {@link MethodBasedAnalysisManager#markMethodAsBenign()} on the manager
	 * to mark a method as benign when it was found to be benign.
	 */
	public void analyzeMethod(SootMethod m, MethodBasedAnalysisManager manager);

}
