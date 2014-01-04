package de.fraunhofer.sit.codescan.sootbridge.typestate;

import static heros.TwoElementSet.twoElementSet;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.flowfunc.Compose;
import heros.flowfunc.Identity;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import de.fraunhofer.sit.codescan.sootbridge.AbstractIFDSAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
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
public abstract class AbstractJimpleTypestateAnalysisProblem<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends
		AbstractIFDSAnalysisProblem<Abstraction<Var, Value, State, StmtID>> {

	protected final JimpleBasedInterproceduralCFG ICFG;

	public AbstractJimpleTypestateAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
		ICFG = context.getICFG();
	}

	protected Abstraction<Var,Value,State,StmtID> createZeroValue() {
		return Abstraction.zero();
	}
	
	/**
	 * Clients must call methods on the parameter object to configure rules that should apply at
	 * call-to-return flow functions. The client must then return the final reference returned
	 * by the fluent API.
	 * @param atCallToReturn reference to the fluent API
	 */
	protected abstract Done<Var,State,StmtID> atCallToReturn(AtCallToReturn<Var,State,StmtID> atCallToReturn);

	/**
	 * Clients must call methods on the parameter object to configure rules that should apply at
	 * return flow functions. The client must then return the final reference returned
	 * by the fluent API.
	 * The rules are applied at the side of the callee, i.e., with value referring to
	 * callee-side names. 
	 * 
	 * @param atReturn reference to the fluent API
	 * @return
	 */
	protected abstract Done<Var,State,StmtID> atReturn(AtReturn<Var,State,StmtID> atReturn);

	@Override
	protected FlowFunctions<Unit, Abstraction<Var,Value,State,StmtID>, SootMethod> createFlowFunctionsFactory() {
		
		return new FlowFunctions<Unit, Abstraction<Var,Value,State,StmtID>, SootMethod>() {

			/**
			 * On calls, replace arguments by formal parameters.
			 */
			public FlowFunction<Abstraction<Var,Value,State,StmtID>> getCallFlowFunction(Unit src, final SootMethod dest) {
				Stmt stmt = (Stmt) src;
				InvokeExpr ie = stmt.getInvokeExpr();
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
							return twoElementSet(source, source.replaceValue(assign.getRightOp(),assign.getLeftOp()));
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

	private final class ApplyReturnRules implements
			FlowFunction<Abstraction<Var, Value, State, StmtID>> {
		private final Unit callSite;
		private final SootMethod callee;

		private ApplyReturnRules(Unit callSite, SootMethod callee) {
			this.callSite = callSite;
			this.callee = callee;
		}

		public Set<Abstraction<Var, Value, State, StmtID>> computeTargets(Abstraction<Var, Value, State, StmtID> source) {
			//first apply rules with abstractions at the callee
			Config<Var, State, StmtID> config = new Config<Var,State,StmtID>(source,(Stmt) callSite, context, callee);
			atReturn(config);
			return config.getAbstractions();
		}
	}

	private class ReplaceValues implements FlowFunction<Abstraction<Var, Value, State, StmtID>> {
		private final List<Value> fromValues;
		private final List<Value> toValues;

		private ReplaceValues(List<Value> fromValues, List<Value> toValues) {
			this.fromValues = fromValues;
			this.toValues = toValues;
		}

		public Set<Abstraction<Var,Value,State,StmtID>> computeTargets(Abstraction<Var,Value,State,StmtID> source) {
			return source.replaceValues(fromValues,toValues);
		}
	}
}