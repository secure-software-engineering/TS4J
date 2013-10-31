package de.fraunhofer.sit.codescan.androidssl.handlers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import com.google.common.base.Joiner;

import de.ecspride.sslanalysis.Main;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AnalyzeAllProjectsHandler extends AbstractHandler {
	
	public static final String ANDROID_NATURE_ID = "com.android.ide.eclipse.adt.AndroidNature";
	
	protected Set<IMethod> callBacksFound = new HashSet<IMethod>();
	
	/**
	 * The constructor.
	 */
	public AnalyzeAllProjectsHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		ArrayList<IJavaProject> androidProjects = new ArrayList<IJavaProject>();
		for (IProject p : projects) {
			if(isAndroidProject(p)) {
				IJavaProject javaProject = JavaCore.create(p);
				androidProjects.add(javaProject);
			}
		}
		IJavaProject[] androidProjectArray = androidProjects.toArray(new IJavaProject[0]);
		
		SearchEngine searchEngine = new SearchEngine();
		IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(androidProjectArray);
		
		SearchPattern pattern = SearchPattern.createPattern("onReceivedSslError", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_ERASURE_MATCH);
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
		
		Map<IJavaProject,Set<ITypeRoot>> projectToFoundMethods = new HashMap<IJavaProject, Set<ITypeRoot>>();
		for(IMethod m : callBacksFound) {
			IJavaProject javaProject = m.getJavaProject();
			ITypeRoot topLevelType = m.getTypeRoot();
			Set<ITypeRoot> topLevelTypesToAnalyze = projectToFoundMethods.get(javaProject);
			if(topLevelTypesToAnalyze==null) topLevelTypesToAnalyze = new HashSet<ITypeRoot>();
			topLevelTypesToAnalyze.add(topLevelType);
			projectToFoundMethods.put(javaProject, topLevelTypesToAnalyze);
		}
		
		for(Map.Entry<IJavaProject, Set<ITypeRoot>> entry: projectToFoundMethods.entrySet()) {
			IJavaProject project = entry.getKey();
			Set<ITypeRoot> topLevelTypesToAnalyze = entry.getValue();
			callAnalysis(topLevelTypesToAnalyze, getSootClasspath(project));
		}
		
		return null;
	}

	private void callAnalysis(Set<ITypeRoot> topLevelTypesToAnalyze, String sootClasspath) {
		Set<String> applicationClasses = new HashSet<String>();
		for (ITypeRoot typeRoot : topLevelTypesToAnalyze) {
			IType type = typeRoot.findPrimaryType();
			String qualifiedName = type.getFullyQualifiedName();
			applicationClasses.add(qualifiedName);
		}
		String[] args = ("-f none -p cg all-reachable:true -no-bodies-for-excluded -w -pp -cp "+sootClasspath+" "+Joiner.on(" ").join(applicationClasses)).split(" ");
		System.err.println(Joiner.on(" ").join(args));
		Main.main(args);
	}

	private Collection<IPath> getOutputDirectories(IJavaProject javaProject) {
		Collection<IPath> binDirs = new HashSet<IPath>();
		try {
			IPath defaultBinDir = javaProject.getOutputLocation();
			binDirs.add(defaultBinDir);
			for (IClasspathEntry classPathEntry : javaProject.getResolvedClasspath(true)) {
				IPath binDir = classPathEntry.getOutputLocation();
				if(binDir!=null) binDirs.add(binDir);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return binDirs;
	}

	private boolean isAndroidProject(IProject p) {
		try {				
			IProjectDescription description = p.getDescription();
			String[] natures = description.getNatureIds();
			boolean found = false;
			for (String nature : natures) {
				if(nature.equals("com.android.ide.eclipse.adt.AndroidNature")) {
					found = true;
				}
			}
			return found;
		} catch (CoreException e) {
			//ignore project
			return false;
		}
	}
	
    public static URL[] projectClassPath(IJavaProject javaProject) {
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

    public String getSootClasspath(IJavaProject javaProject) {
        return urlsToString(projectClassPath(javaProject));
    }

    public static String urlsToString(URL[] urls) {
        StringBuffer cp = new StringBuffer();
        for (URL url : urls) {
                cp.append(url.getPath());
                cp.append(File.pathSeparator);
        }
        
        return cp.toString();
    }

	
}
