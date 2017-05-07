package de.fraunhofer.sit.codescan.typestate.tlschannelcloseanalysis;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class TLSChannelCloseAnalysisPlugin implements IIFDSAnalysisPlugin<TLSChannelCloseAnalysisProblem>{

	public TLSChannelCloseAnalysisProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		// TODO Auto-generated method stub
		return new TLSChannelCloseAnalysisProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext, TLSChannelCloseAnalysisProblem problem) {
		// TODO Auto-generated method stub
		
	}
}
