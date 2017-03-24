package de.fraunhofer.sit.codescan.insecuremacfinder;

import static de.fraunhofer.sit.codescan.insecuremacfinder.InsecureMacFinderProblem.StmtID.GET_INSTANCE;
import static de.fraunhofer.sit.codescan.insecuremacfinder.InsecureMacFinderProblem.Var.VALUE;

import de.fraunhofer.sit.codescan.insecuremacfinder.InsecureMacFinderProblem.State;
import de.fraunhofer.sit.codescan.insecuremacfinder.InsecureMacFinderProblem.StmtID;
import de.fraunhofer.sit.codescan.insecuremacfinder.InsecureMacFinderProblem.Var;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;


public class InsecureMacFinderProblem extends AbstractJimpleTypestateBackwardsAnalysisProblem<Var, State, StmtID>
{

	public InsecureMacFinderProblem(IIFDSAnalysisContext context) {
		super(context);
	}
	
	private static final String MAC_CALL = "<javax.crypto.Mac: javax.crypto.Mac getInstance(java.lang.String)>";
	private static final String ERROR_MESSAGE_1 = "You are using an insecure MAC algorithm.";

	enum Var {VALUE};
	enum State {};
	enum StmtID {GET_INSTANCE}
	
	
	@Override
	protected Done<Var, State, StmtID> atCallToReturn(
			AtCallToReturn<Var, State, StmtID> d) {
		return d.atCallTo(MAC_CALL).always().trackParameter(0).as(VALUE).and().storeStmtAs(GET_INSTANCE);
	}

	@Override 
	protected Done<Var, State, StmtID> atReturn(AtReturn<Var, State, StmtID> atReturn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Done<Var, State, StmtID> atNormalEdge(AtNormalEdge<Var, State, StmtID> d) {
		
		return d.atAssignTo(VALUE).ifValueBoundTo(VALUE).equalsString("HmacMD5").reportError(ERROR_MESSAGE_1).atStmt(GET_INSTANCE)
				.orElse().atAssignTo(VALUE).ifValueBoundTo(VALUE).equalsString("HmacSHA1").reportError(ERROR_MESSAGE_1).atStmt(GET_INSTANCE);
	}

}
