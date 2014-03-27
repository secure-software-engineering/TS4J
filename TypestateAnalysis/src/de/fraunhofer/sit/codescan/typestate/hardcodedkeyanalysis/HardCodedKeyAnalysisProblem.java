package de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis;

import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.Var.KEYBYTES;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.Var.KEYSTRING;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.StatementId.KEYBYTES_CREATED;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.State.INIT;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.State.BYTESINVOKED;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;
import de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.State;
import de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.StatementId;
import de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.Var;

public class HardCodedKeyAnalysisProblem extends AbstractJimpleTypestateBackwardsAnalysisProblem<Var,State,StatementId> {
	
	private static final String MODEL_VALUE_CLASS_NAME = "example1.ModelValue";
	private static final String MODEL_VALUE_ADD_SIG = "<example1.ValueGroup: void add(example1.ModelValue)>";
	private static final String GET_BYTES = "<java.lang.String: byte[] getBytes()>";
	private static final String SECRET_KEY_CONSTRUCTOR = "<javax.crypto.spec.SecretKeySpec: void <init>(byte[],java.lang.String)>";

	enum Var { KEYBYTES, KEYSTRING };
	enum State { INIT, BYTESINVOKED };
	enum StatementId { KEYBYTES_CREATED, MODEL_VALUE_UPDATE };

	public HardCodedKeyAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
	}

	@Override
	protected Done<Var, State, StatementId> atCallToReturn(AtCallToReturn<Var, State, StatementId> d) {
		return d.atCallTo(SECRET_KEY_CONSTRUCTOR).always().trackParameter(0).as(KEYBYTES).toState(INIT)
				.orElse().atCallTo(GET_BYTES).always().trackThis().as(KEYSTRING).toState(BYTESINVOKED);
	}

	@Override
	protected Done<Var, State, StatementId> atReturn(AtReturn<Var, State, StatementId> d) {
		return d.atAnyReturn();
	}

	@Override
	protected Done<Var, State, StatementId> atNormalEdge(
			AtNormalEdge<Var, State, StatementId> d) {
		return d.atAssignTo(KEYSTRING).ifInState(BYTESINVOKED).and().ifValueBoundTo(KEYSTRING).equalsConstant().reportError("Should not use a constant as private Key").here();
	}


}
