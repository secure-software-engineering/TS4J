package de.ecspride.sslanalysis;

import heros.InterproceduralCFG;
import heros.solver.IFDSSolver;

import java.util.Map;

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

public class Main {

	public static final String SUBSIG = "void onReceivedSslError(android.webkit.WebView,android.webkit.SslErrorHandler,android.net.http.SslError)";

	public static void main(String[] args) {
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.sslanalysis", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG();
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

	private static void doAnalysis(JimpleBasedInterproceduralCFG icfg, SootMethod m) {
		SSLAnalysisProblem problem = new SSLAnalysisProblem(icfg,m);
		IFDSSolver<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> solver =
				new JimpleIFDSSolver<Local, InterproceduralCFG<Unit,SootMethod>>(problem);
		solver.solve();
		if(problem.isMethodVulnerable()) {
			m.addTag(new VulnerableMethodTag());
		}
	}
}
