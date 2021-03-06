package de.fraunhofer.sit.codescan.sootbridge.typestate;

import heros.FlowFunction;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import de.fraunhofer.sit.codescan.sootbridge.AbstractIFDSAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
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
		AbstractIFDSAnalysisProblem<Abstraction<Var,  State, StmtID>> {

	final IIFDSAnalysisContext context;
	protected final BiDiInterproceduralCFG<Unit,SootMethod> ICFG;

	public AbstractJimpleTypestateAnalysisProblem(IIFDSAnalysisContext context, BiDiInterproceduralCFG<Unit,SootMethod> graph) {
		super(graph);
		this.context = context;
		ICFG = graph;
	}

	protected Abstraction<Var,State,StmtID> createZeroValue() {
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

	/**
	 * Clients must call methods on the parameter object to configure rules that should apply at
	 * normal flow functions. The client must then return the final reference returned
	 * by the fluent API.
	 * @param atNormalEdge reference to the fluent API
	 */
	protected abstract Done<Var,State,StmtID> atNormalEdge(AtNormalEdge<Var,State,StmtID> atNormalEdge);

	protected final class ApplyReturnRules implements
			FlowFunction<Abstraction<Var,  State, StmtID>> {
		private final Unit callSite;
		private final SootMethod callee;

		 ApplyReturnRules(Unit callSite, SootMethod callee) {
			this.callSite = callSite;
			this.callee = callee;
		}

		public Set<Abstraction<Var,  State, StmtID>> computeTargets(Abstraction<Var,  State, StmtID> source) {
			//first apply rules with abstractions at the callee
			Config<Var, State, StmtID> config = new Config<Var,State,StmtID>(source,(Stmt) callSite, context, callee);
			atReturn(config);
			return config.getAbstractions();
		}
	}
	protected final class ApplyNormalRules implements
		FlowFunction<Abstraction<Var,  State, StmtID>> {
		private final Unit callSite;
		
		ApplyNormalRules(Unit callSite) {
			this.callSite = callSite;
		}
		
		public Set<Abstraction<Var,  State, StmtID>> computeTargets(Abstraction<Var,  State, StmtID> source) {
			//first apply rules with abstractions at the callee
			Config<Var, State, StmtID> config = new Config<Var,State,StmtID>(source,(Stmt) callSite, context, null);
			atNormalEdge(config);
			return config.getAbstractions();
		}
	}

	protected class ReplaceValues implements FlowFunction<Abstraction<Var, State, StmtID>> {
		private final List<Value> fromValues;
		private final List<Value> toValues;

		public ReplaceValues(List<Value> fromValues, List<Value> toValues) {
			this.fromValues = fromValues;
			this.toValues = toValues;
		}

		public Set<Abstraction<Var,State,StmtID>> computeTargets(Abstraction<Var,State,StmtID> source) {
			return Collections.singleton(source.replaceValues(fromValues,toValues));
		}
	}
	protected class DestroyLocals implements FlowFunction<Abstraction<Var, State,StmtID>>{
		private final Collection<Local> destroyLocals;

		public DestroyLocals(Collection<Local> destroyLocals) {
			this.destroyLocals = destroyLocals;
		}
		public Set<Abstraction<Var, State, StmtID>> computeTargets(
				Abstraction<Var, State, StmtID> source) {
			Abstraction<Var, State, StmtID> res = source.destroyLocals(destroyLocals);
			if( res == null){
				return Collections.emptySet();
			}
			return Collections.singleton(res);
		}
		
	}
}