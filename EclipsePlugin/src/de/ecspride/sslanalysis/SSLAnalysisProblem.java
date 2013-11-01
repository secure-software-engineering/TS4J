package de.ecspride.sslanalysis;

import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Compose;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;
import heros.flowfunc.Kill;
import heros.flowfunc.KillAll;
import heros.flowfunc.Transfer;
import heros.flowfunc.Union;
import heros.template.DefaultIFDSTabulationProblem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.NullType;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;

/**
 * Implements a must-analysis to check whether sslErrorHandler is in all cases calling proceed() on its handler argument.
 * {@link #isMethodVulnerable()} can be called to collect the result.
 * See AnalysisDesign.txt for details.
 */
public class SSLAnalysisProblem extends DefaultIFDSTabulationProblem<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> {
	
	private final SootMethod sslErrorHandlerMethod;
	
	private boolean methodNotVulnerable = false; 

	public SSLAnalysisProblem(InterproceduralCFG<Unit, SootMethod> icfg, SootMethod sslErrorHandler) {
		super(icfg);
		this.sslErrorHandlerMethod = sslErrorHandler;
	}

	public Map<Unit, Set<Local>> initialSeeds() {
		return DefaultSeeds.make(Collections.singleton(sslErrorHandlerMethod.getActiveBody().getUnits().getFirst()), zeroValue());
	}

	@Override
	protected FlowFunctions<Unit, Local, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctionFactory();
	}

	@Override
	protected Local createZeroValue() {
		return new JimpleLocal("ZERO", NullType.v());
	}
	
	@Override
	public boolean followReturnsPastSeeds() {
		return true;
	}

	@Override
	public boolean autoAddZero() {
		return false;
	}
	
	@Override
	public boolean computeValues() {
		return false;
	}
	
	private class FlowFunctionFactory implements FlowFunctions<Unit, Local, SootMethod> {

		@SuppressWarnings("unchecked")
		public FlowFunction<Local> getNormalFlowFunction(Unit curr, Unit succ) {
			if(methodNotVulnerable) return KillAll.v(); //quit
			
			if(curr instanceof IdentityStmt) {
				if(interproceduralCFG().getMethodOf(curr).equals(sslErrorHandlerMethod)) {
					IdentityStmt identityStmt = (IdentityStmt) curr;
					Value rightOp = identityStmt.getRightOp();
					if(rightOp instanceof ParameterRef) {
						ParameterRef parameterRef = (ParameterRef) rightOp;
						if(parameterRef.getIndex()==1) {
							return Compose.<Local>compose(new Gen<Local>((Local) identityStmt.getLeftOp(),zeroValue()),new Kill<Local>(zeroValue()));
						}
					}
				}
			}
			if(curr instanceof AssignStmt) {
				AssignStmt assignStmt = (AssignStmt) curr;
				Value right = assignStmt.getRightOp();
				if(assignStmt.getLeftOp() instanceof Local) {
					final Local leftLocal = (Local) assignStmt.getLeftOp();
					if(right instanceof Local) {
						final Local rightLocal = (Local) right;
						return new Transfer<Local>(leftLocal, rightLocal);
					} else {
						return new Kill<Local>(leftLocal);
					}
				}
			}
			return Identity.v();
		}

		public FlowFunction<Local> getCallFlowFunction(Unit src, final SootMethod dest) {
			if(methodNotVulnerable) return KillAll.v(); //quit

			Stmt stmt = (Stmt) src;
			InvokeExpr ie = stmt.getInvokeExpr();
			final List<Value> callArgs = ie.getArgs();
			final List<Local> paramLocals = new ArrayList<Local>();
			for(int i=0;i<dest.getParameterCount();i++) {
				paramLocals.add(dest.getActiveBody().getParameterLocal(i));
			}
			return new FlowFunction<Local>() {

				public Set<Local> computeTargets(Local source) {
					int argIndex = callArgs.indexOf(source);
					if(argIndex>-1) {
						return Collections.singleton(paramLocals.get(argIndex));
					}
					return Collections.emptySet();
				}
			};
		}

		@SuppressWarnings("unchecked")
		public FlowFunction<Local> getReturnFlowFunction(Unit callSite, SootMethod callee, Unit exitStmt, Unit retSite) {
			if(methodNotVulnerable) return KillAll.v(); //quit

			if(interproceduralCFG().getMethodOf(exitStmt).equals(sslErrorHandlerMethod)) {
				//an un-proceeded taint reaches the end of the method; method found to be not vulnerable!				
				methodNotVulnerable = true;
				return KillAll.v();
			} else {
				Stmt stmt = (Stmt) callSite;
				InvokeExpr ie = stmt.getInvokeExpr();
				final List<Value> callArgs = ie.getArgs();
				final List<Local> paramLocals = new ArrayList<Local>();
				for(int i=0;i<callee.getParameterCount();i++) {
					paramLocals.add(callee.getActiveBody().getParameterLocal(i));
				}

				FlowFunction<Local> retranslateArguments = new FlowFunction<Local>() {

					public Set<Local> computeTargets(Local source) {
						int paramIndex = paramLocals.indexOf(source);
						if(paramIndex>-1) {
							Value arg = callArgs.get(paramIndex);
							if(arg instanceof Local) {
								Local argLocal = (Local) arg;
								return Collections.singleton(argLocal);
							}
						}
						return Collections.emptySet();
					}
				};

				FlowFunction<Local> handleRetValue = KillAll.v();
				if (exitStmt instanceof ReturnStmt) {								
					ReturnStmt returnStmt = (ReturnStmt) exitStmt;
					Value op = returnStmt.getOp();
					if(op instanceof Local) {
						if(callSite instanceof DefinitionStmt) {
							DefinitionStmt defnStmt = (DefinitionStmt) callSite;
							Value leftOp = defnStmt.getLeftOp();
							if(leftOp instanceof Local) {
								final Local tgtLocal = (Local) leftOp;
								final Local retLocal = (Local) op;
								handleRetValue = new FlowFunction<Local>() {
		
									public Set<Local> computeTargets(Local source) {
										if(source==retLocal)
											return Collections.singleton(tgtLocal);
										return Collections.emptySet();
									}
									
								};
							}
						}
					}
				}
				return Union.<Local>union(retranslateArguments,handleRetValue);
			} 
		}

		public FlowFunction<Local> getCallToReturnFlowFunction(Unit call, Unit returnSite) {
			if(methodNotVulnerable) return KillAll.v(); //quit

			Stmt stmt = (Stmt) call;
			InvokeExpr ie = stmt.getInvokeExpr();
			if(ie.getMethodRef().getSubSignature().toString().equals("void proceed()")) {
				if(ie instanceof InstanceInvokeExpr) {
					InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
					final Value base = iie.getBase();
					return new FlowFunction<Local>() {
						public Set<Local> computeTargets(Local source) {
							if(base.equals(source)) {
								return Collections.emptySet(); //kill
							}
							return Collections.singleton(source); //keep
						}
					};
				}
			}
			final List<Value> callArgs = ie.getArgs();
			return new FlowFunction<Local>() {

				public Set<Local> computeTargets(Local source) {
					int argIndex = callArgs.indexOf(source);
					if(argIndex>-1) {
						return Collections.emptySet(); //kill
					}
					return Collections.singleton(source);
				}
			};
		}
	}

	public boolean isMethodVulnerable() {
		return !methodNotVulnerable;
	}
}
