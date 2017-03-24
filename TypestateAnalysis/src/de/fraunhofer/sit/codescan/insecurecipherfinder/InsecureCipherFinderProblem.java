package de.fraunhofer.sit.codescan.insecurecipherfinder;

import static de.fraunhofer.sit.codescan.insecurecipherfinder.InsecureCipherFinderProblem.StmtID.GET_INSTANCE;
import static de.fraunhofer.sit.codescan.insecurecipherfinder.InsecureCipherFinderProblem.Var.CIPHER;

import de.fraunhofer.sit.codescan.insecurecipherfinder.InsecureCipherFinderProblem.State;
import de.fraunhofer.sit.codescan.insecurecipherfinder.InsecureCipherFinderProblem.StmtID;
import de.fraunhofer.sit.codescan.insecurecipherfinder.InsecureCipherFinderProblem.Var;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;


public class InsecureCipherFinderProblem extends AbstractJimpleTypestateBackwardsAnalysisProblem<Var, State, StmtID>
{

	public InsecureCipherFinderProblem(IIFDSAnalysisContext context) {
		super(context);
	}
	
	private static final String CIPHER_CALL = "<javax.crypto.Cipher: javax.crypto.Cipher getInstance(java.lang.String)>";
	private static final String ERROR_MESSAGE_1 = "Insecure cipher is used";

	enum Var {CIPHER};
	enum State {};
	enum StmtID {GET_INSTANCE}
	
	
	@Override
	protected Done<Var, State, StmtID> atCallToReturn(
			AtCallToReturn<Var, State, StmtID> d) {
		return d.atCallTo(CIPHER_CALL).always().trackParameter(0).as(CIPHER).and().storeStmtAs(GET_INSTANCE);
	}

	@Override
	protected Done<Var, State, StmtID> atReturn(AtReturn<Var, State, StmtID> atReturn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Done<Var, State, StmtID> atNormalEdge(AtNormalEdge<Var, State, StmtID> d) {
		
		return d.atAssignTo(CIPHER).ifValueBoundTo(CIPHER).contains("RC2").reportError(ERROR_MESSAGE_1).atStmt(GET_INSTANCE)
				.orElse().atAssignTo(CIPHER).ifValueBoundTo(CIPHER).contains("RC4").reportError(ERROR_MESSAGE_1).atStmt(GET_INSTANCE);
	}

}
