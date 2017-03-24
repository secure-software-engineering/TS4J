package de.fraunhofer.sit.codescan.aesiifinder;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class ECBFinderPlugin implements IIFDSAnalysisPlugin<ECBFinderProblem> {

	public ECBFinderProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		// TODO Auto-generated method stub
		return new ECBFinderProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext, ECBFinderProblem problem) {
		// TODO Auto-generated method stub
		
	}

}
