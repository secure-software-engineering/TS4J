package de.fraunhofer.sit.codescan.sootbridge.typestate;

import static heros.TwoElementSet.twoElementSet;
import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.flowfunc.Compose;
import heros.flowfunc.Identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import de.fraunhofer.sit.codescan.sootbridge.AbstractIFDSAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateAnalysisProblem.ApplyReturnRules;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateAnalysisProblem.ReplaceValues;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;

/**
 * An abstract Jimple-based typestate-analysis problem that can be configured through a set of rules defined
 * through a fluent API.
 *
 * @param <Var> The set of variables used to index over bound values.
 * @param <State> The finite set of possible internal states.
 * @param <StmtID> The set of variables used to index over bound statements. 
 */
public abstract class AbstractJimpleTypestateForwardAnalysisProblem<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends
AbstractJimpleTypestateAnalysisProblem<Var, State, StmtID> {

	protected final BiDiInterproceduralCFG<Unit,SootMethod> ICFG;

	public AbstractJimpleTypestateForwardAnalysisProblem(IIFDSAnalysisContext context) {
		super(context, context.getICFG());
		ICFG = context.getICFG();
	}
	@Override
	protected FlowFunctions<Unit, Abstraction<Var,Value,State,StmtID>, SootMethod> createFlowFunctionsFactory() {
		
		return new FlowFunctions<Unit, Abstraction<Var,Value,State,StmtID>, SootMethod>() {

			/**
			 * On calls, replace arguments by formal parameters.
			 */
			public FlowFunction<Abstraction<Var,Value,State,StmtID>> getCallFlowFunction(Unit src, final SootMethod dest) {

				Stmt stmt = (Stmt) src;
				InvokeExpr ie = stmt.getInvokeExpr();
				if(!ie.getMethod().equals(dest))
					return Identity.v();
				List<Value> callArgs = ie.getArgs();
				List<Value> parameterRefs = ICFG.getParameterRefs(dest);
				return new ReplaceValues(callArgs, parameterRefs);				
			}

			/**
			 * On call-to-return, apply the appropriate rules.
			 */
			public FlowFunction<Abstraction<Var,Value,State,StmtID>> getCallToReturnFlowFunction(Unit curr, Unit succ) {
				final Stmt s = (Stmt) curr;

				return new FlowFunction<Abstraction<Var,Value,State,StmtID>>() {
					public Set<Abstraction<Var, Value, State, StmtID>> computeTargets(Abstraction<Var, Value, State, StmtID> source) {
						Config<Var, State, StmtID> config = new Config<Var,State,StmtID>(source,s,context);
						atCallToReturn(config);
						return config.getAbstractions();
					}
				};
			}

			/**
			 * On normal flows we simply track assignments.
			 * TODO may need to configure rules for arithmetic operations.
			 */
			public FlowFunction<Abstraction<Var,Value,State,StmtID>> getNormalFlowFunction(Unit curr,Unit succ) {
				if(curr instanceof DefinitionStmt) {
					final DefinitionStmt assign = (DefinitionStmt) curr;
					return new FlowFunction<Abstraction<Var,Value,State,StmtID>>() {
						public Set<Abstraction<Var,Value,State,StmtID>> computeTargets(final Abstraction<Var,Value,State,StmtID> source) {
							Config<Var, State, StmtID> config = new Config<Var,State,StmtID>(twoElementSet(source, source.replaceValue(assign.getRightOp(), assign.getLeftOp())),assign,context, null);
							atNormalEdge(config);
							return config.getAbstractions();
						}
					};
				}
				return Identity.v();
			}

			/**
			 * On returns, apply the appropriate rules and replace formal parameters by arguments, as well as return locals by
			 * LHS of the assignment of the call (if any).
			 */
			@SuppressWarnings("unchecked")
			public FlowFunction<Abstraction<Var,Value,State,StmtID>> getReturnFlowFunction(final Unit callSite, final SootMethod callee, final Unit exitStmt, Unit retSite) {

				if(callSite!=null) {
					Stmt stmt = (Stmt) callSite;
					InvokeExpr ie = stmt.getInvokeExpr();
					if(!ie.getMethod().equals(callee))
						return Identity.v();
					List<Value> fromValues = new ArrayList<Value>(ICFG.getParameterRefs(callee));
					List<Value> toValues = new ArrayList<Value>(ie.getArgs());
					addAliases(callee, fromValues, toValues);
					if(exitStmt instanceof ReturnStmt && callSite instanceof DefinitionStmt) {
						DefinitionStmt definitionStmt = (DefinitionStmt) callSite;
						ReturnStmt returnStmt = (ReturnStmt) exitStmt;
						fromValues = new ArrayList<Value>(fromValues);
						fromValues.add(returnStmt.getOp());
						toValues = new ArrayList<Value>(toValues);
						toValues.add(definitionStmt.getLeftOp());
					}
					FlowFunction<Abstraction<Var,Value,State,StmtID>> applyRules = new ApplyReturnRules(callSite, callee);
					FlowFunction<Abstraction<Var,Value,State,StmtID>> mapFormalsToActuals = new ReplaceValues(fromValues, toValues);
					return Compose.compose(applyRules,mapFormalsToActuals);
				} else {
					//we have an unbalanced problem and the callsite is null; hence there is no caller to map back to
					return new ApplyReturnRules(callSite, callee);
				}
			}

			/**
			 * Adds from/to mappings also for all aliases of from-values.
			 */
			private void addAliases(final SootMethod fromMethod, List<Value> fromValues, List<Value> toValues) {
				for(ListIterator<Value> fromIter=fromValues.listIterator(), toIter=toValues.listIterator(); fromIter.hasNext();) {
					Value fromValue = fromIter.next();
					Value toValue = toIter.next();
					for(Value fromValueAlias: context.mayAliasesAtExit(fromValue, fromMethod)) {
						if(fromValue==fromValueAlias) continue;
						//we also want to replace the alias by the same to-value
						fromIter.add(fromValueAlias);
						toIter.add(toValue);
					}
				}
			}
		};
	}
	protected Set<Unit> getStartSeed(){
		return Collections.singleton(context.getSootMethod().getActiveBody().getUnits().getFirst());
	}

}