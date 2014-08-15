package de.fraunhofer.sit.codescan.seededrandom;

import heros.solver.IFDSSolver;
import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class SeededRandomPlugin implements
		IIFDSAnalysisPlugin<SeededRandomProblem> {

	public SeededRandomProblem createAnalysisProblem(
			IIFDSAnalysisContext context) {
		return new SeededRandomProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext,
			SeededRandomProblem problem) {
	}

}
