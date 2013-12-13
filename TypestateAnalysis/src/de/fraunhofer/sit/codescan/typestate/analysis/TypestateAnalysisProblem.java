package de.fraunhofer.sit.codescan.typestate.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import heros.FlowFunction;
import heros.FlowFunctions;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import de.fraunhofer.sit.codescan.framework.AbstractIFDSAnalysisProblem;
import de.fraunhofer.sit.codescan.framework.IFDSAnalysisManager;

@SuppressWarnings("serial")
public class TypestateAnalysisProblem extends AbstractIFDSAnalysisProblem<Abstraction> {

	private static final String VALUE_GROUP_CONSTRUCTOR_SIG = "<example1.ValueGroup: void <init>()>";

	public TypestateAnalysisProblem(IFDSAnalysisManager manager) {
		super(manager);
	}

	protected Abstraction createZeroValue() {
		return Abstraction.ZERO;
	}

	@Override
	protected FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Abstraction, SootMethod>() {

			public FlowFunction<Abstraction> getCallFlowFunction(Unit src, SootMethod dest) {
				return Identity.v();
			}

			public FlowFunction<Abstraction> getCallToReturnFlowFunction(Unit curr, Unit succ) {
				if(curr instanceof AssignStmt) {
					AssignStmt assign = (AssignStmt) curr;
					if(assign.containsInvokeExpr()) {
						InvokeExpr ie = assign.getInvokeExpr();
						if(ie.getMethodRef().getSignature().equals(VALUE_GROUP_CONSTRUCTOR_SIG)) {
							return new Gen<Abstraction>(new Abstraction(assign), zeroValue());
						}
					}
				}
				//TODO handle adding of ModelValue to the ValueGroup etc.
				return Identity.v();
			}

			public FlowFunction<Abstraction> getNormalFlowFunction(Unit curr,Unit succ) {
				if(curr instanceof DefinitionStmt) {
					final DefinitionStmt assign = (DefinitionStmt) curr;
					return new FlowFunction<Abstraction>() {

						public Set<Abstraction> computeTargets(final Abstraction source) {
							//TODO replace also element locals
							if(source.getValueGroupLocal().equals(assign.getRightOp())) {
								return new HashSet<Abstraction>() {{
									add(source);
									add(source.derive(assign.getLeftOp()));
								}};
							} else
								return Collections.singleton(source);
						}
					};
				}
				return Identity.v();
			}

			public FlowFunction<Abstraction> getReturnFlowFunction(Unit callSite, SootMethod callee, Unit exitStmt, Unit retSite) {
				return Identity.v();
			}
		};
	}

}
