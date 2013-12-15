package de.fraunhofer.sit.codescan.sootbridge;

import heros.InterproceduralCFG;

import java.util.HashMap;
import java.util.Map;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * This class is used to cache must-alias analyses.
 */
public class MustAlias {
	
	private final Map<SootMethod,LocalMustAliasAnalysis> methodToMustAlias;
	private final InterproceduralCFG<Unit, SootMethod> icfg;

	public MustAlias(InterproceduralCFG<Unit, SootMethod> icfg) {
		this.icfg = icfg;
		this.methodToMustAlias = new HashMap<SootMethod, LocalMustAliasAnalysis>();
	}
	
	public boolean mustAlias(Stmt stmt, Local l1, Stmt stmt2, Local l2) {
		if(l1.equals(l2)) return true;
		SootMethod methodOf = icfg.getMethodOf(stmt);
		SootMethod methodOf2 = icfg.getMethodOf(stmt2);
		if(!methodOf.equals(methodOf2)) return false;
		LocalMustAliasAnalysis mustAliasAnalysis = getOrCreateMustAliasAnalysis(methodOf);
		return mustAliasAnalysis.mustAlias(l1, stmt, l2, stmt2);
	}

	protected LocalMustAliasAnalysis getOrCreateMustAliasAnalysis(SootMethod m) {
		LocalMustAliasAnalysis analysis = methodToMustAlias.get(m);
		if(analysis==null) {
			analysis = new LocalMustAliasAnalysis(new ExceptionalUnitGraph(m.getActiveBody()));
			methodToMustAlias.put(m, analysis);
		}
		return analysis;
	}
}
