package de.fraunhofer.sit.codescan.framework;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import com.google.common.base.Joiner;

import soot.Body;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import de.fraunhofer.sit.codescan.framework.internal.Constants;
import de.fraunhofer.sit.codescan.framework.internal.analysis.MustAlias;

/**
 * Registers an analysis pack with Soot, which can then be executed by calling {@link soot.Main#main(String[])}.
 * This is currently used internally within the plugin but also by the test harness.
 */
public class SootBridge {

	private static final Set<String> PRIMITIVE_TYPE_NAMES;
	
	static {
		PRIMITIVE_TYPE_NAMES = new HashSet<String>();
		PRIMITIVE_TYPE_NAMES.add("void");
		PRIMITIVE_TYPE_NAMES.add("byte");
		PRIMITIVE_TYPE_NAMES.add("int");
		PRIMITIVE_TYPE_NAMES.add("boolean");
		PRIMITIVE_TYPE_NAMES.add("long");
		PRIMITIVE_TYPE_NAMES.add("short");
		PRIMITIVE_TYPE_NAMES.add("float");
		PRIMITIVE_TYPE_NAMES.add("double");
	}

	public static void registerAnalysisPack(IJavaProject project, final Map<AnalysisConfiguration, Set<IMethod>> analysisToRelevantMethods) {
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
	
				for(Map.Entry<AnalysisConfiguration, Set<IMethod>> analysisAndMethods: analysisToRelevantMethods.entrySet()) {
					for(final IMethod method: analysisAndMethods.getValue()) {
						String sootMethodSignature = getSootMethodSignature(method);
						if(sootMethodSignature==null) {
							//TODO log error
							continue;
						}
						final SootMethod m = Scene.v().getMethod(sootMethodSignature);
						if(!m.hasActiveBody()) continue;

						final AnalysisConfiguration analysisConfig = analysisAndMethods.getKey();
						
						analysisConfig.registerAnalysis(new IAnalysisContext() {
							
							public MustAlias getMustAliasManager() {
								return mustAliasManager;
							}
							
							public SootMethod getSootMethod() {
								return m;
							}
							
							public JimpleBasedInterproceduralCFG getICFG() {
								return icfg;
							}
							
							public AnalysisConfiguration getAnalysisConfiguration() {
								return analysisConfig;
							}

							public IMethod getMethod() {
								return method;
							}
						});
						
					}
				}
			}				
		}));	
		Set<String> classNames = extractClassNames(analysisToRelevantMethods.values());					
		String[] args = (Constants.SOOT_ARGS+" -cp "+getSootClasspath(project)+" "+Joiner.on(" ").join(classNames)).split(" ");
		G.v().out = new PrintStream(new OutputStream() {
			public void write(int b) throws IOException {
				//swallow output
			}
		});
		try {
			soot.Main.main(args);
		} catch(RuntimeException e) {
			e.printStackTrace();
			G.reset();
		}
	}
	
	private static Set<String> extractClassNames(Collection<Set<IMethod>> values) {
		Set<String> res = new HashSet<String>();
		for(Set<IMethod> methods: values) {
			for(IMethod m: methods) {
				res.add(m.getDeclaringType().getFullyQualifiedName());
			}
		}
		return res;
	}

	
	private static String getSootMethodSignature(IMethod iMethod)
	{
		try {
	        StringBuilder name = new StringBuilder();
	        name.append("<");
	        name.append(iMethod.getDeclaringType().getFullyQualifiedName());
	        name.append(": ");
	        String retTypeName = resolveName(iMethod, iMethod.getReturnType());
	        if(retTypeName==null) return null;
	        name.append(retTypeName);
	        name.append(" ");
	        name.append(iMethod.getElementName());
	        name.append("(");

	        String comma = "";
			String[] parameterTypes = iMethod.getParameterTypes();
				for (int i=0; i<iMethod.getParameterTypes().length; ++i) {
					name.append(comma);
					String readableName = resolveName(iMethod, parameterTypes[i]);
					if(readableName==null) return null;
					name.append(readableName);
	                comma = ",";
				}

	        name.append(")");
	        name.append(">");
	        
	        //workaround for this bug in Eclipse:
	        //https://bugs.eclipse.org/bugs/show_bug.cgi?id=423358
	        //ignore inner classes for now
	        
	        if(name.toString().contains("$")) return null;

	        return name.toString();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}

	private static String resolveName(IMethod iMethod, String simpleName) throws JavaModelException {
		String readableName = Signature.toString(simpleName);
		if(!PRIMITIVE_TYPE_NAMES.contains(readableName)) {
			String[][] fqTypes = iMethod.getDeclaringType().resolveType(readableName);
			if(fqTypes.length!=1) {
				return null;
			}
			String pkg = fqTypes[0][0];
			String className = fqTypes[0][1];
			readableName = pkg+"."+className;
		}
		return readableName;
	}
	
	private static URL[] projectClassPath(IJavaProject javaProject) {
	    IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    IClasspathEntry[] cp;
	    try {
	            cp = javaProject.getResolvedClasspath(true);
	            List<URL> urls = new ArrayList<URL>();
	            String uriString = workspace.getRoot().getFile(
	                            javaProject.getOutputLocation()).getLocationURI().toString()
	                            + "/";
	            urls.add(new URI(uriString).toURL());
	            for (IClasspathEntry entry : cp) {
	                    File file = entry.getPath().toFile();
	                    URL url = file.toURI().toURL();
	                    urls.add(url);
	            }
	            URL[] array = new URL[urls.size()];
	            urls.toArray(array);
	            return array;
	    } catch (JavaModelException e) {
	            e.printStackTrace();
	            return new URL[0];
	    } catch (MalformedURLException e) {
	            e.printStackTrace();
	            return new URL[0];
	    } catch (URISyntaxException e) {
	            e.printStackTrace();
	            return new URL[0];
	    }
	}

	private static String getSootClasspath(IJavaProject javaProject) {
	    return urlsToString(projectClassPath(javaProject));
	}

	private static String urlsToString(URL[] urls) {
	    StringBuffer cp = new StringBuffer();
	    for (URL url : urls) {
	            cp.append(url.getPath());
	            cp.append(File.pathSeparator);
	    }
	    
	    return cp.toString();
	}

}
