package de.fraunhofer.sit.codescan.sootbridge;

import java.util.Set;

import soot.SootMethod;

public interface IAnalysisContext {

	public SootMethod getSootMethod();
	
	public MustAlias getMustAliasManager();
	
	public IAnalysisConfiguration getAnalysisConfiguration();
	
	public void setResult(Set<ErrorMarker> result);


}