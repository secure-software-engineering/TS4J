package de.fraunhofer.sit.codescan.typestate.enabledprotocolanalysis;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class EnabledProtocolAnalysisPlugin implements IIFDSAnalysisPlugin<EnabledProtocolAnalysisProblem>{

	public EnabledProtocolAnalysisProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		// TODO Auto-generated method stub
		return new EnabledProtocolAnalysisProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext, EnabledProtocolAnalysisProblem problem) {
		// TODO Auto-generated method stub
		
	}
}
