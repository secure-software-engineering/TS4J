package de.fraunhofer.sit.codescan.framework;

import org.eclipse.jdt.core.IMethod;

import soot.SootMethod;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import de.fraunhofer.sit.codescan.framework.internal.analysis.MustAlias;

public interface IAnalysisContext {
	
	JimpleBasedInterproceduralCFG getICFG();
	
	MustAlias getMustAliasManager();
	
	SootMethod getSootMethod();
	
	IMethod getMethod();
	
	AnalysisConfiguration getAnalysisConfiguration();
	
}
