package de.fraunhofer.sit.codescan.typestate.valuegroupanalysis;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class ValueGroupAnalysisPlugin implements IIFDSAnalysisPlugin<ValueGroupAnalysisProblem> {

	public ValueGroupAnalysisProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		return new ValueGroupAnalysisProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext context, ValueGroupAnalysisProblem problem) {
		//do nothing
	}
	
}