package de.fraunhofer.sit.codescan.framework.internal.analysis;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import soot.toolkits.scalar.Pair;
import de.fraunhofer.sit.codescan.framework.AnalysisConfiguration;
import de.fraunhofer.sit.codescan.framework.IAnalysisPack;
import de.fraunhofer.sit.codescan.framework.IFDSAnalysisConfiguration;
import de.fraunhofer.sit.codescan.framework.MethodBasedAnalysisConfiguration;
import de.fraunhofer.sit.codescan.framework.internal.Constants;
import de.fraunhofer.sit.codescan.framework.internal.Extensions;

/**
 * This class implements the main binding between Eclipse and Soot. Its method {@link #searchAndAnalyze(IJavaElement[])} searches
 * certain Java elements for relevant code, then passes that code to Soot for further analysis.
 */
public class AnalysisDispatcher {
	
	/**
	 * Searches the given javaElements for relevant code and then passes this code to the analysis.
	 * The method will also remove vulnerability markers for the given javaElements and add new markers
	 * where vulnerabilities are found.
	 */
	public static void searchAndAnalyze(final IJavaElement[] javaElements) {
		Job job = new Job("Vulnerability analysis") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IConfigurationElement[] extensions = Extensions.getContributorsToExtensionPoint();
				if(extensions.length==0) return Status.OK_STATUS;
				
				Map<AnalysisConfiguration, Set<IMethod>> analysisToRelevantMethods = searchRelevantClasses(javaElements,extensions);
				
				//re-map found methods
				Map<IJavaProject, Map<AnalysisConfiguration, Set<IMethod>>> projectToAnalysisAndMethods = reMapFindings(analysisToRelevantMethods);

				System.err.println();
				
				//				
//				//delete markers on the java elements
//				deleteMarkers(javaElements);
//		
//				//call the analysis
//				if(extensions.length==0) {
//					Display.getDefault().asyncExec(new Runnable() {
//				        public void run() {
//				        	final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();  					  
//				            MessageDialog.openWarning(shell,"No analyses found","There are no analysis plugins installed. Install a plugin to conduct analyses.");
//				        }
//				    });
//				} else {
//					for(Map.Entry<IJavaProject, Set<ITypeRoot>> entry: projectToFoundMethods.entrySet()) {
//						IJavaProject project = entry.getKey();
//						Set<ITypeRoot> topLevelTypesToAnalyze = entry.getValue();
//						Set<String> classesToAnalyze = typesToClassNames(topLevelTypesToAnalyze);
//						//perform analysis
//						G.reset();
////						registerMarkerCreator(project, classesToAnalyze, analysisPacks);
////						registerAnalysisPack(classesToAnalyze, analysisPacks);
//						String[] args = (SOOT_ARGS+" -cp "+getSootClasspath(project)+" "+Joiner.on(" ").join(classesToAnalyze)).split(" ");
//						soot.Main.main(args);
//					}
//				}
				return Status.OK_STATUS;
			}

			private Map<IJavaProject, Map<AnalysisConfiguration, Set<IMethod>>> reMapFindings(
					Map<AnalysisConfiguration, Set<IMethod>> analysisToRelevantMethods) {
				Map<IJavaProject,Map<AnalysisConfiguration, Set<IMethod>>> projectToAnalysisAndMethods = 
						new HashMap<IJavaProject,Map<AnalysisConfiguration, Set<IMethod>>>();
				
				for(Map.Entry<AnalysisConfiguration, Set<IMethod>> entry: analysisToRelevantMethods.entrySet()) {
					AnalysisConfiguration analysis = entry.getKey();
					Set<IMethod> methods = entry.getValue();
					for(IMethod m: methods) {
						IJavaProject project = m.getJavaProject();
						Map<AnalysisConfiguration, Set<IMethod>> analysisToMethods = projectToAnalysisAndMethods.get(project);
						if(analysisToMethods==null) {
							analysisToMethods = new HashMap<AnalysisConfiguration, Set<IMethod>>();
							projectToAnalysisAndMethods.put(project,analysisToMethods);
						}
						Set<IMethod> ms = analysisToMethods.get(analysis);
						if(ms==null) {
							ms = new HashSet<IMethod>();
							analysisToMethods.put(analysis,ms);
						}
						ms.add(m);
					}
				}
				return projectToAnalysisAndMethods;
			}

			private Map<AnalysisConfiguration,Set<IMethod>> searchRelevantClasses(final IJavaElement[] javaElements,
					IConfigurationElement[] extensions) {
				//initialize and execute the search for relevant methods
				SearchEngine searchEngine = new SearchEngine();
				Set<IAnalysisPack> analysisPacks = createAnalysisConfigurations(extensions);
				Map<AnalysisConfiguration,Set<IMethod>> result = new HashMap<AnalysisConfiguration, Set<IMethod>>();
				for (IAnalysisPack pack : analysisPacks) {
					IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(javaElements,IJavaSearchScope.SOURCES);
					Set<AnalysisConfiguration> allConfigs = new HashSet<AnalysisConfiguration>();
					allConfigs.addAll(Arrays.asList(pack.getIFDSAnalysisConfigs()));
					allConfigs.addAll(Arrays.asList(pack.getMethodBasedAnalysisConfigs()));
					for (AnalysisConfiguration config : allConfigs) {
						IConfigurationElement[] filters = config.getFilters();
						NextFilter:
						for (IConfigurationElement filter : filters) {
							IJavaElement[] filteredJavaElements = javaElements;
							IConfigurationElement[] superTypeFilters = filter.getChildren("bySuperType");
							for (IConfigurationElement superTypeFilter : superTypeFilters) {
								String superClassName = superTypeFilter.getAttribute("superType");
								final Set<IType> subTypesFound = new HashSet<IType>();
								if(superClassName!=null && !superClassName.isEmpty()) {
									SearchPattern pattern = SearchPattern.createPattern(superClassName, IJavaSearchConstants.CLASS, IJavaSearchConstants.IMPLEMENTORS, SearchPattern.R_EXACT_MATCH);
									SearchRequestor requestor = new SearchRequestor() {
										public void acceptSearchMatch(SearchMatch match) throws CoreException {
											IType found = (IType) match.getElement();
											subTypesFound.add(found);
										}
									};
									try {
										searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, searchScope, requestor, null);
									} catch (CoreException e) {
										e.printStackTrace();
									}
									filteredJavaElements = subTypesFound.toArray(new IJavaElement[subTypesFound.size()]);
									searchScope = SearchEngine.createJavaSearchScope(filteredJavaElements,IJavaSearchScope.SOURCES);
								}

								IConfigurationElement[] methodDeclFilters = superTypeFilter.getChildren("byMethodDecl");
								if(methodDeclFilters.length==0) {
									//no further filters; add all non-abstract, non-native methods in all found classes
									Set<IMethod> methodsFound = getOrCreate(config, result);
									for(IType t: subTypesFound) {
										try {
											for(IMethod m: t.getMethods()) {
												if(!Flags.isAbstract(m.getFlags()) && !Flags.isNative(m.getFlags())) {
													methodsFound.add(m);
												}
											}
										} catch (JavaModelException e) {
											e.printStackTrace();
										}
									}
								} 
								for (IConfigurationElement methodDeclFilter: methodDeclFilters) {
									String declSubSignature = methodDeclFilter.getAttribute("subsignature");
									final Set<IMethod> declsFound = new HashSet<IMethod>();
									if(declSubSignature!=null && !declSubSignature.isEmpty()) {
										declSubSignature = toEclipseSearchPattern(declSubSignature);
										SearchPattern pattern = SearchPattern.createPattern(declSubSignature, IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_ERASURE_MATCH);
										SearchRequestor requestor = new SearchRequestor() {
											public void acceptSearchMatch(SearchMatch match) throws CoreException {
												IMethod found = (IMethod) match.getElement();
												declsFound.add(found);
											}
										};
										try {
											searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, searchScope, requestor, null);
										} catch (CoreException e) {
											e.printStackTrace();
										}
										filteredJavaElements = declsFound.toArray(new IJavaElement[declsFound.size()]);
										searchScope = SearchEngine.createJavaSearchScope(filteredJavaElements,IJavaSearchScope.SOURCES);
									}
									
									IConfigurationElement[] methodCallFilters = methodDeclFilter.getChildren("byMethodCall");
									if(methodCallFilters.length==0) {
										//matched classes not filtered further
										Set<IMethod> methodsFound = getOrCreate(config, result);
										methodsFound.addAll(declsFound);
									}	
									
									for (IConfigurationElement methodCallFilter: methodCallFilters) {
										String callSubSignature = methodCallFilter.getAttribute("subsignature");
										if(callSubSignature!=null && !callSubSignature.isEmpty()) {
											callSubSignature = toEclipseSearchPattern(callSubSignature);
											SearchPattern pattern = SearchPattern.createPattern(callSubSignature, IJavaSearchConstants.METHOD, IJavaSearchConstants.REFERENCES, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_ERASURE_MATCH);
											final Set<IMethod> containersFound = new HashSet<IMethod>();
											SearchRequestor requestor = new SearchRequestor() {
												public void acceptSearchMatch(SearchMatch match) throws CoreException {
													IMethod found = (IMethod) match.getElement();
													containersFound.add(found);
												}
											};
											try {
												searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, searchScope, requestor, null);
											} catch (CoreException e) {
												e.printStackTrace();
											}
											Set<IMethod> methodsFound = getOrCreate(config, result);
											methodsFound.addAll(containersFound);
										}
									}
								}
							}
						}
					}
				}
				return result;
			}

			private Set<IMethod> getOrCreate(AnalysisConfiguration config,Map<AnalysisConfiguration, Set<IMethod>> result) {
				Set<IMethod> methodsFound = result.get(config);
				if(methodsFound==null) {
					methodsFound = new HashSet<IMethod>();
					result.put(config, methodsFound);
				}
				return methodsFound;
			}

			private String toEclipseSearchPattern(String subSignature) {
				String returnType = subSignature.substring(0,subSignature.indexOf(" "));
				String nameAndArguments = subSignature.substring(subSignature.indexOf(" ")+1);
				return nameAndArguments + " " + returnType;
			}
		};
		job.schedule();
	}

	/**
	 * Returns true if the given project is an Android project.
	 */
	public static boolean isAndroidProject(IProject p) {
		try {				
			IProjectDescription description = p.getDescription();
			String[] natures = description.getNatureIds();
			boolean found = false;
			for (String nature : natures) {
				if(nature.equals(Constants.ANDROID_NATURE_ID)) {
					found = true;
				}
			}
			return found;
		} catch (CoreException e) {
			//ignore project
			return false;
		}
	}
	
	private static void deleteMarkers(IJavaElement[] javaElements) {
		for (final IJavaElement je : javaElements) {
			try {
				je.getResource().deleteMarkers(Constants.MARKER_TYPE, false, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private static void registerMarkerCreator(final IJavaProject project, final Set<String> classesToStartAnalysisAt, final IAnalysisPack[] configs) {
		//register marker creation
//		PackManager.v().getPack("wjap").add(new Transform("wjap.errorreporter", new SceneTransformer() {			
//			protected void internalTransform(String arg0, Map<String, String> arg1) {
//				for(IAnalysisConfiguration config : configs) {
//					for (String appClass : classesToStartAnalysisAt) {
//						SootClass c = Scene.v().getSootClass(appClass);
//						Set<SootMethod> methodsToConsider = new HashSet<SootMethod>();
//						String subSig = config.getMethodSubSignature();
//						if(subSig!=null && !subSig.isEmpty()) {
//							methodsToConsider.add(c.getMethod(subSig));
//						} else {
//							methodsToConsider.addAll(c.getMethods());
//						}							
//						for (SootMethod m : methodsToConsider) {
//							if(m.hasTag(VulnerableMethodTag.class.getName())) {
//								VulnerableMethodTag tag = (VulnerableMethodTag) m.getTag(VulnerableMethodTag.class.getName());
//								if(tag.getAnalysisConfig().equals(config)) {
//									try {
//										IType erronousClass = project.findType(c.getName());
//										IResource erroneousFile = erronousClass.getCompilationUnit().getResource();
//										IMarker marker = erroneousFile.createMarker(MARKER_TYPE);
//										marker.setAttribute(IMarker.SEVERITY,IMarker.SEVERITY_ERROR);
//										marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
//										marker.setAttribute(IMarker.LINE_NUMBER, m.getJavaSourceStartLineNumber());
//										marker.setAttribute(IMarker.USER_EDITABLE, false);
//										marker.setAttribute(IMarker.MESSAGE, config.getErrorMessage());
//									} catch (JavaModelException e) {
//										e.printStackTrace();
//									} catch (CoreException e) {
//										e.printStackTrace();
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}));		
	}
	
	private static Set<String> typesToClassNames(Set<ITypeRoot> topLevelTypesToAnalyze) {
		Set<String> applicationClasses = new HashSet<String>();
		for (ITypeRoot typeRoot : topLevelTypesToAnalyze) {
			IType type = typeRoot.findPrimaryType();
			String qualifiedName = type.getFullyQualifiedName();
			applicationClasses.add(qualifiedName);
		}
		return applicationClasses;
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

	private static Set<IAnalysisPack> createAnalysisConfigurations(IConfigurationElement[] packs) {
		Set<IAnalysisPack> configs = new HashSet<IAnalysisPack>();
		for (final IConfigurationElement pack : packs) {
			configs.add(new IAnalysisPack() {
				public IFDSAnalysisConfiguration[] getIFDSAnalysisConfigs() {
					IConfigurationElement[] analyses = pack.getChildren("ifdsAnalysis");
					IFDSAnalysisConfiguration[] res = new IFDSAnalysisConfiguration[analyses.length];
					int i=0;
					for (IConfigurationElement analysisInfo : analyses) {
						res[i++] = new IFDSAnalysisConfiguration(analysisInfo);
					}
					return res;
				}
				public MethodBasedAnalysisConfiguration[] getMethodBasedAnalysisConfigs() {
					IConfigurationElement[] analyses = pack.getChildren("methodBasedAnalysis");
					MethodBasedAnalysisConfiguration[] res = new MethodBasedAnalysisConfiguration[analyses.length];
					int i=0;
					for (IConfigurationElement analysisInfo : analyses) {
						res[i++] = new MethodBasedAnalysisConfiguration(analysisInfo);
					}
					return res;
				}
			});
		}
		return configs;
	}

}
