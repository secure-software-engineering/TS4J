package de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis;

import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.State.BYTESINVOKED;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.State.INIT;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.State.TO_STRING;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.State.SB_APPENDED;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.State.SB_CONSTRUCTED;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.State.FINALLY_APPENDED;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.StatementId.SECRET_KEY_INVOKED;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.Var.KEYBYTES;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.Var.KEYSTRING;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.Var.SB_APPENDSTRING_ARG1;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.Var.SB_APPENDSTRING_ARG2;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.Var.SB_APPENDSTRING_BASE;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.Var.TO_STRING_BASE;
import static de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.Var.SB_INIT_ARG;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;
import de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.State;
import de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.StatementId;
import de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisProblem.Var;

public class HardCodedKeyAnalysisProblem extends AbstractJimpleTypestateBackwardsAnalysisProblem<Var,State,StatementId> {
	
	private static final String GET_BYTES = "<java.lang.String: byte[] getBytes()>";
	private static final String SECRET_KEY_CONSTRUCTOR = "<javax.crypto.spec.SecretKeySpec: void <init>(byte[],java.lang.String)>";
	private static final String SB_TO_STRING_SIG = "<java.lang.StringBuilder: java.lang.String toString()>";
	private static final String SB_APPEND_SIG = "<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>";
	private static final String SB_CONSTRUCTOR = "<java.lang.StringBuilder: void <init>(java.lang.String)>";
	private static final String SB_VALUE_OF = "<java.lang.String: java.lang.String valueOf(java.lang.Object)>";

	enum Var { KEYBYTES, KEYSTRING, SB_APPENDSTRING_ARG2, SB_APPENDSTRING_ARG1, SB_APPENDSTRING_BASE, TO_STRING_BASE, SB_INIT_ARG };
	enum State { INIT, BYTESINVOKED, TO_STRING, SB_APPENDED, SB_CONSTRUCTED, FINALLY_APPENDED };
	enum StatementId { KEYBYTES_CREATED, SECRET_KEY_INVOKED  };

	public HardCodedKeyAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
	}

	@Override
	protected Done<Var, State, StatementId> atCallToReturn(AtCallToReturn<Var, State, StatementId> d) {
		return d.atCallTo(SECRET_KEY_CONSTRUCTOR).always().trackParameter(0).as(KEYBYTES).toState(INIT).storeStmtAs(SECRET_KEY_INVOKED).
				orElse().atCallTo(GET_BYTES).ifValueBoundTo(KEYBYTES).equalsReturnValue().trackThis().as(KEYSTRING).and().ifInState(INIT).toState(BYTESINVOKED).
				orElse().atCallTo(SB_TO_STRING_SIG).ifValueBoundTo(KEYSTRING).equalsReturnValue().trackThis().as(TO_STRING_BASE).and().ifInState(BYTESINVOKED).toState(TO_STRING).
				orElse().atCallTo(SB_APPEND_SIG).ifValueBoundTo(TO_STRING_BASE).equalsReturnValue().trackParameter(0).as(SB_APPENDSTRING_ARG2).
				and().always().trackThis().as(SB_APPENDSTRING_BASE).and().ifInState(TO_STRING).toState(SB_APPENDED).
				orElse().atCallTo(SB_CONSTRUCTOR).ifValueBoundTo(SB_APPENDSTRING_BASE).equalsThis().and().always().trackParameter(0).as(SB_INIT_ARG).and().ifInState(SB_APPENDED).toState(SB_CONSTRUCTED).
				orElse().atCallTo(SB_VALUE_OF).ifValueBoundTo(SB_INIT_ARG).equalsReturnValue().trackParameter(0).as(SB_APPENDSTRING_ARG1).and().ifInState(SB_CONSTRUCTED).toState(FINALLY_APPENDED);
	}

	@Override
	protected Done<Var, State, StatementId> atReturn(AtReturn<Var, State, StatementId> d) {
		return d.doNothing();
	}

	@Override
	protected Done<Var, State, StatementId> atNormalEdge(
			AtNormalEdge<Var, State, StatementId> d) {
		return d.atAssignTo(KEYSTRING).ifInState(BYTESINVOKED).and().ifValueBoundTo(KEYSTRING).equalsStringConstant().
				orElse().atAssignTo(SB_APPENDSTRING_ARG1).ifInState(FINALLY_APPENDED).and().ifValueBoundTo(SB_APPENDSTRING_ARG2).equalsStringConstant().
				and().ifValueBoundTo(SB_APPENDSTRING_ARG1).equalsStringConstant().
				orElse().atAssignTo(SB_APPENDSTRING_ARG2).ifInState(FINALLY_APPENDED).and().ifValueBoundTo(SB_APPENDSTRING_ARG2).equalsStringConstant().
				and().ifValueBoundTo(SB_APPENDSTRING_ARG1).equalsStringConstant().
				reportError("Should not use a constant as private Key").here();
	}
}
