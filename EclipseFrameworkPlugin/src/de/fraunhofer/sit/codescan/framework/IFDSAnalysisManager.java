package de.fraunhofer.sit.codescan.framework;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import soot.Local;
import soot.SootMethod;
import soot.Unit;

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
	 * Gives access to the ICFG.
	 */
	public InterproceduralCFG<Unit, SootMethod> interproceduralCFG();
		
	/**
	 * Returns the method to analyze against. Clients will typically use this handle
	 * to gzenerate flows or for reporting.
	 */
	public SootMethod getMethodToFocusOn();
		
	/**
	 * The ZERO flow fact as defined in the IFDS framework.
	 * @see IFDSTabulationProblem
	 */
	public Local zeroValue();
}
