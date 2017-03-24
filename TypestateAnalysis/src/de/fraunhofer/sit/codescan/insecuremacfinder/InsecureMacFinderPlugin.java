package de.fraunhofer.sit.codescan.insecuremacfinder;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class InsecureMacFinderPlugin implements IIFDSAnalysisPlugin<InsecureMacFinderProblem>{

	public InsecureMacFinderProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		// TODO Auto-generated method stub
		return new InsecureMacFinderProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext, InsecureMacFinderProblem problem) {
		// TODO Auto-generated method stub
		
	}
}
