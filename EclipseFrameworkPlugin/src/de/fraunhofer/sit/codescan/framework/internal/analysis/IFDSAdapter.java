package de.fraunhofer.sit.codescan.framework.internal.analysis;

import heros.DefaultSeeds;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.template.DefaultIFDSTabulationProblem;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.NullType;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import de.fraunhofer.sit.codescan.framework.IAnalysisContext;
import de.fraunhofer.sit.codescan.framework.IFDSAnalysisConfiguration;
import de.fraunhofer.sit.codescan.framework.IFDSAnalysisManager;
import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;

/**
 * Generic super class for implementing IFDS-based code analyses. Operates on {@link Local}s.
 * The analysis focuses on a given {@link SootMethod}. The method is assumed to be vulnerable unless proven otherwise.
 * If a method is found to be not vulnerable, the analysis should call {@link #markMethodAsBenign()}.
 */
public class IFDSAdapter extends DefaultIFDSTabulationProblem<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> 
implements IFDSAnalysisManager {

	protected boolean methodNotVulnerable = false;
	private final IAnalysisContext context;

	public IFDSAdapter(IAnalysisContext context) {
		super(context.getICFG());
		this.context = context;
	}

	/**
	 * Clients can call this method after analysis to determine whether the respective method is vulnerable.
	 * Usually this method is only used by the framework.  
	 */
	public boolean isMethodVulnerable() {
		return !methodNotVulnerable;
	}
	
	/**
	 * Subclasses should call this method when the analysis found the respective method under analysis not to be vulnerable.
	 */
	public void markMethodAsBenign() {
		methodNotVulnerable = true;
	}

	/**
	 * Checks whether l1 will at stmt definitely point to the same value as l2 at stmt2.
	 */
	public boolean mustAlias(Stmt stmt, Local l1, Stmt stmt2, Local l2) {
		return context.getMustAliasManager().mustAlias(stmt, l1, stmt2, l2);
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

	public Map<Unit, Set<Local>> initialSeeds() {
		return DefaultSeeds.make(Collections.singleton(context.getSootMethod().getActiveBody().getUnits().getFirst()), zeroValue());
	}

	@Override
	protected FlowFunctions<Unit, Local, SootMethod> createFlowFunctionsFactory() {
		IFDSAnalysisConfiguration analysisConfiguration = (IFDSAnalysisConfiguration) context.getAnalysisConfiguration();
		IIFDSAnalysisPlugin ifdsAnalysisPlugin = analysisConfiguration.createIFDSAnalysisPlugin();
		return ifdsAnalysisPlugin.createFlowFunctionsFactory(this);
	}
	
	public SootMethod getMethodToFocusOn() {
		return context.getSootMethod();
	}
}