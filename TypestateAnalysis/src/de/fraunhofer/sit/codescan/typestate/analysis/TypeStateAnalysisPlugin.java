package de.fraunhofer.sit.codescan.typestate.analysis;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class TypeStateAnalysisPlugin implements IIFDSAnalysisPlugin<TypestateAnalysisProblem> {

	public TypestateAnalysisProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		return new TypestateAnalysisProblem(context);
	}

	public void afterAnalysis(TypestateAnalysisProblem problem) {
		//do nothing
	}
	
}