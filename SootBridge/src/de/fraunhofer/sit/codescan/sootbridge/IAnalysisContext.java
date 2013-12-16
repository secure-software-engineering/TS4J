package de.fraunhofer.sit.codescan.sootbridge;

import soot.Local;
import soot.SootMethod;
import soot.jimple.Stmt;

public interface IAnalysisContext {

	public SootMethod getSootMethod();
	
	public boolean mustAlias(Stmt stmt, Local l, Stmt stmt2, Local l2);
	
	public IAnalysisConfiguration getAnalysisConfiguration();
	
	public void reportError(ErrorMarker... result);


}