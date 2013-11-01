package de.fraunhofer.sit.codescan.androidssl.listeners;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.SearchEngine;

import de.fraunhofer.sit.codescan.androidssl.handlers.AnalyzeAllProjectsHandler;

/**
 * Attached to the eclipse framework to react on POST_CHANGE (build on a project has been finished) events
 * @author triller
 *
 */
public class AfterBuildListener implements IResourceChangeListener {

	private static AfterBuildListener instance = null;

	private AfterBuildListener()
	{		
	}

	public static AfterBuildListener getInstance()
	{
		if(instance == null) {
			instance = new AfterBuildListener();
		}
		return instance;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		try {
			final Set<IJavaElement> changedJavaElements = new HashSet<IJavaElement>();
			event.getDelta().accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					switch(delta.getKind()) {
					case IResourceDelta.ADDED:
					case IResourceDelta.CHANGED:
						IResource res = delta.getResource();
						IJavaElement javaElement = JavaCore.create(res);
						if(javaElement!=null) {
							if(res instanceof IProject) {
								if(!AnalyzeAllProjectsHandler.isAndroidProject((IProject) res)) {
									//don't care about non-Android projects
									return false;
								}
							}
							if(javaElement instanceof ICompilationUnit) {
								//only care if file contents changed
								if((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
									changedJavaElements.add(javaElement);
								}
								return false;
							}
						}
					}					
					return true;
				}
			});
			if(changedJavaElements.isEmpty()) return;
			
			IJavaElement[] changeArray = changedJavaElements.toArray(new IJavaElement[0]);
			AnalyzeAllProjectsHandler.searchAndAnalyze(SearchEngine.createJavaSearchScope(changeArray));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
