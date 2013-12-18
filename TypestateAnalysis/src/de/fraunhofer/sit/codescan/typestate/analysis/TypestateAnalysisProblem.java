package de.fraunhofer.sit.codescan.typestate.analysis;

import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.State.FLUSHED;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.State.TAINTED;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.StatementId.MODEL_VALUE_UPDATE;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.Var.MODEL_VALUE;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.Var.VALUE_GROUP;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import de.fraunhofer.sit.codescan.framework.AbstractIFDSAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.ErrorMarker;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.State;
import de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.StatementId;
import de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.Var;

@SuppressWarnings("serial")
public class TypestateAnalysisProblem extends AbstractIFDSAnalysisProblem<Abstraction<Var,Value,State,StatementId>> {
	
	private static final String MODEL_VALUE_CLASS_NAME = "example1.ModelValue";
	private static final String MODEL_VALUE_ADD_SIG = "<example1.ValueGroup: void add(example1.ModelValue)>";
	private static final String VALUE_GROUP_FLUSH_SIG = "<example1.ValueGroup: void flush()>";
	private static final String VALUE_GROUP_CONSTRUCTOR_SIG = "<example1.ValueGroup: void <init>()>";

	enum Var { VALUE_GROUP, MODEL_VALUE };
	
	enum State { FLUSHED, TAINTED };
	
	enum StatementId { VALUE_ADD_CALL, MODEL_VALUE_UPDATE };

	private final JimpleBasedInterproceduralCFG ICFG;

	public TypestateAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
		ICFG = context.getICFG();
	}

	protected Abstraction<Var,Value,State,StatementId> createZeroValue() {
		return Abstraction.zero();
	}

	@Override
	protected FlowFunctions<Unit, Abstraction<Var,Value,State,StatementId>, SootMethod> createFlowFunctionsFactory() {
		
		return new FlowFunctions<Unit, Abstraction<Var,Value,State,StatementId>, SootMethod>() {

			public FlowFunction<Abstraction<Var,Value,State,StatementId>> getCallFlowFunction(Unit src, final SootMethod dest) {
				Stmt stmt = (Stmt) src;
				InvokeExpr ie = stmt.getInvokeExpr();
				final List<Value> callArgs = ie.getArgs();
				final List<Value> parameterRefs = ICFG.getParameterRefs(dest);
				return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {	
					public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(Abstraction<Var,Value,State,StatementId> source) {
						//no need to pass on ZERO beyond calls
						if(source==zeroValue()) return Collections.emptySet();

						return source.replaceValues(callArgs,parameterRefs);
					}
				};				
			}

			public FlowFunction<Abstraction<Var,Value,State,StatementId>> getCallToReturnFlowFunction(Unit curr, Unit succ) {
				final Stmt s = (Stmt) curr;
				final InvokeExpr ie = s.getInvokeExpr();				
				if(ie.getMethodRef().getSignature().equals(VALUE_GROUP_CONSTRUCTOR_SIG)) {
					InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
					return new Gen<Abstraction<Var,Value,State,StatementId>>(
							new Abstraction<Var,Value,State,StatementId>(VALUE_GROUP,iie.getBase(),FLUSHED), zeroValue());
				} else if(ie.getMethodRef().getSignature().equals(MODEL_VALUE_ADD_SIG)) {
					return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {
						public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(Abstraction<Var,Value,State,StatementId> source) {
							InstanceInvokeExpr iie = (InstanceInvokeExpr)ie;
							if(iie.getBase().equals(source.getValue(VALUE_GROUP))) {
								//we are adding a value to our value group; change its typestate
								//and bind the value
								return source.storeStmt(s, StatementId.VALUE_ADD_CALL).bindValue(iie.getArg(0), MODEL_VALUE);
							} else {
								return Collections.singleton(source);
							}								
						}
					};
				} else if(ie.getMethodRef().getSignature().equals(VALUE_GROUP_FLUSH_SIG)) {
					return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {
						public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(Abstraction<Var,Value,State,StatementId> source) {
							InstanceInvokeExpr iie = (InstanceInvokeExpr)ie;
							if(iie.getBase().equals(source.getValue(VALUE_GROUP))) {
								//mark the value group as flushed
								return Collections.singleton(source.withStateChangedTo(State.FLUSHED));									
							} else {
								return Collections.singleton(source);
							}								
						}
					};
				} else if(Scene.v().getActiveHierarchy().isClassSubclassOfIncluding(
							ie.getMethodRef().declaringClass(), Scene.v().getSootClass(MODEL_VALUE_CLASS_NAME))) {
					//on any instance invoke to a model value...
					if(ie instanceof InstanceInvokeExpr) {
						final InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
						return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {
							public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(Abstraction<Var,Value,State,StatementId> source) {
								if(source.getValue(MODEL_VALUE)!=null && source.getValue(MODEL_VALUE).equals(iie.getBase())) {
									//mark the value group as tainted
									return Collections.singleton(source.withStateChangedTo(TAINTED).storeStmt(s, MODEL_VALUE_UPDATE));									
								} else {
									return Collections.singleton(source);
								}								
							}
						};
					}
				}				
				return Identity.v();
			}

			public FlowFunction<Abstraction<Var,Value,State,StatementId>> getNormalFlowFunction(Unit curr,Unit succ) {
				if(curr instanceof DefinitionStmt) {
					final DefinitionStmt assign = (DefinitionStmt) curr;
					return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {

						public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(final Abstraction<Var,Value,State,StatementId> source) {
							HashSet<Abstraction<Var,Value,State,StatementId>> hashSet = new HashSet<Abstraction<Var,Value,State,StatementId>>() {{
								add(source);
								add(source.replaceValue(assign.getRightOp(),assign.getLeftOp()));
							}};
							return hashSet;
						}
					};
				}
				return Identity.v();
			}

			public FlowFunction<Abstraction<Var,Value,State,StatementId>> getReturnFlowFunction(final Unit callSite, SootMethod callee, final Unit exitStmt, Unit retSite) {
				return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {
					public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(Abstraction<Var,Value,State,StatementId> source) {
						//no need to pass on ZERO beyond returns
						if(source==zeroValue()) return Collections.emptySet();
						//if we are returning from the method in which the value group was constructed...
						if(source.stateIs(TAINTED)) {
							Unit reportStmt = source.getStatement(MODEL_VALUE_UPDATE);
							String className = interproceduralCFG().getMethodOf(reportStmt).getDeclaringClass().getName();
							context.reportError(new ErrorMarker("ValueGroup not flushed!",className,reportStmt.getJavaSourceStartLineNumber()));
							//don't propagate further
							return Collections.emptySet();
						}
						if(exitStmt instanceof ReturnStmt) {
							ReturnStmt returnStmt = (ReturnStmt) exitStmt;
							if(callSite instanceof AssignStmt) {
								AssignStmt assignStmt = (AssignStmt) callSite;
								Value leftOp = assignStmt.getLeftOp();
								Value retVal = returnStmt.getOp();
								return Collections.singleton(source.replaceValue(retVal, leftOp));
							}
						}
						return Collections.emptySet();
					}
				};
				
			}
		};
	}

}
