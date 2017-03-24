package de.fraunhofer.sit.codescan.insecuremessagedigestfinder;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class InsecureMessageDigestFinderPlugin implements IIFDSAnalysisPlugin<InsecureMessageDigestFinderProblem>{

	public InsecureMessageDigestFinderProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		// TODO Auto-generated method stub
		return new InsecureMessageDigestFinderProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext, InsecureMessageDigestFinderProblem problem) {
		// TODO Auto-generated method stub
		
	}
}
