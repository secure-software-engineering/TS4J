package de.fraunhofer.sit.codescan.insecurecipherfinder;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class InsecureCipherFinderPlugin implements IIFDSAnalysisPlugin<InsecureCipherFinderProblem>{

	public InsecureCipherFinderProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		// TODO Auto-generated method stub
		return new InsecureCipherFinderProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext, InsecureCipherFinderProblem problem) {
		// TODO Auto-generated method stub
		
	}
}
