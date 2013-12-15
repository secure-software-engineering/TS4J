package de.fraunhofer.sit.codescan.sootbridge;

import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

public interface IIFDSAnalysisContext extends IAnalysisContext {
	
	JimpleBasedInterproceduralCFG getICFG();

}
