package de.fraunhofer.sit.codescan.typestate.analysis;

import heros.FlowFunction;
import heros.FlowFunctions;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import de.fraunhofer.sit.codescan.framework.AbstractIFDSAnalysisProblem;
import de.fraunhofer.sit.codescan.framework.IFDSAnalysisManager;

@SuppressWarnings("serial")
public class TypestateAnalysisProblem extends AbstractIFDSAnalysisProblem<Abstraction> {

	private static final String MODEL_VALUE_ADD_SIG = "<example1.ValueGroup: void add(example1.ModelValue)>";
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
				final Stmt s = (Stmt) curr;
				if(s instanceof AssignStmt) {
					AssignStmt assign = (AssignStmt) curr;
					if(assign.containsInvokeExpr()) {
						InvokeExpr ie = assign.getInvokeExpr();
						if(ie.getMethodRef().getSignature().equals(VALUE_GROUP_CONSTRUCTOR_SIG)) {
							return new Gen<Abstraction>(new Abstraction(assign.getLeftOp()), zeroValue());
						}
					}
				}
				if(s.containsInvokeExpr()) {
					final InvokeExpr ie = s.getInvokeExpr();
					if(ie.getMethodRef().getSignature().equals(MODEL_VALUE_ADD_SIG)) {
						new FlowFunction<Abstraction>() {
							public Set<Abstraction> computeTargets(Abstraction source) {
								if(source.getValueGroupLocal().equals(((InstanceInvokeExpr)ie).getBase())) {
									//we are adding a value to our value group; change its typestate
									//and bind the value
									return Collections.singleton(source.valueAdded(s));									
								} else {
									return Collections.singleton(source);
								}								
							}
						};
					}
				}
				
				return Identity.v();
			}

			public FlowFunction<Abstraction> getNormalFlowFunction(Unit curr,Unit succ) {
				if(curr instanceof DefinitionStmt) {
					final DefinitionStmt assign = (DefinitionStmt) curr;
					return new FlowFunction<Abstraction>() {

						public Set<Abstraction> computeTargets(final Abstraction source) {
							if(source.getValueGroupLocal().equals(assign.getRightOp())) {
								return new HashSet<Abstraction>() {{
									add(source);
									add(source.derive(assign.getLeftOp(),assign.getRightOp()));
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
