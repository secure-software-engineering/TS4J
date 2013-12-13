package de.fraunhofer.sit.codescan.framework;

import heros.DefaultSeeds;
import heros.InterproceduralCFG;
import heros.template.DefaultIFDSTabulationProblem;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;

public abstract class AbstractIFDSAnalysisProblem<D> extends DefaultIFDSTabulationProblem<Unit, D, SootMethod, InterproceduralCFG<Unit, SootMethod>> {

	protected final IFDSAnalysisManager manager;

	public AbstractIFDSAnalysisProblem(IFDSAnalysisManager manager) {
		super(manager.getContext().getICFG());
		this.manager = manager;
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
		return DefaultSeeds.make(Collections.singleton(manager.getContext().getSootMethod().getActiveBody().getUnits().getFirst()), zeroValue());
	}

}
