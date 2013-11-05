package de.fraunhofer.sit.codescan.androidssl.analysis;

import heros.InterproceduralCFG;
import heros.template.DefaultIFDSTabulationProblem;

import java.util.HashMap;
import java.util.Map;

import soot.Local;
import soot.NullType;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;
import de.fraunhofer.sit.codescan.androidssl.Constants;

public abstract class DefaultAnalysisProblem extends DefaultIFDSTabulationProblem<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> {

	private final Map<SootMethod,LocalMustAliasAnalysis> methodToMustAlias;
	protected boolean methodNotVulnerable = false;

	public DefaultAnalysisProblem(InterproceduralCFG<Unit,SootMethod> icfg) {
		super(icfg);
		this.methodToMustAlias = new HashMap<SootMethod, LocalMustAliasAnalysis>();
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

	public boolean isMethodVulnerable() {
		return !methodNotVulnerable;
	}
	
	public void markMethodAsBenign() {
		methodNotVulnerable = true;
	}

	private LocalMustAliasAnalysis getOrCreateMustAliasAnalysis(SootMethod m) {
		LocalMustAliasAnalysis analysis = methodToMustAlias.get(m);
		if(analysis==null) {
			analysis = new LocalMustAliasAnalysis(new ExceptionalUnitGraph(m.getActiveBody()));
			methodToMustAlias.put(m, analysis);
		}
		return analysis;
	}

	protected boolean mustAlias(Stmt stmt, Local l1, Local l2) {
		if(l1.equals(l2)) return true;
		if(Constants.USE_MUST_ALIAS_ANALYSIS) {
			LocalMustAliasAnalysis mustAliasAnalysis = getOrCreateMustAliasAnalysis(interproceduralCFG().getMethodOf(stmt));
			return mustAliasAnalysis.mustAlias(l1, stmt, l2, stmt);
		} else {
			return false;
		}
	}

}