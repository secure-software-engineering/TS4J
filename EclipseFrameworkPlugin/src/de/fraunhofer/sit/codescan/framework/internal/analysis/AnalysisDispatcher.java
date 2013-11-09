package de.fraunhofer.sit.codescan.framework.internal.analysis;


import static de.fraunhofer.sit.codescan.framework.SootBridge.registerAnalysisPack;
import static de.fraunhofer.sit.codescan.framework.internal.Constants.MARKER_TYPE;
import static de.fraunhofer.sit.codescan.framework.internal.Constants.SOOT_ARGS;
import static de.fraunhofer.sit.codescan.framework.internal.Extensions.getContributorsToExtensionPoint;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
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

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;

import com.google.common.base.Joiner;

import de.fraunhofer.sit.codescan.framework.AnalysisConfiguration;
import de.fraunhofer.sit.codescan.framework.AnalysisPlugin;
import de.fraunhofer.sit.codescan.framework.VulnerableMethodTag;
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
				//initialize and execute the search for relevant methods
				IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(javaElements,IJavaSearchScope.SOURCES);
				SearchEngine searchEngine = new SearchEngine();
				SearchPattern pattern = SearchPattern.createPattern("onReceivedSslError", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_ERASURE_MATCH);
				final Set<IMethod> callBacksFound = new HashSet<IMethod>();
				SearchRequestor requestor = new SearchRequestor() {
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						IMethod found = (IMethod) match.getElement();
						callBacksFound.add(found);
					}
				};
				try {
					searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, searchScope, requestor, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}

				//re-map found methods
				Map<IJavaProject,Set<ITypeRoot>> projectToFoundMethods = new HashMap<IJavaProject, Set<ITypeRoot>>();
				for(IMethod m : callBacksFound) {
					IJavaProject javaProject = m.getJavaProject();
					ITypeRoot topLevelType = m.getTypeRoot();
					Set<ITypeRoot> topLevelTypesToAnalyze = projectToFoundMethods.get(javaProject);
					if(topLevelTypesToAnalyze==null) topLevelTypesToAnalyze = new HashSet<ITypeRoot>();
					topLevelTypesToAnalyze.add(topLevelType);
					projectToFoundMethods.put(javaProject, topLevelTypesToAnalyze);
				}
				
				//delete markers on the java elements
				deleteMarkers(javaElements);
		
				//call the analysis
				for(Map.Entry<IJavaProject, Set<ITypeRoot>> entry: projectToFoundMethods.entrySet()) {
					IJavaProject project = entry.getKey();
					Set<ITypeRoot> topLevelTypesToAnalyze = entry.getValue();
					Set<String> classesToAnalyze = typesToClassNames(topLevelTypesToAnalyze);
					//perform analysis
					G.reset();
					registerMarkerCreator(project, classesToAnalyze);
					IConfigurationElement[] extensions = Extensions.getContributorsToExtensionPoint();
					AnalysisConfiguration[] configs = createAnalysisConfigurations(extensions);
					registerAnalysisPack(classesToAnalyze, configs);
					String[] args = (SOOT_ARGS+" -cp "+getSootClasspath(project)+" "+Joiner.on(" ").join(classesToAnalyze)).split(" ");
					soot.Main.main(args);
				}
				return Status.OK_STATUS;
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

	private static void registerMarkerCreator(final IJavaProject project, final Set<String> classesToStartAnalysisAt) {
		//register marker creation
		PackManager.v().getPack("wjap").add(new Transform("wjap.errorreporter", new SceneTransformer() {			
			protected void internalTransform(String arg0, Map<String, String> arg1) {
				for(final IConfigurationElement extension : getContributorsToExtensionPoint()) {
					for (String appClass : classesToStartAnalysisAt) {
						SootClass c = Scene.v().getSootClass(appClass);
						SootMethod m = c.getMethod(extension.getAttribute("subsignature"));
						if(m.hasTag(VulnerableMethodTag.class.getName())) {
							try {
								IType erronousClass = project.findType(c.getName());
								IResource erroneousFile = erronousClass.getCompilationUnit().getResource();
								IMarker marker = erroneousFile.createMarker(MARKER_TYPE);
								marker.setAttribute(IMarker.SEVERITY,IMarker.SEVERITY_ERROR);
								marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
								marker.setAttribute(IMarker.LINE_NUMBER, m.getJavaSourceStartLineNumber());
								marker.setAttribute(IMarker.USER_EDITABLE, false);
								marker.setAttribute(IMarker.MESSAGE, extension.getAttribute("errormessage"));
							} catch (JavaModelException e) {
								e.printStackTrace();
							} catch (CoreException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}));		
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

	private static AnalysisConfiguration[] createAnalysisConfigurations(IConfigurationElement[] extensions) {
		AnalysisConfiguration[] configs = new AnalysisConfiguration[extensions.length];
		int i=0;
		for (final IConfigurationElement extension : extensions) {
			configs[i++] = new AnalysisConfiguration() {
				public String getSuperClassName() {
					return extension.getAttribute("superclass");
				}
				public String getMethodSubSignature() {
					return extension.getAttribute("subsignature");
				}
				public AnalysisPlugin getAnalysisPlugin() {
					return Extensions.createPluginObject(extension);
				}
			};
		}
		return configs;
	}

}