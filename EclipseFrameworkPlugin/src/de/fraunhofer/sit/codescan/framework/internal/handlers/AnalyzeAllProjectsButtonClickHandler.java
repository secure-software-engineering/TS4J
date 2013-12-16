package de.fraunhofer.sit.codescan.framework.internal.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;



import de.fraunhofer.sit.codescan.framework.internal.analysis.AnalysisDispatcher;

/**
 * A handler which is called when the button to analyze all projects is clicked.
 */
public class AnalyzeAllProjectsButtonClickHandler extends AbstractHandler {
	
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ArrayList<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
		for (IProject p : projects) {
				IJavaProject javaProject = JavaCore.create(p);
				javaProjects.add(javaProject);
		}
		IJavaProject[] javaProjectArray = javaProjects.toArray(new IJavaProject[0]);		
		
		AnalysisDispatcher.searchAndAnalyze(javaProjectArray);

		return null;
	}

	
}
