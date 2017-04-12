package de.fraunhofer.sit.codescan.typestate.enabledciphersuiteanalysis;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class EnabledCipherSuiteAnalysisPlugin implements IIFDSAnalysisPlugin<EnabledCipherSuiteAnalysisProblem>{

	public EnabledCipherSuiteAnalysisProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		// TODO Auto-generated method stub
		return new EnabledCipherSuiteAnalysisProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext, EnabledCipherSuiteAnalysisProblem problem) {
		// TODO Auto-generated method stub
		
	}
}
