package de.fraunhofer.sit.codescan.framework;

import heros.InterproceduralCFG;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

public interface AnalysisManager {

	public void markMethodAsBenign();
	
	public boolean isMethodVulnerable();
	public boolean mustAlias(Stmt stmt, Local l1, Stmt stmt2, Local l2);
	public InterproceduralCFG<Unit, SootMethod> interproceduralCFG();
	public SootMethod getMethodToFocusOn();
	public Local zeroValue();
	
}
