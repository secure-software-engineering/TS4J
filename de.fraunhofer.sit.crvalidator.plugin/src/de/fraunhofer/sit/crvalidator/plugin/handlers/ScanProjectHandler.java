package de.fraunhofer.sit.crvalidator.plugin.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.fraunhofer.sit.crvalidator.plugin.core.MarkerJob;

/**
 * Handle CRV ReScan project button (scans the project a user has selected a resource from)
 * 
 * @author triller
 *
 */
public class ScanProjectHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    if (window != null)
	    {
	    	ISelection sel = window.getSelectionService().getSelection();
	    	if(! (sel instanceof IStructuredSelection))
	    	{
	    		//nothing usable selected :)
	    		return null;
	    	}	
	    	
	        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
	        Object firstElement = selection.getFirstElement();
	        
	        IJavaProject project = null;
	        if(firstElement instanceof IJavaElement)
	        {
	        	IJavaElement ije = (IJavaElement) firstElement;
	        	project = ije.getJavaProject();
	        }
	        else if(firstElement instanceof Project)
	        {
	        	Project p = (Project) firstElement;
				try {
					if (p.hasNature(JavaCore.NATURE_ID)) {
					    project = JavaCore.create(p);
					}
				} catch (CoreException e) {
					System.out.println("Selected project is not a Java Project, please select one (or a resource of it)");
				}
	        }
	        else if(firstElement instanceof File)
	        {
	        	File f = (File) firstElement;
	        	IProject p = f.getProject();
	        	try {
					if (p.hasNature(JavaCore.NATURE_ID)) {
					    project = JavaCore.create(p);
					}
				} catch (CoreException e) {
					System.out.println("Selected file does not belong to a Java Project, please select one that does");
				}
	        }
	        
	        if(project != null)
	        {
	        	String name = project.getElementName();
	            System.out.println("Selected project: " + name);
	            
	            //trigger a fullscan of the project via a backgorund job
	            System.out.println("Start FULL job now... for project: " + name);
				MarkerJob jh = new MarkerJob("CRV Violation Marker Job: " + name);
				jh.setProject(project);
				jh.setFullScan(true);		
				jh.schedule();
	        }
	    }
		
		return null;
	}

}
