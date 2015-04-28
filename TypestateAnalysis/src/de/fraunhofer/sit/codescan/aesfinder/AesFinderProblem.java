package de.fraunhofer.sit.codescan.aesfinder;


import de.fraunhofer.sit.codescan.aesfinder.AesFinderProblem.Var;
import de.fraunhofer.sit.codescan.aesfinder.AesFinderProblem.State;
import de.fraunhofer.sit.codescan.aesfinder.AesFinderProblem.StmtID;
import static de.fraunhofer.sit.codescan.aesfinder.AesFinderProblem.Var.CIPHER;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;

public class AesFinderProblem extends
		AbstractJimpleTypestateBackwardsAnalysisProblem<Var, State, StmtID> {
	private static final String CIPHER_CALL = "<javax.crypto.Cipher: javax.crypto.Cipher getInstance(java.lang.String)>";
	public AesFinderProblem(IIFDSAnalysisContext context) {
		super(context);
	}
	enum Var { CIPHER };
	enum State {};
	enum StmtID {}
	@Override
	protected Done<Var, State, StmtID> atCallToReturn(
			AtCallToReturn<Var, State, StmtID> d) {
		return d.atCallTo(CIPHER_CALL).always().trackParameter(0).as(CIPHER);
	}
	@Override
	protected Done<Var, State, StmtID> atReturn(
			AtReturn<Var, State, StmtID> atReturn) {
		return null;
	}
	@Override
	protected Done<Var, State, StmtID> atNormalEdge(
			AtNormalEdge<Var, State, StmtID> d) {
		// TODO Auto-generated method stub
		return d.atAssignTo(CIPHER).ifValueBoundTo(CIPHER).equalsString("AES").reportError("AES is used").here();
	}
}
