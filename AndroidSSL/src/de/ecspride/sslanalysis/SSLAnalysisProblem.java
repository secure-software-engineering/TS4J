package de.ecspride.sslanalysis;

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
import soot.jimple.internal.JimpleLocal;

public class SSLAnalysisProblem extends DefaultIFDSTabulationProblem<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> {
	
	private final SootMethod sslErrorHandler;

	public SSLAnalysisProblem(InterproceduralCFG<Unit, SootMethod> icfg, SootMethod sslErrorHandler) {
		super(icfg);
		this.sslErrorHandler = sslErrorHandler;
	}

	public Map<Unit, Set<Local>> initialSeeds() {
		return DefaultSeeds.make(Collections.singleton(sslErrorHandler.getActiveBody().getUnits().getFirst()), zeroValue());
	}

	@Override
	protected FlowFunctions<Unit, Local, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctionFactory(sslErrorHandler,interproceduralCFG(),zeroValue());
	}

	@Override
	protected Local createZeroValue() {
		return new JimpleLocal("ZERO", NullType.v());
	}

}
