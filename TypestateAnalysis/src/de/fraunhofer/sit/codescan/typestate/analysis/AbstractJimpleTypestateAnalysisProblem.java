package de.fraunhofer.sit.codescan.typestate.analysis;

import static heros.TwoElementSet.twoElementSet;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.flowfunc.Identity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import de.fraunhofer.sit.codescan.framework.AbstractIFDSAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

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
				final List<Value> callArgs = ie.getArgs();
				final List<Value> parameterRefs = ICFG.getParameterRefs(dest);
				return new FlowFunction<Abstraction<Var,Value,State,StmtID>>() {	
					public Set<Abstraction<Var,Value,State,StmtID>> computeTargets(Abstraction<Var,Value,State,StmtID> source) {
						return source.replaceValues(callArgs,parameterRefs);
					}
				};				
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
			public FlowFunction<Abstraction<Var,Value,State,StmtID>> getReturnFlowFunction(final Unit callSite, final SootMethod callee, final Unit exitStmt, Unit retSite) {
				if(callSite!=null) {
					Stmt stmt = (Stmt) callSite;
					InvokeExpr ie = stmt.getInvokeExpr();
					final List<Value> callArgs = ie.getArgs();
					//FIXME must also map back all may-aliases
					final List<Value> parameterRefs = ICFG.getParameterRefs(callee);
					Value retFrom = null, retTo = null;
					if(exitStmt instanceof ReturnStmt && callSite instanceof DefinitionStmt) {
						DefinitionStmt definitionStmt = (DefinitionStmt) callSite;
						ReturnStmt returnStmt = (ReturnStmt) exitStmt;
						retFrom = returnStmt.getOp();
						retTo = definitionStmt.getLeftOp();
					}
					final Value fRetFrom = retFrom, fRetTo = retTo;
					return new FlowFunction<Abstraction<Var,Value,State,StmtID>>() {
						public Set<Abstraction<Var, Value, State, StmtID>> computeTargets(Abstraction<Var, Value, State, StmtID> source) {
							//first apply rules with abstractions at the callee
							Config<Var, State, StmtID> config = new Config<Var,State,StmtID>(source,(Stmt) callSite, context, callee);
							atReturn(config);
							Set<Abstraction<Var, Value, State, StmtID>> abstractionWithRulesApplied = config.getAbstractions();
							
							//then map back to caller's context
							Set<Abstraction<Var, Value, State, StmtID>> res = new HashSet<Abstraction<Var,Value,State,StmtID>>();
							for (Abstraction<Var, Value, State, StmtID> abs : abstractionWithRulesApplied) {
								if(fRetFrom!=null){
									abs = abs.replaceValue(fRetFrom, fRetTo);
								}
								res.addAll(abs.replaceValues(parameterRefs, callArgs));
							}
							
							return res;
						}
					};
				} else {
					//we have an unbalanced problem and the callsite is null; hence there is no caller to map back to
					return new FlowFunction<Abstraction<Var,Value,State,StmtID>>() {
						public Set<Abstraction<Var, Value, State, StmtID>> computeTargets(Abstraction<Var, Value, State, StmtID> source) {
							//just apply rules with abstractions at the callee
							Config<Var, State, StmtID> config = new Config<Var,State,StmtID>(source,(Stmt) callSite, context, callee);
							atReturn(config);
							return config.getAbstractions();
						}
					};
				}
			}
		};
	}

}