package de.fraunhofer.sit.codescan.framework.internal.analysis;

import soot.Local;
import soot.SootMethod;
import soot.jimple.Stmt;
import de.fraunhofer.sit.codescan.framework.IAnalysisContext;
import de.fraunhofer.sit.codescan.framework.IFDSAnalysisManager;

/**
 * Generic super class for implementing IFDS-based code analyses. Operates on {@link Local}s.
 * The analysis focuses on a given {@link SootMethod}. The method is assumed to be vulnerable unless proven otherwise.
 * If a method is found to be not vulnerable, the analysis should call {@link #markMethodAsBenign()}.
 */
public class IFDSAdapter implements IFDSAnalysisManager {

	protected boolean methodNotVulnerable = false;
	private final IAnalysisContext context;

	public IFDSAdapter(IAnalysisContext context) {
		this.context = context;
	}

	/**
	 * Clients can call this method after analysis to determine whether the respective method is vulnerable.
	 * Usually this method is only used by the framework.  
	 */
	public boolean isMethodVulnerable() {
		return !methodNotVulnerable;
	}
	
	/**
	 * Subclasses should call this method when the analysis found the respective method under analysis not to be vulnerable.
	 */
	public void markMethodAsBenign() {
		methodNotVulnerable = true;
	}

	/**
	 * Checks whether l1 will at stmt definitely point to the same value as l2 at stmt2.
	 */
	public boolean mustAlias(Stmt stmt, Local l1, Stmt stmt2, Local l2) {
		return context.getMustAliasManager().mustAlias(stmt, l1, stmt2, l2);
	}
	
	public SootMethod getMethodToFocusOn() {
		return context.getSootMethod();
	}
	
	public IAnalysisContext getContext() {
		return context;
	}
}