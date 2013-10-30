package de.fraunhofer.sit.codescan.androidssl.handlers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.google.common.base.Joiner;

import de.ecspride.sslanalysis.Main;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AnalyzeAllProjectsHandler extends AbstractHandler {
	
	public static final String ANDROID_NATURE_ID = "com.android.ide.eclipse.adt.AndroidNature";  
	
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
		
		for (IProject p : projects) {
			if(isAndroidProject(p)) {
				IJavaProject javaProject = JavaCore.create(p);
				Collection<IPath> outDirs = getOutputDirectories(javaProject);
				if(outDirs.isEmpty()) continue;
				String sootClasspath = getSootClasspath(javaProject);
				callAnalysis(outDirs,sootClasspath);
			}
		}
		
		
		return null;
	}

	private void callAnalysis(Collection<IPath> outDirs, String sootClasspath) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		Set<String> processDirs = new HashSet<String>();
		for (IPath outDir : outDirs) {
			processDirs.add("-process-dir "+root.findMember(outDir).getLocation().toString());
		}
		String[] args = ("-f none -p cg all-reachable:true -no-bodies-for-excluded -w -pp -cp "+sootClasspath+" "+Joiner.on(" ").join(processDirs)).split(" ");
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
