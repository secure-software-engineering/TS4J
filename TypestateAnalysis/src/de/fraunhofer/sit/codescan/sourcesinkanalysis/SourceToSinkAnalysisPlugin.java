package de.fraunhofer.sit.codescan.sourcesinkanalysis;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class SourceToSinkAnalysisPlugin implements IIFDSAnalysisPlugin<SourceToSinkAnalysisProblem> {

	public SourceToSinkAnalysisProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		return new SourceToSinkAnalysisProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext, SourceToSinkAnalysisProblem problem) {
		//do nothing
	}
	
}