package de.fraunhofer.sit.codescan.androidssl.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;



import de.ecspride.sslanalysis.AnalysisDispatcher;

/**
 * A handler which is called when the button to analyze all projects is clicked.
 */
public class AnalyzeAllProjectsHandler extends AbstractHandler {
	
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		ArrayList<IJavaProject> androidProjects = new ArrayList<IJavaProject>();
		for (IProject p : projects) {
			if(AnalysisDispatcher.isAndroidProject(p)) {
				IJavaProject javaProject = JavaCore.create(p);
				androidProjects.add(javaProject);
			}
		}
		IJavaProject[] androidProjectArray = androidProjects.toArray(new IJavaProject[0]);		
		
		AnalysisDispatcher.searchAndAnalyze(androidProjectArray);

		return null;
	}

	
}
