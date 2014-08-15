package de.fraunhofer.sit.codescan.seededrandom;

import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;
import static de.fraunhofer.sit.codescan.seededrandom.SeededRandomProblem.Var;
import static de.fraunhofer.sit.codescan.seededrandom.SeededRandomProblem.State;
import static de.fraunhofer.sit.codescan.seededrandom.SeededRandomProblem.StmtID;
import static de.fraunhofer.sit.codescan.seededrandom.SeededRandomProblem.Var.SEED_LONG;
import static de.fraunhofer.sit.codescan.seededrandom.SeededRandomProblem.Var.SEED_ARRAY;
import static de.fraunhofer.sit.codescan.seededrandom.SeededRandomProblem.State.SEEDED_LONG;
import static de.fraunhofer.sit.codescan.seededrandom.SeededRandomProblem.State.SEEDED_BYTE;

public class SeededRandomProblem extends
		AbstractJimpleTypestateBackwardsAnalysisProblem<Var, State, StmtID> {

	private static final String SET_SEED_LONG = "<java.security.SecureRandom: void setSeed(long)>";
	private static final String SET_SEED_BYTE = "<java.security.SecureRandom: void setSeed(byte[])>";
	private static final String CURRENT_MILLISECS = "<java.lang.System: long currentTimeMillis()>";
	
	enum Var { SEED_LONG, SEED_ARRAY };
	enum State { SEEDED_LONG, SEEDED_BYTE};
	enum StmtID {}
	public SeededRandomProblem(IIFDSAnalysisContext context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Done<Var, State, StmtID> atCallToReturn(
			AtCallToReturn<Var, State, StmtID> d) {
		// TODO Auto-generated method stub
		return d.atCallTo(SET_SEED_LONG).always().trackParameter(0).as(SEED_LONG).toState(SEEDED_LONG).orElse().atCallTo(SET_SEED_BYTE).always().trackParameter(0).asArray(SEED_ARRAY).toState(SEEDED_BYTE).
				orElse().atCallTo(CURRENT_MILLISECS).ifInState(SEEDED_LONG).and().ifValueBoundTo(SEED_LONG).equalsReturnValue().reportError("The SecureRandom should not be seeded with the current time").here();
	}

	@Override
	protected Done<Var, State, StmtID> atReturn(
			AtReturn<Var, State, StmtID> d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Done<Var, State, StmtID> atNormalEdge(
			AtNormalEdge<Var, State, StmtID> d) {
		// TODO Auto-generated method stub
		return d.atAssignTo(SEED_ARRAY).ifInState(SEEDED_BYTE).and().ifValueBoundTo(SEED_ARRAY).eachEqualsInstance(soot.jimple.IntConstant.class).reportError("The SecureRandom should not be seeded with a constant value").here();
	}

}
