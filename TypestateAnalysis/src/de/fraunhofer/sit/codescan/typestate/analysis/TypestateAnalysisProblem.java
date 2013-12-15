package de.fraunhofer.sit.codescan.typestate.analysis;

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
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import de.fraunhofer.sit.codescan.framework.AbstractIFDSAnalysisProblem;
import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisManager;

@SuppressWarnings("serial")
public class TypestateAnalysisProblem extends AbstractIFDSAnalysisProblem<Abstraction> {

	private static final String MODEL_VALUE_CLASS_NAME = "example1.ModelValue";
	private static final String MODEL_VALUE_ADD_SIG = "<example1.ValueGroup: void add(example1.ModelValue)>";
	private static final String MODEL_GROUP_FLUSH_SIG = "<example1.ValueGroup: void flush()>";
	private static final String VALUE_GROUP_CONSTRUCTOR_SIG = "<example1.ValueGroup: void <init>()>";
	private final JimpleBasedInterproceduralCFG ICFG;

	public TypestateAnalysisProblem(IIFDSAnalysisManager manager) {
		super(manager);
		ICFG = manager.getContext().getICFG();
	}

	protected Abstraction createZeroValue() {
		return Abstraction.ZERO;
	}

	@Override
	protected FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
		
		return new FlowFunctions<Unit, Abstraction, SootMethod>() {

			public FlowFunction<Abstraction> getCallFlowFunction(Unit src, final SootMethod dest) {
				Stmt stmt = (Stmt) src;
				InvokeExpr ie = stmt.getInvokeExpr();
				final List<Value> callArgs = ie.getArgs();
				final List<ParameterRef> parameterRefs = ICFG.getParameterRefs(dest);
				return new FlowFunction<Abstraction>() {	
					public Set<Abstraction> computeTargets(Abstraction source) {
						//no need to pass on ZERO beyond calls
						if(source==zeroValue()) return Collections.emptySet();

						return source.replaceValues(callArgs,parameterRefs);
					}
				};				
			}

			public FlowFunction<Abstraction> getCallToReturnFlowFunction(Unit curr, Unit succ) {
				final Stmt s = (Stmt) curr;
				final InvokeExpr ie = s.getInvokeExpr();
				if(ie.getMethodRef().getSignature().equals(VALUE_GROUP_CONSTRUCTOR_SIG)) {
					return new Gen<Abstraction>(new Abstraction(s), zeroValue());
				} else if(ie.getMethodRef().getSignature().equals(MODEL_VALUE_ADD_SIG)) {
					return new FlowFunction<Abstraction>() {
						public Set<Abstraction> computeTargets(Abstraction source) {
							InstanceInvokeExpr iie = (InstanceInvokeExpr)ie;
							if(source.getValueGroupLocal().equals(iie.getBase())) {
								//we are adding a value to our value group; change its typestate
								//and bind the value
								return Collections.singleton(source.valueAdded(s));									
							} else {
								return Collections.singleton(source);
							}								
						}
					};
				} else if(ie.getMethodRef().getSignature().equals(MODEL_GROUP_FLUSH_SIG)) {
					return new FlowFunction<Abstraction>() {
						public Set<Abstraction> computeTargets(Abstraction source) {
							InstanceInvokeExpr iie = (InstanceInvokeExpr)ie;
							if(source.getValueGroupLocal().equals(iie.getBase())) {
								//mark the value group as flushed
								return Collections.singleton(source.markedAsFlushed());									
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
						return new FlowFunction<Abstraction>() {
							public Set<Abstraction> computeTargets(Abstraction source) {
								if(source.getModelValueLocal()!=null && source.getModelValueLocal().equals(iie.getBase())) {
									//mark the value group as tainted
									return Collections.singleton(source.markedAsTainted());									
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
							HashSet<Abstraction> hashSet = new HashSet<Abstraction>() {{
								add(source);
								add(source.replaceValue(assign.getRightOp(),assign.getLeftOp()));
							}};
							return hashSet;
						}
					};
				}
				return Identity.v();
			}

			public FlowFunction<Abstraction> getReturnFlowFunction(final Unit callSite, final SootMethod callee, final Unit exitStmt, Unit retSite) {
				return new FlowFunction<Abstraction>() {
					public Set<Abstraction> computeTargets(Abstraction source) {
						//no need to pass on ZERO beyond returns
						if(source==zeroValue()) return Collections.emptySet();
						
						//if we are returning from the method in which the value group was constructed...
						if(interproceduralCFG().getMethodOf(source.getConstrCallToValueGroup()).equals(callee)) {
							if(!source.isFlushed()) {
								//TODO report error
								System.err.println("VIOLATION FOUND!");
							}
							//don't check past this return edge
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
