package de.fraunhofer.sit.codescan.sootbridge.typestate;

import static heros.TwoElementSet.twoElementSet;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.flowfunc.Compose;
import heros.flowfunc.Identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

/**
 * An abstract Jimple-based typestate-analysis problem that can be configured
 * through a set of rules defined through a fluent API.
 * 
 * @param <Var>
 *            The set of variables used to index over bound values.
 * @param <State>
 *            The finite set of possible internal states.
 * @param <StmtID>
 *            The set of variables used to index over bound statements.
 */
public abstract class AbstractJimpleTypestateBackwardsAnalysisProblem<Var extends Enum<Var>, State extends Enum<State>, StmtID extends Enum<StmtID>>
		extends AbstractJimpleTypestateAnalysisProblem<Var, State, StmtID> {

	protected final BiDiInterproceduralCFG<Unit, SootMethod> ICFG;

	public AbstractJimpleTypestateBackwardsAnalysisProblem(
			IIFDSAnalysisContext context) {
		super(context, context.getBackwardICFG());
		ICFG = context.getICFG();
	}

	@Override
	protected FlowFunctions<Unit, Abstraction<Var, Value, State, StmtID>, SootMethod> createFlowFunctionsFactory() {

		return new FlowFunctions<Unit, Abstraction<Var, Value, State, StmtID>, SootMethod>() {

			/**
			 * On calls, replace returned values are mapped to internal variables.
			 */
			@SuppressWarnings("unchecked")
			public FlowFunction<Abstraction<Var, Value, State, StmtID>> getCallFlowFunction(
					Unit callSite, final SootMethod dest) {
				if (callSite != null) {
					Stmt stmt = (Stmt) callSite;
					InvokeExpr ie = stmt.getInvokeExpr();
					if (!ie.getMethod().equals(dest)
							|| !ie.getMethod().isConcrete())
						return Identity.v();
					List<Value> fromValues = new ArrayList<Value>();
					List<Value> toValues = new ArrayList<Value>();
					Unit calleeExit = dest.getActiveBody().getUnits().getLast();
					// addAliases(callee, fromValues, toValues);
					if (calleeExit instanceof ReturnStmt
							&& callSite instanceof DefinitionStmt) {
						DefinitionStmt definitionStmt = (DefinitionStmt) callSite;
						ReturnStmt returnStmt = (ReturnStmt) calleeExit;
						fromValues.add(definitionStmt.getLeftOp());
						toValues.add(returnStmt.getOp());
					}
					FlowFunction<Abstraction<Var,Value,State, StmtID>> applyReturnRules = new ApplyReturnRules(callSite, dest);
					FlowFunction<Abstraction<Var,Value,State, StmtID>> mapFormalsToActuals = new ReplaceValues(fromValues, toValues);
					return Compose.compose(applyReturnRules,mapFormalsToActuals);
				} 
				return Identity.v();
			}

			/**
			 * On call-to-return, apply the appropriate rules.
			 */
			public FlowFunction<Abstraction<Var, Value, State, StmtID>> getCallToReturnFlowFunction(
					Unit curr, Unit succ) {
				final Stmt s = (Stmt) curr;

				return new FlowFunction<Abstraction<Var, Value, State, StmtID>>() {
					public Set<Abstraction<Var, Value, State, StmtID>> computeTargets(
							Abstraction<Var, Value, State, StmtID> source) {
						Config<Var, State, StmtID> config = new Config<Var, State, StmtID>(
								source, s, context);
						atCallToReturn(config);
						return config.getAbstractions();
					}
				};
			}

			/**
			 * On normal flows we simply track assignments and apply atNormalEdge TODO may need to
			 * configure rules for arithmetic operations.
			 */
			public FlowFunction<Abstraction<Var, Value, State, StmtID>> getNormalFlowFunction(
					Unit curr, Unit succ) {
				if (curr instanceof DefinitionStmt) {
					final DefinitionStmt assign = (DefinitionStmt) curr;
					return new FlowFunction<Abstraction<Var, Value, State, StmtID>>() {
						public Set<Abstraction<Var, Value, State, StmtID>> computeTargets(
								final Abstraction<Var, Value, State, StmtID> source) {
							if(assign instanceof IdentityStmt){
								return Collections.singleton(source.replaceValue(assign.getLeftOp(), assign.getRightOp()));
							}
							Config<Var, State, StmtID> config = new Config<Var, State, StmtID>(
									twoElementSet(
											source,
											source.replaceValue(
													assign.getLeftOp(),
													assign.getRightOp())),
									assign, context, null);
							atNormalEdge(config);
							return config.getAbstractions();
						}
					};
				}
				return Identity.v();
			}

			/**
			 * On returns, apply the appropriate rules and replace formal
			 * parameters by arguments, as well as LHS locals by appropriate return values of the callee (if any).
			 */
			public FlowFunction<Abstraction<Var, Value, State, StmtID>> getReturnFlowFunction(
					final Unit callSite, final SootMethod callee,
					final Unit exitStmt, Unit retSite) {
				if (callSite != null) {
					Stmt stmt = (Stmt) callSite;
					if (!stmt.containsInvokeExpr())
						return Identity.v();
					InvokeExpr ie = stmt.getInvokeExpr();
					if (!ie.getMethod().equals(callee))
						return Identity.v();
					List<Value> callArgs = ie.getArgs();
					List<Value> paramLocals = callee.getActiveBody().getParameterRefs();

					Unit calleeExit = callee.getActiveBody().getUnits().getLast();
					if (calleeExit instanceof ReturnStmt
							&& callSite instanceof DefinitionStmt) {
						DefinitionStmt definitionStmt = (DefinitionStmt) callSite;
						ReturnStmt returnStmt = (ReturnStmt) calleeExit;
						paramLocals = new ArrayList<Value>(paramLocals);
						callArgs = new ArrayList<Value>(callArgs);
						callArgs.add(definitionStmt.getLeftOp());
						paramLocals.add(returnStmt.getOp());
					}
					FlowFunction<Abstraction<Var, Value, State, StmtID>> mapFormalsToActuals = new ReplaceValues(paramLocals, callArgs);
					return mapFormalsToActuals;
				} else {
					return Identity.v();
				}
			}
		};
	}

	protected Set<Unit> getStartSeed() {
		return Collections.singleton(context.getSootMethod().getActiveBody()
				.getUnits().getLast());
	}

}