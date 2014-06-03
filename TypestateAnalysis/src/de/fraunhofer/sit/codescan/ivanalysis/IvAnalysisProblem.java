package de.fraunhofer.sit.codescan.ivanalysis;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;
import de.fraunhofer.sit.codescan.ivanalysis.IvAnalysisProblem.Var;
import de.fraunhofer.sit.codescan.ivanalysis.IvAnalysisProblem.State;
import de.fraunhofer.sit.codescan.ivanalysis.IvAnalysisProblem.StmtID;
import static de.fraunhofer.sit.codescan.ivanalysis.IvAnalysisProblem.Var.IV_VALUE;
import static de.fraunhofer.sit.codescan.ivanalysis.IvAnalysisProblem.State.NOT_STATIC;
import static de.fraunhofer.sit.codescan.ivanalysis.IvAnalysisProblem.State.INIT;

public class IvAnalysisProblem extends AbstractJimpleTypestateBackwardsAnalysisProblem<Var, State, StmtID> {

	public IvAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
	}
	enum Var { IV_VALUE };
	enum State { NOT_STATIC, INIT};
	enum StmtID {}
	
	@Override
	protected Done<Var, State, StmtID> atCallToReturn(
			AtCallToReturn<Var, State, StmtID> d) {

		// TODO Auto-generated method stub
		return d.atCallTo("<javax.crypto.spec.IvParameterSpec: void <init>(byte[])>").always().trackParameter(0).asArray(IV_VALUE).and().toState(INIT).orElse()
				.atCallTo("<java.security.SecureRandom: void nextBytes(byte[])>").ifValueBoundTo(IV_VALUE).equalsParameter(0).toState(NOT_STATIC);
	}
	@Override
	protected Done<Var, State, StmtID> atReturn(
			AtReturn<Var, State, StmtID> d) {
		return null;
	}
	@Override
	protected Done<Var, State, StmtID> atNormalEdge(
			AtNormalEdge<Var, State, StmtID> d) {
		// TODO Auto-generated method stub
		return d.atAssignTo(IV_VALUE).ifInState(INIT).and().ifValueBoundTo(IV_VALUE).eachEqualsInstance(soot.jimple.IntConstant.class).reportError("The Initialization Vector is choosen constant!").here();
	};
	
}
