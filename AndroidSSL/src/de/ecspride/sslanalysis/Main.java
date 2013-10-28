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

	private static final String SUBSIG = "void onReceivedSslError(WebView,SslErrorHandler,SslError)";

	public static void main(String[] args) {
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.sslanalysis", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG();
				for(SootClass c: Scene.v().getApplicationClasses()) {
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
		IFDSSolver<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> solver =
				new JimpleIFDSSolver<Local, InterproceduralCFG<Unit,SootMethod>>(new SSLAnalysisProblem(icfg,m));
		solver.solve();
	}
}
