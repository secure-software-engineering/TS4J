package de.fraunhofer.sit.crvalidator.plugin.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import de.fraunhofer.sit.crvalidator.exception.NoRulesFoundException;
import de.fraunhofer.sit.crvalidator.exception.UnknownRuleException;
import de.fraunhofer.sit.crvalidator.plugin.PluginStateManager;
import de.fraunhofer.sit.crvalidator.relation.Relation;
import de.fraunhofer.sit.crvalidator.rule.Rule;
import de.fraunhofer.sit.crvalidator.util.Util;
import de.fraunhofer.sit.crvalidator.validator.RuleValidator;
import de.fraunhofer.sit.crvalidator.validator.Violation;

/**
 * Background job that attaches custom violation and exception marker to eclipse project resources
 * @author triller
 *
 */
public class MarkerJob extends WorkspaceJob {

	private IJavaProject project = null;
	private Boolean fullScan = false;
	private LinkedList<IResource> resources = new LinkedList<IResource>();	
	
	/**
	 * Set whether the project should be fully scanned
	 * @param fullScan: true if the project should be fully scanned
	 */
	public void setFullScan(Boolean fullScan) {
		this.fullScan = fullScan;
	}	
	
	/**
	 * Set the changed resources for the project
	 * @param resources: resources that have been changed in the project
	 */
	public void setResources(LinkedList<IResource> resources) {
		this.resources = resources;
	}

	/**
	 * Set the project to be processed
	 * @param project: project to be processed
	 */
	public void setProject(IJavaProject project) {
		this.project = project;
	}

	public MarkerJob(String name)
	{
		super(name);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
//		System.out.println("run in workspace");
		
		//if we do not have a project set => do nothing
		if(project != null)
		{
//			collect src folder
//			IPath srcPath = ResourceUtils.getJavaSourceLocation(project);
			
//			ArrayList<String> srcPaths = new ArrayList<String>();
//			try {
//	            IPackageFragmentRoot[] packageFragmentRoot = project.getAllPackageFragmentRoots();
//	            for (int i = 0; i < packageFragmentRoot.length; i++){
//	                if (packageFragmentRoot[i].getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT && !packageFragmentRoot[i].isArchive())
//	                {
//	                	//ret.add(packageFragmentRoot[i]);
//	                	srcPaths.add(packageFragmentRoot[i].getPath().toString());
//	                }
//	            }
//	        } catch (JavaModelException e) {
//	            e.printStackTrace();
//	            return null;
//	        }			
//			String srcPath = srcPaths.get(0);
//			System.out.println("Project SourcePath: " + workspacePath + srcPath);
			
			//remove exception markers from project
			//TODO: CorrespondingResource vs UnderlyingResource ?
			IResource pres = project.getCorrespondingResource();
			pres.deleteMarkers(MarkerBuilder.EXCEPTIONMARKER, false, IResource.DEPTH_INFINITE);
			
			
			IResource projectResource = project.getUnderlyingResource();				
			String projectFileSystemPath = projectResource.getLocation().toString();
			
			//obtain the path to the project on the local filesystem
			String binDir = project.getOutputLocation().toString();
			int len = project.getElementName().length();
			binDir = binDir.substring(len+1);
			
			//obtain the binary directory of the project on the local filesystem (can be out of the workspace, in git for example)
			String projectBinDir = projectFileSystemPath + binDir;
//			System.out.println("bindir: " + projectBinDir);
			
			ArrayList<Violation> vios = new ArrayList<Violation>();
			
			try {
				//TODO: maybe handle more than one output folder?
				long start = System.currentTimeMillis();
				
				//check if rules.txt exists in project, if not, clear violation markers on project
				File rulesFile = new File(projectFileSystemPath + "/rules.txt");
				if(!rulesFile.exists())
				{
					MarkerBuilder.createExceptionMarker(project.getProject(), new FileNotFoundException("rules.txt not found in project"), false);
					pres.deleteMarkers(MarkerBuilder.VIOLATIONMARKER, false, IResource.DEPTH_INFINITE);
					
					return Status.OK_STATUS;
				}
				
				RuleValidator rv = PluginStateManager.ruleValidator;
				
				if(fullScan)
				{
					//call the core library
					vios = rv.getViolationsSync(projectFileSystemPath, projectBinDir);
				}
				else
				{
					ArrayList<String> files = new ArrayList<>();
					//collect paths to the changed files on the filesystem to pass it to the core library
					for(IResource res : resources)
					{
						String fileName = res.getLocation().toString();
//						System.out.println("filename: " + fileName);
						files.add(fileName);
					}
					//call the core library
					vios = rv.getViolationsSync(projectFileSystemPath, projectBinDir, files);					
				}
				
				long stop = System.currentTimeMillis();				
				System.out.println("Millis Soot: " + (stop-start));
				
				
			} catch (UnknownRuleException ure) {				
				//find rules.txt
				IResource res = project.getProject().findMember("rules.txt");							
				MarkerBuilder.createExceptionMarker(res, ure, false);
				
				return Status.OK_STATUS;
				
			} catch (NoRulesFoundException nrfe) {
				MarkerBuilder.createExceptionMarker(pres, nrfe, false);			
				return Status.OK_STATUS;
			} catch (Exception e)
			{
				//for debugging purposes we print the stacktrace of unknown exceptions
				e.printStackTrace();
				
				MarkerBuilder.createExceptionMarker(project.getProject(), e, true);			
				return Status.OK_STATUS;
			}
			
			long start = System.currentTimeMillis();
			
			if(fullScan) //delete ALL project markers on fullscan for the project
			{
				pres.deleteMarkers(MarkerBuilder.VIOLATIONMARKER, false, IResource.DEPTH_INFINITE);
			}
			else
			{
				for (IResource ires : resources)
				{
					String className = "";
					try {												
						className = Util.getJavaNameFromPath(projectBinDir, ires.getLocation().toString(), ".class");
					} catch (FileNotFoundException e) {
						MarkerBuilder.createExceptionMarker(project.getProject(), e, true);
					}
//					System.out.println("Delete markers from " + className);
					
					//fetch java file as resource to remove markers
					IResource javaRes = getResourceByClassName(className, project);

					if(javaRes != null)
						javaRes.deleteMarkers(MarkerBuilder.VIOLATIONMARKER, false, 1);				
				}
			}			
			
			for (Violation v : vios)
			{
				String className = v.getViolatingRelation().getSrcClass(); //name of class where marker will be attached
				
				IResource res = getResourceByClassName(className, project);
				
				String msg = "";
				String relationStr = "";
				String violationStr = "";
				
				Relation r = v.getViolatingRelation();
				Rule rule = v.getViolatedRule();
				switch (r.getRelationType())
				{					
					case ACCESS:
						relationStr = r.getSrcClass() + " accesses " + r.getDestClass();
						msg = r.getSrcClass() + " accesses " + r.getDestClass() + ". This violates: \"";
						switch(rule.getRuleType())
						{
							case NOTACCESS:
								msg += rule.getSrcClass() + " may not access " + rule.getDestClass() + "\"";
								violationStr = rule.getSrcClass() + " may not access " + rule.getDestClass();
								break;
							case ONLYACCESSEDBY:
								msg += rule.getSrcClass() + " may only be accessed by " + rule.getDestClass() + "\"";
								violationStr = rule.getSrcClass() + " may only be accessed by " + rule.getDestClass();
								break;
						}
						break;
					case EXTEND:
						relationStr = r.getSrcClass() + " extends " + r.getDestClass();
						msg = r.getSrcClass() + " extends " + r.getDestClass() + ". This violates: \"";
						switch(rule.getRuleType())
						{
							case NOTEXTEND:
								msg += rule.getSrcClass() + " may not extend " + rule.getDestClass() + "\"";
								violationStr = rule.getSrcClass() + " may not extend " + rule.getDestClass();
								break;
							case ONLYEXTENDBY:
								msg += rule.getSrcClass() + " may only be extended by " + rule.getDestClass() + "\"";
								violationStr = rule.getSrcClass() + " may only be extended by " + rule.getDestClass();
								break;
						}
						break;
					case IMPLEMENT:
						relationStr = r.getSrcClass() + " implements " + r.getDestClass();
						msg = r.getSrcClass() + " implements " + r.getDestClass() + ". This violates: \"";
						switch(rule.getRuleType())
						{
							case NOTIMPLEMENT:
								msg += rule.getSrcClass() + " may not implement " + rule.getDestClass() + "\"";
								violationStr = rule.getSrcClass() + " may not implement " + rule.getDestClass();
								break;
							case ONLYIMPLEMENTEDBY:
								msg += rule.getSrcClass() + " may only be implemented by " + rule.getDestClass() + "\"";
								violationStr = rule.getSrcClass() + " may only be implemented by " + rule.getDestClass();
								break;
						}
						break;
					case INVOKE:
						relationStr = r.getSrcClass() + " invokes " + r.getDestClass();
						msg = r.getSrcClass() + " invokes " + r.getDestClass() + ". This violates: \"";
						switch(rule.getRuleType())
						{
							case NOTINVOKE:
								msg += rule.getSrcClass() + " may not invoke " + rule.getDestClass() + "\"";
								violationStr = rule.getSrcClass() + " may not invoke " + rule.getDestClass();
								break;
							case ONLYINVOKEDBY:
								msg += rule.getSrcClass() + " may only be invoked by " + rule.getDestClass() + "\"";
								violationStr = rule.getSrcClass() + " may only be invoked by " + rule.getDestClass();
								break;
						}
						break;					
				}
				
				int lineNumber = v.getViolatingRelation().getLineNumber();
				if(lineNumber == -1) lineNumber = 1;

				//attach custom violation marker to the eclipse resource
				MarkerBuilder.createViolationMarker(res, violationStr, relationStr, msg, lineNumber);
				
			}
			long stop = System.currentTimeMillis();
			System.out.println("Millis Marker: " + (stop-start));
		}
		
		return Status.OK_STATUS;
	}
	
	private IResource getResourceByClassName(String className, IJavaProject p)
	{
		className = className + ".java";
				
		Path pa = new Path(className);				
		IJavaElement elem = null;
		try {
			elem = p.findElement(pa);
		} catch (JavaModelException e) {
			MarkerBuilder.createExceptionMarker(p.getProject(), e, true);
			//e.printStackTrace();
		}
		
		IResource res = null;
		if(elem != null)
		{
			res = elem.getResource();
		}
		return res;
	}

}
