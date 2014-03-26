package de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class HardCodedKeyAnalysisPlugin implements IIFDSAnalysisPlugin<HardCodedKeyAnalysisProblem> {

	public HardCodedKeyAnalysisProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		return new HardCodedKeyAnalysisProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext context, HardCodedKeyAnalysisProblem problem) {
		//do nothing
	}
	
}