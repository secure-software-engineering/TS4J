package de.fraunhofer.sit.codescan.typestate.analysis;

import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.State.FLUSHED;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.State.TAINTED;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.StatementId.MODEL_VALUE_UPDATE;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.Var.MODEL_VALUE;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.Var.VALUE_GROUP;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.State;
import de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.StatementId;
import de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.Var;

public class TypestateAnalysisProblem extends AbstractTypestateAnalysisProblem<Var,State,StatementId> {
	
	private static final String MODEL_VALUE_CLASS_NAME = "example1.ModelValue";
	private static final String MODEL_VALUE_ADD_SIG = "<example1.ValueGroup: void add(example1.ModelValue)>";
	private static final String VALUE_GROUP_FLUSH_SIG = "<example1.ValueGroup: void flush()>";
	private static final String VALUE_GROUP_CONSTRUCTOR_SIG = "<example1.ValueGroup: void <init>()>";

	enum Var { VALUE_GROUP, MODEL_VALUE };
	enum State { FLUSHED, TAINTED };
	enum StatementId { MODEL_VALUE_UPDATE };

	public TypestateAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
	}

	@Override
	protected Done<Var, State, StatementId> atCallToReturn(AtCallToReturn<Var, State, StatementId> d) {
		return d.atCallTo(VALUE_GROUP_CONSTRUCTOR_SIG).always().trackThis().as(VALUE_GROUP).toState(FLUSHED).orElse().
			     atCallTo(MODEL_VALUE_ADD_SIG).ifValueBoundTo(VALUE_GROUP).equalsThis().trackParameter(0).as(MODEL_VALUE).orElse().
			     atCallTo(VALUE_GROUP_FLUSH_SIG).ifValueBoundTo(VALUE_GROUP).equalsThis().toState(FLUSHED).orElse().
			     atAnyCallToClass(MODEL_VALUE_CLASS_NAME).ifValueBoundTo(MODEL_VALUE).equalsThis().toState(TAINTED).storeStmtAs(MODEL_VALUE_UPDATE);
	}

	@Override
	protected Done<Var, State, StatementId> atReturn(AtReturn<Var, State, StatementId> d) {
		return d.atAnyReturn().ifInState(TAINTED).reportError("ERROR!").atStmt(MODEL_VALUE_UPDATE);
	}

}
