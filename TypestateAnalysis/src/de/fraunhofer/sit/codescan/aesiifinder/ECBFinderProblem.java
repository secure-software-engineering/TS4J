package de.fraunhofer.sit.codescan.aesiifinder;

import de.fraunhofer.sit.codescan.aesiifinder.ECBFinderProblem.Var;
import de.fraunhofer.sit.codescan.aesiifinder.ECBFinderProblem.State;
import de.fraunhofer.sit.codescan.aesiifinder.ECBFinderProblem.StmtID;
import static de.fraunhofer.sit.codescan.aesiifinder.ECBFinderProblem.Var.CIPHER;
import static de.fraunhofer.sit.codescan.aesiifinder.ECBFinderProblem.StmtID.GET_INSTANCE;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;

public class ECBFinderProblem extends AbstractJimpleTypestateBackwardsAnalysisProblem<Var, State, StmtID> {

	private static final String CIPHER_CALL = "<javax.crypto.Cipher: javax.crypto.Cipher getInstance(java.lang.String)>";

	enum Var {
		CIPHER
	};

	enum State {};

	enum StmtID {
		GET_INSTANCE
	}

	public ECBFinderProblem(IIFDSAnalysisContext context) {
		super(context);
	}

	@Override
	protected Done<Var, State, StmtID> atCallToReturn(AtCallToReturn<Var, State, StmtID> d) {
		return d.atCallTo(CIPHER_CALL).always().trackParameter(0).as(CIPHER).and().storeStmtAs(GET_INSTANCE);
	}

	@Override
	protected Done<Var, State, StmtID> atReturn(AtReturn<Var, State, StmtID> atReturn) {
		return null;
	}

	@Override
	protected Done<Var, State, StmtID> atNormalEdge(AtNormalEdge<Var, State, StmtID> d) {
		return d.atAssignTo(CIPHER).ifValueBoundTo(CIPHER).contains("ECB").reportError("ECB Mode is used").atStmt(GET_INSTANCE);
	}
}
