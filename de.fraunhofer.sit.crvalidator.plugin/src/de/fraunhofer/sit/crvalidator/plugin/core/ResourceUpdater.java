package de.fraunhofer.sit.crvalidator.plugin.core;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

public class ResourceUpdater implements IResourceDeltaVisitor {
	
	protected HashMap<IProject, LinkedList<IResource>> resourcesMap = new HashMap<IProject, LinkedList<IResource>>();
	protected HashMap<IProject, Boolean> fullScanMap = new HashMap<IProject, Boolean>();
	
	public HashMap<IProject, Boolean> getFullScanMap() {
		return fullScanMap;
	}

	public HashMap<IProject, LinkedList<IResource>> getResourcesMap() {
		return resourcesMap;
	}

	private IProject project = null;

	public IProject getProject() {
		return project;
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {

		IResource res = delta.getResource();
//		System.out.println("visit changetype:\t" + delta.getFlags() + " resource:\t" + res.getName());
		
		//if(!res.getFullPath().toString().endsWith("java") &&
		if(!res.getFullPath().toString().endsWith("class") &&
				! res.getFullPath().toString().contains("rules.txt"))
		{
			System.out.println("Ignore resource: (" + res.getFullPath().toString() +")");
			return true;
		}

		IProject curProject = res.getProject();
		
		try {
			if (curProject.hasNature(JavaCore.NATURE_ID)) {
			    
			    this.project = curProject;
			    
			    if(! resourcesMap.containsKey(curProject))			    	
			    	resourcesMap.put(curProject, new LinkedList<IResource>());
			    if(! fullScanMap.containsKey(curProject))
			    	fullScanMap.put(curProject, false);
			    
			    //System.out.println("\t Setting project to: " + this.project.getName());
			}
		} catch (CoreException e) {
			//e.printStackTrace();
			//resource is not part of a java project => ignore it
			return false;
		}

		int flags = delta.getFlags();
		//int mask = IResourceDelta.MOVED_TO | IResourceDelta.CONTENT;
		
		//do not react on both move events, only on MOVED_TO when operation has finished
		if((flags & IResourceDelta.MOVED_TO) != 0)
		{
			System.out.println("MOVED TO -> Fullscan (" + res.getFullPath().toString() + ")");
			fullScanMap.put(curProject, true);
			return false;
		}
		
		if(delta.getKind() == IResourceDelta.REMOVED)
		{
			System.out.println("REMOVED -> Nothing to scan (" + res.getFullPath().toString() + ")");
			
			if(res.getFullPath().toString().contains("rules.txt"))
				fullScanMap.put(curProject, true); //do full scan if rules.txt was deleted
		}
		
		if((delta.getKind() == IResourceDelta.ADDED))
		{
			System.out.println("ADDED -> Fullscan (" + res.getFullPath().toString() + ")");
			fullScanMap.put(curProject, true);
			return true;
		}
		
		if ( ((flags & IResourceDelta.CONTENT) !=0) && (delta.getKind() == IResourceDelta.CHANGED) )
		{
			if(res.getFullPath().toString().contains("rules.txt"))
			{
				System.out.println("Rules.txt CHANGED -> Fullscan");
				fullScanMap.put(curProject, true);
				return false;
			}
			else
			{
				System.out.println("CHANGED Content -> Deltascan (" + res.getFullPath().toString() + ")");
				
				LinkedList<IResource> il = resourcesMap.get(curProject);
				if(il != null)
				{
					il.add(res);
				}
			}
		}
		
		return true;
	}
}
