package de.fraunhofer.sit.codescan.typestate.analysis;

import heros.FlowFunction;
import heros.FlowFunctions;
import heros.flowfunc.Identity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import de.fraunhofer.sit.codescan.framework.AbstractIFDSAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

public abstract class AbstractTypestateAnalysisProblem<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends
		AbstractIFDSAnalysisProblem<Abstraction<Var, Value, State, StmtID>> {

	protected final JimpleBasedInterproceduralCFG ICFG;

	public AbstractTypestateAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
		ICFG = context.getICFG();
	}

	protected Abstraction<Var,Value,State,StmtID> createZeroValue() {
		return Abstraction.zero();
	}
	
	protected abstract Done<Var,State,StmtID> applyRules(Do<Var,State,StmtID> d);
	
	@Override
	protected FlowFunctions<Unit, Abstraction<Var,Value,State,StmtID>, SootMethod> createFlowFunctionsFactory() {
		
		return new FlowFunctions<Unit, Abstraction<Var,Value,State,StmtID>, SootMethod>() {

			public FlowFunction<Abstraction<Var,Value,State,StmtID>> getCallFlowFunction(Unit src, final SootMethod dest) {
				Stmt stmt = (Stmt) src;
				InvokeExpr ie = stmt.getInvokeExpr();
				final List<Value> callArgs = ie.getArgs();
				final List<Value> parameterRefs = ICFG.getParameterRefs(dest);
				return new FlowFunction<Abstraction<Var,Value,State,StmtID>>() {	
					public Set<Abstraction<Var,Value,State,StmtID>> computeTargets(Abstraction<Var,Value,State,StmtID> source) {
						//no need to pass on ZERO beyond calls
						if(source==zeroValue()) return Collections.emptySet();

						return source.replaceValues(callArgs,parameterRefs);
					}
				};				
			}

			public FlowFunction<Abstraction<Var,Value,State,StmtID>> getCallToReturnFlowFunction(Unit curr, Unit succ) {
				final Stmt s = (Stmt) curr;
				return new FlowFunction<Abstraction<Var,Value,State,StmtID>>() {
					public Set<Abstraction<Var, Value, State, StmtID>> computeTargets(Abstraction<Var, Value, State, StmtID> source) {
						Config<Var, State, StmtID> config = new Config<Var,State,StmtID>(source,s);
						applyRules(config);
						return config.getAbstractions();
					}
				};
			}

			public FlowFunction<Abstraction<Var,Value,State,StmtID>> getNormalFlowFunction(Unit curr,Unit succ) {
				if(curr instanceof DefinitionStmt) {
					final DefinitionStmt assign = (DefinitionStmt) curr;
					return new FlowFunction<Abstraction<Var,Value,State,StmtID>>() {

						public Set<Abstraction<Var,Value,State,StmtID>> computeTargets(final Abstraction<Var,Value,State,StmtID> source) {
							@SuppressWarnings("serial")
							HashSet<Abstraction<Var,Value,State,StmtID>> hashSet = new HashSet<Abstraction<Var,Value,State,StmtID>>() {{
								add(source);
								add(source.replaceValue(assign.getRightOp(),assign.getLeftOp()));
							}};
							return hashSet;
						}
					};
				}
				return Identity.v();
			}

			public FlowFunction<Abstraction<Var,Value,State,StmtID>> getReturnFlowFunction(final Unit callSite, SootMethod callee, final Unit exitStmt, Unit retSite) {
				System.err.println(callee.getActiveBody());
				return new FlowFunction<Abstraction<Var,Value,State,StmtID>>() {
					public Set<Abstraction<Var, Value, State, StmtID>> computeTargets(Abstraction<Var, Value, State, StmtID> source) {
						//TODO add more context
						if(callSite==null) {
							return Collections.emptySet();
						}
							
						Config<Var, State, StmtID> config = new Config<Var,State,StmtID>(source,(Stmt) callSite);
						applyRules(config);
						return config.getAbstractions();
					}
				};
			}
		};
	}

}