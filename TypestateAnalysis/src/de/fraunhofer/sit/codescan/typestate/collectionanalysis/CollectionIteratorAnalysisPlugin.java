package de.fraunhofer.sit.codescan.typestate.collectionanalysis;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public class CollectionIteratorAnalysisPlugin implements IIFDSAnalysisPlugin<CollectionIteratorAnalysisProblem> {

	public CollectionIteratorAnalysisProblem createAnalysisProblem(IIFDSAnalysisContext context) {
		return new CollectionIteratorAnalysisProblem(context);
	}

	public void afterAnalysis(IIFDSAnalysisContext ifdsContext, CollectionIteratorAnalysisProblem problem) {
		//do nothing
	}
	
}