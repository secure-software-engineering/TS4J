package de.fraunhofer.sit.codescan.framework;

import soot.SootMethod;

/**
 * This interface is used to provide context to client analyses.
 */
public interface IFDSAnalysisManager extends MethodBasedAnalysisManager {

	/**
	 * Returns <code>true</code> if {@link #markMethodAsBenign()} was called
	 * and <code>false</code> otherwise.
	 */
	public boolean isMethodVulnerable();
	
	/**
	 * Returns the method to analyze against. Clients will typically use this handle
	 * to gzenerate flows or for reporting.
	 */
	public SootMethod getMethodToFocusOn();
		
	
	public IAnalysisContext getContext();
}
