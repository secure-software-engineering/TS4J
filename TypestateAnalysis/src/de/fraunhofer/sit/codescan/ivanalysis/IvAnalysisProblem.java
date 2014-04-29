package de.fraunhofer.sit.codescan.ivanalysis;

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

public class IvAnalysisProblem extends AbstractJimpleTypestateBackwardsAnalysisProblem<Var, State, StmtID> {

	public IvAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
	}
	enum Var { IV_VALUE };
	enum State {};
	enum StmtID {}
	
	@Override
	protected Done<Var, State, StmtID> atCallToReturn(
			AtCallToReturn<Var, State, StmtID> d) {
		// TODO Auto-generated method stub
		return d.atCallTo("<javax.crypto.spec.IvParameterSpec: void <init>(byte[])>").always().trackParameter(0).asArray(IV_VALUE,16);
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
		return d.atAssignTo(IV_VALUE).ifValueBoundTo(IV_VALUE).each().equalsConstant(soot.jimple.IntConstant.class).reportError("The Initialization Vector is choosen constant!").here().orElse()
				.atReplaceInArray(IV_VALUE).ifValueBoundTo(IV_VALUE).each().equalsConstant(soot.jimple.IntConstant.class).reportError("The Initialization Vector will probably be constant!").here();
	};
	
}
