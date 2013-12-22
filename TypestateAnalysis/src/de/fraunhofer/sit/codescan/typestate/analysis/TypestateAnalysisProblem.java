package de.fraunhofer.sit.codescan.typestate.analysis;

import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.State.FLUSHED;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.State.TAINTED;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.StatementId.MODEL_VALUE_UPDATE;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.Var.MODEL_VALUE;
import static de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.Var.VALUE_GROUP;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.State;
import de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.StatementId;
import de.fraunhofer.sit.codescan.typestate.analysis.TypestateAnalysisProblem.Var;

public class TypestateAnalysisProblem extends AbstractTypestateAnalysisProblem<Var,State,StatementId> {
	
	private static final String MODEL_VALUE_CLASS_NAME = "example1.ModelValue";
	private static final String MODEL_VALUE_ADD_SIG = "<example1.ValueGroup: void add(example1.ModelValue)>";
	private static final String VALUE_GROUP_FLUSH_SIG = "<example1.ValueGroup: void flush()>";
	private static final String VALUE_GROUP_CONSTRUCTOR_SIG = "<example1.ValueGroup: void <init>()>";

	enum Var { VALUE_GROUP, MODEL_VALUE };
	
	enum State { FLUSHED, TAINTED };
	
	enum StatementId { MODEL_VALUE_UPDATE };

	public TypestateAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
	}

	@Override
	protected Done<Var, State, StatementId> applyRules(Do<Var, State, StatementId> d) {
		return d.atCallTo(VALUE_GROUP_CONSTRUCTOR_SIG).always().trackThis().as(VALUE_GROUP).toState(FLUSHED).orElse().
			     atCallTo(MODEL_VALUE_ADD_SIG).ifValueBoundTo(VALUE_GROUP).equalsThis().trackParameter(0).as(MODEL_VALUE).orElse().
			     atCallTo(VALUE_GROUP_FLUSH_SIG).ifValueBoundTo(VALUE_GROUP).equalsThis().toState(FLUSHED).orElse().
			     atAnyCallToClass(MODEL_VALUE_CLASS_NAME).ifValueBoundTo(MODEL_VALUE).equalsThis().toState(TAINTED).storeStmtAs(MODEL_VALUE_UPDATE);
	}
	
//	@Override
//	protected FlowFunctions<Unit, Abstraction<Var,Value,State,StatementId>, SootMethod> createFlowFunctionsFactory() {
//
//		return new FlowFunctions<Unit, Abstraction<Var,Value,State,StatementId>, SootMethod>() {
//
//			public FlowFunction<Abstraction<Var,Value,State,StatementId>> getCallFlowFunction(Unit src, final SootMethod dest) {
//				Stmt stmt = (Stmt) src;
//				InvokeExpr ie = stmt.getInvokeExpr();
//				final List<Value> callArgs = ie.getArgs();
//				final List<Value> parameterRefs = ICFG.getParameterRefs(dest);
//				return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {	
//					public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(Abstraction<Var,Value,State,StatementId> source) {
//						//no need to pass on ZERO beyond calls
//						if(source==zeroValue()) return Collections.emptySet();
//
//						return source.replaceValues(callArgs,parameterRefs);
//					}
//				};				
//			}
//
//			public FlowFunction<Abstraction<Var,Value,State,StatementId>> getCallToReturnFlowFunction(Unit curr, Unit succ) {
//				final Stmt s = (Stmt) curr;
//				final InvokeExpr ie = s.getInvokeExpr();				
//				if(ie.getMethodRef().getSignature().equals(VALUE_GROUP_CONSTRUCTOR_SIG)) {
//					InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
//					return new Gen<Abstraction<Var,Value,State,StatementId>>(
//							new Abstraction<Var,Value,State,StatementId>(VALUE_GROUP,iie.getBase(),FLUSHED), zeroValue());
//				} else if(ie.getMethodRef().getSignature().equals(MODEL_VALUE_ADD_SIG)) {
//					return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {
//						public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(Abstraction<Var,Value,State,StatementId> source) {
//							InstanceInvokeExpr iie = (InstanceInvokeExpr)ie;
//							if(iie.getBase().equals(source.getValue(VALUE_GROUP))) {
//								//we are adding a value to our value group; change its typestate
//								//and bind the value
//								return source.storeStmt(s, StatementId.VALUE_ADD_CALL).bindValue(iie.getArg(0), MODEL_VALUE);
//							} else {
//								return Collections.singleton(source);
//							}								
//						}
//					};
//				} else if(ie.getMethodRef().getSignature().equals(VALUE_GROUP_FLUSH_SIG)) {
//					return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {
//						public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(Abstraction<Var,Value,State,StatementId> source) {
//							InstanceInvokeExpr iie = (InstanceInvokeExpr)ie;
//							if(iie.getBase().equals(source.getValue(VALUE_GROUP))) {
//								//mark the value group as flushed
//								return Collections.singleton(source.withStateChangedTo(State.FLUSHED));									
//							} else {
//								return Collections.singleton(source);
//							}								
//						}
//					};
//				} else if(Scene.v().getActiveHierarchy().isClassSubclassOfIncluding(
//						ie.getMethodRef().declaringClass(), Scene.v().getSootClass(MODEL_VALUE_CLASS_NAME))) {
//					//on any instance invoke to a model value...
//					if(ie instanceof InstanceInvokeExpr) {
//						final InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
//						return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {
//							public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(Abstraction<Var,Value,State,StatementId> source) {
//								if(source.getValue(MODEL_VALUE)!=null && source.getValue(MODEL_VALUE).equals(iie.getBase())) {
//									//mark the value group as tainted
//									return Collections.singleton(source.withStateChangedTo(TAINTED).storeStmt(s, MODEL_VALUE_UPDATE));									
//								} else {
//									return Collections.singleton(source);
//								}								
//							}
//						};
//					}
//				}				
//				return Identity.v();
//			}
//
//			public FlowFunction<Abstraction<Var,Value,State,StatementId>> getNormalFlowFunction(Unit curr,Unit succ) {
//				if(curr instanceof DefinitionStmt) {
//					final DefinitionStmt assign = (DefinitionStmt) curr;
//					return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {
//
//						public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(final Abstraction<Var,Value,State,StatementId> source) {
//							HashSet<Abstraction<Var,Value,State,StatementId>> hashSet = new HashSet<Abstraction<Var,Value,State,StatementId>>() {{
//								add(source);
//								add(source.replaceValue(assign.getRightOp(),assign.getLeftOp()));
//							}};
//							return hashSet;
//						}
//					};
//				}
//				return Identity.v();
//			}
//
//			public FlowFunction<Abstraction<Var,Value,State,StatementId>> getReturnFlowFunction(final Unit callSite, SootMethod callee, final Unit exitStmt, Unit retSite) {
//				return new FlowFunction<Abstraction<Var,Value,State,StatementId>>() {
//					public Set<Abstraction<Var,Value,State,StatementId>> computeTargets(Abstraction<Var,Value,State,StatementId> source) {
//						//no need to pass on ZERO beyond returns
//						if(source==zeroValue()) return Collections.emptySet();
//						//if we are returning from the method in which the value group was constructed...
//						if(source.stateIs(TAINTED)) {
//							Unit reportStmt = source.getStatement(MODEL_VALUE_UPDATE);
//							String className = interproceduralCFG().getMethodOf(reportStmt).getDeclaringClass().getName();
//							context.reportError(new ErrorMarker("ValueGroup not flushed!",className,reportStmt.getJavaSourceStartLineNumber()));
//							//don't propagate further
//							return Collections.emptySet();
//						}
//						if(exitStmt instanceof ReturnStmt) {
//							ReturnStmt returnStmt = (ReturnStmt) exitStmt;
//							if(callSite instanceof AssignStmt) {
//								AssignStmt assignStmt = (AssignStmt) callSite;
//								Value leftOp = assignStmt.getLeftOp();
//								Value retVal = returnStmt.getOp();
//								return Collections.singleton(source.replaceValue(retVal, leftOp));
//							}
//						}
//						return Collections.emptySet();
//					}
//				};
//
//			}
//		};
//	}

}
