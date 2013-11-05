package de.fraunhofer.sit.codescan.androidssl.analysis;

import heros.InterproceduralCFG;
import heros.solver.IFDSSolver;

import java.util.Map;

import soot.Body;
import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

public class Main {

	public static final String SUBSIG = "void onReceivedSslError(android.webkit.WebView,android.webkit.SslErrorHandler,android.net.http.SslError)";

	/**
	 * Runs the actual analysis with Soot's arguments.
	 */
	public static void main(String[] args) {
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.sslanalysis", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG() {
					@Override
					protected synchronized DirectedGraph<Unit> makeGraph(Body body) {
						//we use brief unit graphs such that we warn in situations where
						//the code only might be safe due to some exceptional flows
						return new BriefUnitGraph(body);
					}
				};
				for(SootClass c: Scene.v().getApplicationClasses()) {
					if(!Scene.v().getFastHierarchy().isSubclass(c, Scene.v().getSootClass("android.webkit.WebViewClient"))) continue;
					if(!c.declaresMethod(SUBSIG)) continue;

					SootMethod m = c.getMethod(SUBSIG);
					if(!m.hasActiveBody()) continue;
					
					doAnalysis(icfg,m);
				}
			}

		}));		
		soot.Main.main(args);
	}

	/**
	 * Conducts the analysis with the given method as start point.
	 * Adds a {@link VulnerableMethodTag} to the method if it is found to be vulnerable.
	 */
	private static void doAnalysis(JimpleBasedInterproceduralCFG icfg, SootMethod m) {
		DefaultAnalysisProblem problem = new SSLAnalysisProblem(icfg,m);
		IFDSSolver<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> solver =
				new JimpleIFDSSolver<Local, InterproceduralCFG<Unit,SootMethod>>(problem);
		solver.solve();
		if(problem.isMethodVulnerable()) {
			m.addTag(new VulnerableMethodTag());
		}
	}
}
