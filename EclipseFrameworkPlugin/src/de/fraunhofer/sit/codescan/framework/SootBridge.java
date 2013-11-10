package de.fraunhofer.sit.codescan.framework;

import heros.InterproceduralCFG;
import heros.solver.IFDSSolver;

import java.util.HashSet;
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
import soot.jimple.Stmt;
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
	 * @param configs A number of {@link IAnalysisPack}s which define how to conduct the analysis.
	 */
	public static void registerAnalysisPack(final Set<String> classesToStartAnalysisAt, final IAnalysisPack... configs) {
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
	
				for(IAnalysisPack config : configs) {
					String superTypeName = config.getSuperClassName();
					for(SootClass c: Scene.v().getApplicationClasses()) {
						//filter by super-class name (if given) and method signature
						if(superTypeName!=null && !superTypeName.isEmpty() &&
						   !Scene.v().getFastHierarchy().isSubclass(c, Scene.v().getSootClass(superTypeName))) continue;
						String subSig = config.getMethodSubSignature();
						Set<SootMethod> methodsToConsider = new HashSet<SootMethod>();
						if(subSig!=null && !subSig.isEmpty()) {
							if(c.declaresMethod(subSig))
								methodsToConsider.add(c.getMethod(subSig));
						} else {
							methodsToConsider.addAll(c.getMethods());
						}							
						for (SootMethod m : methodsToConsider) {
							if(!m.hasActiveBody()) continue;

							IIFDSAnalysisPlugin[] ifdsPlugins = config.getIFDSAnalysisPlugins();
							for (IIFDSAnalysisPlugin plugin : ifdsPlugins) {
								IFDSAdapter ifdsProblem = new IFDSAdapter(icfg, mustAliasManager, plugin, m);
								IFDSSolver<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> solver =
										new JimpleIFDSSolver<Local, InterproceduralCFG<Unit,SootMethod>>(ifdsProblem);
								solver.solve();
								if(ifdsProblem.isMethodVulnerable()) {
									m.addTag(new VulnerableMethodTag(config));
								}
							}
							
							IMethodBasedAnalysisPlugin[] methodPlugins = config.getMethodBasedAnalysisPlugins();
							if(methodPlugins.length>0) {
								final boolean[] vulnerable = new boolean[] { true };
								for (IMethodBasedAnalysisPlugin plugin : methodPlugins) {
									plugin.analyzeMethod(m, new MethodBasedAnalysisManager() {
										
										public boolean mustAlias(Stmt stmt, Local l1, Stmt stmt2, Local l2) {
											return mustAliasManager.mustAlias(stmt, l1, stmt2, l2);
										}
										
										public void markMethodAsBenign() {
											vulnerable[0] = false; 
										}
									});
									if(vulnerable[0]) break;
								}
								if(vulnerable[0]) {
									m.addTag(new VulnerableMethodTag(config));
								}
							}
						}
					}
				}
			}				
		}));		
	}

}
