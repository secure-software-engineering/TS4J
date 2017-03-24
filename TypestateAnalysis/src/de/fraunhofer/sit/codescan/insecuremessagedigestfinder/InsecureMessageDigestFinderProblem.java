package de.fraunhofer.sit.codescan.insecuremessagedigestfinder;

import static de.fraunhofer.sit.codescan.insecuremessagedigestfinder.InsecureMessageDigestFinderProblem.StmtID.GET_INSTANCE;
import static de.fraunhofer.sit.codescan.insecuremessagedigestfinder.InsecureMessageDigestFinderProblem.Var.VALUE;

import de.fraunhofer.sit.codescan.insecuremessagedigestfinder.InsecureMessageDigestFinderProblem.State;
import de.fraunhofer.sit.codescan.insecuremessagedigestfinder.InsecureMessageDigestFinderProblem.StmtID;
import de.fraunhofer.sit.codescan.insecuremessagedigestfinder.InsecureMessageDigestFinderProblem.Var;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;


public class InsecureMessageDigestFinderProblem extends AbstractJimpleTypestateBackwardsAnalysisProblem<Var, State, StmtID>
{

	public InsecureMessageDigestFinderProblem(IIFDSAnalysisContext context) {
		super(context);
	}
	
	private static final String MD_CALL = "<java.security.MessageDigest: java.security.MessageDigest getInstance(java.lang.String)>";
	private static final String ERROR_MESSAGE_1 = "You are using an insecure message digest algorithm.";

	enum Var {VALUE};
	enum State {};
	enum StmtID {GET_INSTANCE}
	
	
	@Override
	protected Done<Var, State, StmtID> atCallToReturn(
			AtCallToReturn<Var, State, StmtID> d) {
		return d.atCallTo(MD_CALL).always().trackParameter(0).as(VALUE).and().storeStmtAs(GET_INSTANCE);
	}

	@Override 
	protected Done<Var, State, StmtID> atReturn(AtReturn<Var, State, StmtID> atReturn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Done<Var, State, StmtID> atNormalEdge(AtNormalEdge<Var, State, StmtID> d) {
		
		return d.atAssignTo(VALUE).ifValueBoundTo(VALUE).equalsString("MD2").reportError(ERROR_MESSAGE_1).atStmt(GET_INSTANCE)
				.orElse().atAssignTo(VALUE).ifValueBoundTo(VALUE).equalsString("MD4").reportError(ERROR_MESSAGE_1).atStmt(GET_INSTANCE)
				.orElse().atAssignTo(VALUE).ifValueBoundTo(VALUE).equalsString("MD5").reportError(ERROR_MESSAGE_1).atStmt(GET_INSTANCE)
				.orElse().atAssignTo(VALUE).ifValueBoundTo(VALUE).equalsString("SHA-1").reportError(ERROR_MESSAGE_1).atStmt(GET_INSTANCE);
	}

}
