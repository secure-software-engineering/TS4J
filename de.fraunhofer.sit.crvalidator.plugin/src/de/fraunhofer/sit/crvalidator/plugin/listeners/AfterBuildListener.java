package de.fraunhofer.sit.crvalidator.plugin.listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import de.fraunhofer.sit.crvalidator.plugin.PluginStateManager;
import de.fraunhofer.sit.crvalidator.plugin.core.MarkerJob;
import de.fraunhofer.sit.crvalidator.plugin.core.ResourceUpdater;

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

	/**
	 * Singleton so the Activator class can obtain and remove the listener registered by early startup
	 * @return active or new AfterBuildListener
	 */

	public static AfterBuildListener getInstance()
	{
		if(instance == null)
		{
			return new AfterBuildListener();
		}
		else
		{
			return instance;
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		//IResource res = event.getResource();
		switch (event.getType()) {
		/*
           case IResourceChangeEvent.PRE_CLOSE:
              System.out.print("Project ");
              System.out.print(res.getFullPath());
              System.out.println(" is about to close.");
              break;
           case IResourceChangeEvent.PRE_DELETE:
              System.out.print("Project ");
              System.out.print(res.getFullPath());
              System.out.println(" is about to be deleted.");
              break;
		 */
		case IResourceChangeEvent.POST_CHANGE:
			System.out.println("Resources have changed.");              
			//create job list and let it run afterwards
			try {
				//ResourceUpdater will run trough all current changes  in a visitor pattern
				ResourceUpdater ruh = new ResourceUpdater();
				System.out.println("xxxxx Before visitor");
				event.getDelta().accept(ruh);
				System.out.println("xxxxx After visitor");

				//fullScanMap holds mapping from project to its status, whether it has to be fully scanned or not
				HashMap<IProject, Boolean> fullScanMap = ruh.getFullScanMap();
				//resourcesMap contains all changed resources mapped to their projects
				HashMap<IProject, LinkedList<IResource>> resourcesMap = ruh.getResourcesMap();
								
				Iterator<Entry<IProject, LinkedList<IResource>>> it = resourcesMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry)it.next();
					//System.out.println(pairs.getKey() + " = " + pairs.getValue());

					IProject project = (IProject) pairs.getKey();
					LinkedList<IResource> changedResources = (LinkedList<IResource>) pairs.getValue();

					boolean fullScan = fullScanMap.get(project);
					if(!fullScan)
					{
						//check whether we have not scanned the project yet
						if(PluginStateManager.getProjectFullScanStatus(project.getName()))
						{
							fullScan = true;
							PluginStateManager.setProjectFullScanStatus(project.getName(), false);
						}
					}
					else
					{
						//set fullscan status of this project to false (i.e. not needed anymore) because we do a fullscan now
						PluginStateManager.setProjectFullScanStatus(project.getName(), false);
					}

					if(fullScan || (! changedResources.isEmpty()))
					{
						System.out.println("Start job now... for project: " + project.getName());

						IJavaProject p = null;
						if (project.hasNature(JavaCore.NATURE_ID)) {
							p = JavaCore.create(project);
						}

						//schedule a background job to create markers for the resources
						//it has to be a background job, because we are currently traversing the project resources tree and will therefore catch a concurrent modification exception with this backgound job
						MarkerJob jh = new MarkerJob("CRV Violation Marker Job: " + project.getName());
						jh.setProject(p);
						jh.setFullScan(fullScan);
						jh.setResources(changedResources);
						jh.schedule();
					}

				}

			} catch (CoreException e) {
				System.err.println("Cannot handle change after POST_CHANGE event:");
				e.printStackTrace();
			}
			break;
			/*
           case IResourceChangeEvent.PRE_BUILD:
              System.out.println("Build about to run.");
              try {
				event.getDelta().accept(new DeltaPrinter());
			} catch (CoreException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
              break;
           case IResourceChangeEvent.POST_BUILD:
              System.out.println("Build complete.");
              try {
				event.getDelta().accept(new DeltaPrinter());
			} catch (CoreException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
              break;
			 */
		}

	}

}
