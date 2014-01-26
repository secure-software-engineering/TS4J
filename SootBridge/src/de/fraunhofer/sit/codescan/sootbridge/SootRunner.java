package de.fraunhofer.sit.codescan.sootbridge;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.G;
import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.BackwardsInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import com.google.common.base.Joiner;

import de.fraunhofer.sit.codescan.sootbridge.internal.LoggingOutputStream;

public class SootRunner {
	
	private static final class Transformer<C extends IAnalysisConfiguration> extends SceneTransformer {
		private final Map<C, Set<ErrorMarker>> analysisConfigToResultingErrorMarkers;
		private final Map<C, Set<String>> analysisToEntryMethodSignatures;
		private JimpleBasedInterproceduralCFG icfg;
		private BackwardsInterproceduralCFG bicfg;
		private AliasAnalysisManager aliasAnalysisManager;

		private Transformer(
				Map<C, Set<ErrorMarker>> analysisConfigToResultingErrorMarkers,
				Map<C, Set<String>> analysisToEntryMethodSignatures) {
			this.analysisConfigToResultingErrorMarkers = analysisConfigToResultingErrorMarkers;
			this.analysisToEntryMethodSignatures = analysisToEntryMethodSignatures;
		}

		@Override
		protected void internalTransform(String phaseName, Map<String, String> options) {

			for(Map.Entry<C, Set<String>> analysisAndMethodSignatures: analysisToEntryMethodSignatures.entrySet()) {
				for(final String methodSignature: analysisAndMethodSignatures.getValue()) {
					final SootMethod m;
					try {
						m = Scene.v().getMethod(methodSignature);
					} catch(RuntimeException e) {
						LOGGER.debug("Failed to find SootMethod:"+methodSignature);
						continue;
					}
					if(!m.hasActiveBody()) continue;

					final C analysisConfig = analysisAndMethodSignatures.getKey();
					analysisConfig.runAnalysis(new IIFDSAnalysisContext() {

						public SootMethod getSootMethod() {
							return m;
						}
						
						public BiDiInterproceduralCFG<Unit,SootMethod> getICFG() {
							return getOrCreateICFG();
						}
						
						public IAnalysisConfiguration getAnalysisConfiguration() {
							return analysisConfig;
						}

						@Override
						public void reportError(ErrorMarker... result) {
							Set<ErrorMarker> set = analysisConfigToResultingErrorMarkers.get(analysisConfig);
							if(set==null) {
								set = new HashSet<ErrorMarker>();
								analysisConfigToResultingErrorMarkers.put(analysisConfig, set);
							}
							set.addAll(Arrays.asList(result));
						}

						@Override
						public boolean mustAlias(Stmt stmt, Local l1, Stmt stmt2, Local l2) {
							return getOrCreateAnalysisManager().mustAlias(stmt, l1, stmt2, l2);
						}

						@Override
						public BiDiInterproceduralCFG<Unit,SootMethod> getBackwardICFG() {
							return getOrCreateBackwardsICFG();
						}

						@Override
						public Set<Value> mayAliasesAtExit(Value v, SootMethod owner) {
							return getOrCreateAnalysisManager().mayAliasesAtExit(v, owner);
						}

						private BiDiInterproceduralCFG<Unit, SootMethod> getOrCreateBackwardsICFG() {
							if(bicfg==null)
								bicfg = new BackwardsInterproceduralCFG(getOrCreateICFG());
							return bicfg;
						}

						private AliasAnalysisManager getOrCreateAnalysisManager() {
							if(aliasAnalysisManager==null) {
								new AliasAnalysisManager(getOrCreateICFG());
							}
							return aliasAnalysisManager;
						}
						
						private JimpleBasedInterproceduralCFG getOrCreateICFG() {
							if(icfg ==null) {
								icfg = new JimpleBasedInterproceduralCFG() {
									@Override
									protected synchronized DirectedGraph<Unit> makeGraph(Body body) {
										//we use brief unit graphs such that we warn in situations where
										//the code only might be safe due to some exceptional flows
										return new BriefUnitGraph(body);
									}
								};
							}
							return icfg;
						}
					});
					
				}
			}
		}

	}


	private final static Logger LOGGER = LoggerFactory.getLogger(SootRunner.class);
	
	public static <C extends IAnalysisConfiguration> Map<C, Set<ErrorMarker>> runSoot(final Map<C, Set<String>> analysisToEntryMethodSignatures, String staticSootArgs, String sootClassPath) {
		final Map<C,Set<ErrorMarker>> analysisConfigToResultingErrorMarkers = new HashMap<C, Set<ErrorMarker>>();
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.vulnanalysis", new Transformer<C>(analysisConfigToResultingErrorMarkers, analysisToEntryMethodSignatures)));	
		Set<String> classNames = extractClassNames(analysisToEntryMethodSignatures.values());					
		String[] args = (staticSootArgs+" -cp "+sootClassPath+" "+Joiner.on(" ").join(classNames)).split(" ");
		G.v().out = new PrintStream(new LoggingOutputStream(LOGGER, false), true);
		try {
			LOGGER.trace("SOOT ARGS:"+Joiner.on(" ").join(args));
			soot.Main.main(args);
		} catch(Throwable t) {
			LOGGER.error("Error executing Soot",t);
			G.reset();
		}
		return analysisConfigToResultingErrorMarkers;
	}

	
	private static Set<String> extractClassNames(Collection<Set<String>> values) {
		Set<String> res = new HashSet<String>();
		for(Set<String> methodSignatures: values) {
			for(String methodSig: methodSignatures) {
				res.add(methodSig.substring(1,methodSig.indexOf(":")));
			}
		}
		return res;
	}
}
