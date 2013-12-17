package de.fraunhofer.sit.codescan.androidssl.analysis;

import soot.SootMethod;
import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.ErrorMarker;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class SSLAnalysisPlugin implements IIFDSAnalysisPlugin<SSLAnalysisProblem> {

	private static final String ERROR_MESSAGE = "Handler is always proceeding, which allows for man-in-the-middle attacks.";

	public SSLAnalysisProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		return new SSLAnalysisProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext context, SSLAnalysisProblem problem) {
		if(problem.isVulnerable()) {
			SootMethod m = context.getSootMethod();
			context.reportError(new ErrorMarker(
					ERROR_MESSAGE,
					m.getDeclaringClass().getName(),
					context.getSootMethod().getJavaSourceStartLineNumber()
					));
		}
	}

}
