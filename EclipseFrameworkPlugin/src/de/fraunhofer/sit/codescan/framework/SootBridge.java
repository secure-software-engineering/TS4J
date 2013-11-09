package de.fraunhofer.sit.codescan.framework;

import heros.InterproceduralCFG;
import heros.solver.IFDSSolver;

import java.util.Map;
import java.util.Set;

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
import de.fraunhofer.sit.codescan.framework.internal.analysis.IFDSAdapter;
import de.fraunhofer.sit.codescan.framework.internal.analysis.MustAlias;

/**
 * Registers an analysis pack with Soot, which can then be executed by calling {@link soot.Main#main(String[])}.
 * This is currently used internally within the plugin but also by the test harness.
 */
public class SootBridge {

	/**
	 * @param classesToStartAnalysisAt Those classes will be given as argument classes to Soot.
	 * @param configs A number of {@link AnalysisConfiguration}s which define how to conduct the analysis.
	 */
	public static void registerAnalysisPack(final Set<String> classesToStartAnalysisAt, final AnalysisConfiguration... configs) {
		//register analyses
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.vulnanalysis", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				//create single ICFG and MustAlias objects used for all the analyses
				final JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG() {
					@Override
					protected synchronized DirectedGraph<Unit> makeGraph(Body body) {
						//we use brief unit graphs such that we warn in situations where
						//the code only might be safe due to some exceptional flows
						return new BriefUnitGraph(body);
					}
				};
				final MustAlias mustAliasManager = new MustAlias(icfg);
	
				for(AnalysisConfiguration config : configs) {
					String superTypeName = config.getSuperClassName();
					for(SootClass c: Scene.v().getApplicationClasses()) {
						//filter by super-class name (if given) and method signature
						if(superTypeName!=null &&
						   !Scene.v().getFastHierarchy().isSubclass(c, Scene.v().getSootClass(superTypeName))) continue;
						String subSig = config.getMethodSubSignature();
						if(!c.declaresMethod(subSig)) continue;
						
						SootMethod m = c.getMethod(subSig);
						if(!m.hasActiveBody()) continue;
						
						
						AnalysisPlugin plugin = config.getAnalysisPlugin();
						IFDSAdapter ifdsProblem = new IFDSAdapter(icfg, mustAliasManager, plugin, m);
						IFDSSolver<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> solver =
								new JimpleIFDSSolver<Local, InterproceduralCFG<Unit,SootMethod>>(ifdsProblem);
						solver.solve();
						if(ifdsProblem.isMethodVulnerable()) {
							m.addTag(new VulnerableMethodTag());
						}
					}
				}
			}				
		}));		
	}

}
