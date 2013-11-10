package de.fraunhofer.sit.codescan.framework;

import soot.Local;
import soot.jimple.Stmt;

public interface MethodBasedAnalysisManager {

	/**
	 * The client analyses should call this method if it was discovered
	 * that the analyzed method is not vulnerable, i.e., is benign.
	 * By default, the method is assumed to be vulnerable. 
	 */
	public void markMethodAsBenign();

	/**
	 * Clients can call this method to determine if l1 at stmt and l2 at stmt2
	 * must alias. A local must-alias analysis is created on the fly and is then
	 * cached. Will return <code>false</code> if both statements
	 * reside in different methods.
	 */
	public boolean mustAlias(Stmt stmt, Local l1, Stmt stmt2, Local l2);

}