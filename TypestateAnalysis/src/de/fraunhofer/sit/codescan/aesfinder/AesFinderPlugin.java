package de.fraunhofer.sit.codescan.aesfinder;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class AesFinderPlugin implements IIFDSAnalysisPlugin<AesFinderProblem> {

	public AesFinderProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		// TODO Auto-generated method stub
		return new AesFinderProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext, AesFinderProblem problem) {
		// TODO Auto-generated method stub
		
	}

}
