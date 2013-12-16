package de.fraunhofer.sit.codescan.androidssl.analysis;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class SSLAnalysisPlugin implements IIFDSAnalysisPlugin<SSLAnalysisProblem> {

	public SSLAnalysisProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		return new SSLAnalysisProblem(context);
	}

	public void afterAnalysis(SSLAnalysisProblem problem) {
		if(problem.isVulnerable()) {
			//TODO create error marker
		}
	}

}
