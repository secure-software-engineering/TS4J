package de.fraunhofer.sit.codescan.framework;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

/**
 * This interface is used to provide context to client analyses.
 */
public interface AnalysisManager {

	/**
	 * The client analyses should call this method if it was discovered
	 * that the analyzed method is not vulnerable, i.e., is benign.
	 * By default, the method is assumed to be vulnerable. 
	 */
	public void markMethodAsBenign();
	
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
	
	/**
	 * Clients can call this method to determine if l1 at stmt and l2 at stmt2
	 * must alias. A local must-alias analysis is created on the fly and is then
	 * cached. Will return <code>false</code> if both statements
	 * reside in different methods.
	 */
	public boolean mustAlias(Stmt stmt, Local l1, Stmt stmt2, Local l2);
}
