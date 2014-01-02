package de.fraunhofer.sit.codescan.sootbridge;

import heros.DefaultSeeds;
import heros.InterproceduralCFG;
import heros.template.DefaultIFDSTabulationProblem;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;

public abstract class AbstractIFDSAnalysisProblem<D> extends DefaultIFDSTabulationProblem<Unit, D, SootMethod, InterproceduralCFG<Unit, SootMethod>> {

	protected final IIFDSAnalysisContext context;

	public AbstractIFDSAnalysisProblem(IIFDSAnalysisContext context) {
		super(context.getICFG());
		this.context = context;
	}

	public boolean autoAddZero() {
		return false;
	}

	public boolean computeValues() {
		return false;
	}

	public boolean followReturnsPastSeeds() {
		return true;
	}

	public Map<Unit, Set<D>> initialSeeds() {
		return DefaultSeeds.make(Collections.singleton(context.getSootMethod().getActiveBody().getUnits().getFirst()), zeroValue());
	}

}
