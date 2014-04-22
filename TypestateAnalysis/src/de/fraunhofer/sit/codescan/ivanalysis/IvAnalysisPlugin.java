package de.fraunhofer.sit.codescan.ivanalysis;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class IvAnalysisPlugin implements IIFDSAnalysisPlugin<IvAnalysisProblem> {

	public IvAnalysisProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		// TODO Auto-generated method stub
		return new IvAnalysisProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext, IvAnalysisProblem problem) {
		// TODO Auto-generated method stub
		
	}

}
