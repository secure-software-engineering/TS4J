package de.fraunhofer.sit.codescan.sourcesinkanalysis;


import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateForwardAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;
import de.fraunhofer.sit.codescan.sourcesinkanalysis.SourceToSinkAnalysisProblem.State;
import de.fraunhofer.sit.codescan.sourcesinkanalysis.SourceToSinkAnalysisProblem.StatementId;
import static de.fraunhofer.sit.codescan.sourcesinkanalysis.SourceToSinkAnalysisProblem.State.*;
import de.fraunhofer.sit.codescan.sourcesinkanalysis.SourceToSinkAnalysisProblem.Var;
import static de.fraunhofer.sit.codescan.sourcesinkanalysis.SourceToSinkAnalysisProblem.Var.*;

public class SourceToSinkAnalysisProblem extends AbstractJimpleTypestateBackwardsAnalysisProblem<Var,State,StatementId> {
	
	private static final String SANITIZER_ANNOTATION = "Lservletexample/Sanitizer;";
	private static final String SOURCE_ANNOTATION = "Lservletexample/Source;";
	private static final String SINK_ANNOTATION = "Lservletexample/Sink;";

	enum Var { INPUT_SINK};
	enum State { FOUND_SINK, SANITIZED };
	enum StatementId { };

	public SourceToSinkAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
	}

	@Override
	protected Done<Var, State, StatementId> atCallToReturn(
			AtCallToReturn<Var, State, StatementId> d) {
		return null;
	}

	@Override
	protected Done<Var, State, StatementId> atReturn(
			AtReturn<Var, State, StatementId> d) {
		return	d.atReturnFromMethodWithAnnotation(SINK_ANNOTATION).always().trackParameter(1).as(INPUT_SINK).toState(FOUND_SINK)
				.orElse().atReturnFromMethodWithAnnotation(SOURCE_ANNOTATION).ifValueBoundTo(INPUT_SINK).equalsReturnValue().and().ifInState(FOUND_SINK).reportError("NOTYETSANT").here()
				.orElse().atReturnFromMethodWithAnnotation(SANITIZER_ANNOTATION).ifValueBoundTo(INPUT_SINK).equalsReturnValue().toState(SANITIZED);
	}

	@Override
	protected Done<Var, State, StatementId> atNormalEdge(
			AtNormalEdge<Var, State, StatementId> d) {
		return null;
	}


}
