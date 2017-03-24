package de.fraunhofer.sit.codescan.aesfinder;


import static de.fraunhofer.sit.codescan.aesfinder.AesFinderProblem.StmtID.GET_INSTANCE;
import static de.fraunhofer.sit.codescan.aesfinder.AesFinderProblem.Var.CIPHER;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.sit.codescan.aesfinder.AesFinderProblem.State;
import de.fraunhofer.sit.codescan.aesfinder.AesFinderProblem.StmtID;
import de.fraunhofer.sit.codescan.aesfinder.AesFinderProblem.Var;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;

public class AesFinderProblem extends
		AbstractJimpleTypestateBackwardsAnalysisProblem<Var, State, StmtID> {
	private static final String CIPHER_CALL = "<javax.crypto.Cipher: javax.crypto.Cipher getInstance(java.lang.String)>";
	
	enum Var { CIPHER };
	enum State {};
	enum StmtID {GET_INSTANCE}
	
	public AesFinderProblem(IIFDSAnalysisContext context) {
		super(context);
	}
	
	@Override
	protected Done<Var, State, StmtID> atCallToReturn(
			AtCallToReturn<Var, State, StmtID> d) {
		return d.atCallTo(CIPHER_CALL).always().
			trackParameter(0).as(CIPHER).
			and().storeStmtAs(GET_INSTANCE).orElse().atAssignTo(CIPHER).ifValueBoundTo(CIPHER).startsWith("DES").reportError("DES is used").atStmt(GET_INSTANCE);
	}
	
	@Override
	protected Done<Var, State, StmtID> atReturn(
			AtReturn<Var, State, StmtID> atReturn) {
		return null;
	}
	
	@Override
	protected Done<Var, State, StmtID> atNormalEdge(
			AtNormalEdge<Var, State, StmtID> d) {
		List<String> starters = new ArrayList<String>();
		starters.add("AES");
		starters.add("DES");
		return d.atAssignTo(CIPHER).ifValueBoundTo(CIPHER).doesNotContain("/").
			and().ifValueBoundTo(CIPHER).startsWiths(starters).reportError("ECB Mode is used").atStmt(GET_INSTANCE);
	}
}
