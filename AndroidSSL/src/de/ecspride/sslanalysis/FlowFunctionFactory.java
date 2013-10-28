package de.ecspride.sslanalysis;

import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;
import heros.flowfunc.Kill;
import heros.flowfunc.KillAll;
import heros.flowfunc.Transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;

public class FlowFunctionFactory implements FlowFunctions<Unit, Local, SootMethod> {

	private final InterproceduralCFG<Unit, SootMethod> icfg;
	private final SootMethod sslErrorHandler;
	private final Local zero;

	public FlowFunctionFactory(SootMethod sslErrorHandler, InterproceduralCFG<Unit, SootMethod> interproceduralCFG, Local zero) {
		this.sslErrorHandler = sslErrorHandler;
		this.icfg = interproceduralCFG;
		this.zero = zero;
	}

	public FlowFunction<Local> getNormalFlowFunction(Unit curr, Unit succ) {
		if(curr instanceof IdentityStmt) {
			if(icfg.getMethodOf(curr).equals(sslErrorHandler)) {
				IdentityStmt identityStmt = (IdentityStmt) curr;
				Value rightOp = identityStmt.getRightOp();
				if(rightOp instanceof ParameterRef) {
					ParameterRef parameterRef = (ParameterRef) rightOp;
					if(parameterRef.getIndex()==2) {
						return new Gen<Local>((Local) identityStmt.getLeftOp(),zero);
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

	public FlowFunction<Local> getReturnFlowFunction(Unit callSite, SootMethod callee, Unit exitStmt, Unit retSite) {
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
						return new FlowFunction<Local>() {

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
		return KillAll.v();
	}

	public FlowFunction<Local> getCallToReturnFlowFunction(Unit call, Unit returnSite) {
		return Identity.v();
	}

}
