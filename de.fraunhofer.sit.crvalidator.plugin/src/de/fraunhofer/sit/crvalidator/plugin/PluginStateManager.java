package de.fraunhofer.sit.crvalidator.plugin;

import java.util.HashMap;

import de.fraunhofer.sit.crvalidator.validator.RuleValidator;

/**
 * Manages which project has been fully scanned and provides one single access to the core lib (RuleValidator)
 * @author triller
 *  
 */
public class PluginStateManager {

	//true means the project needs to be fully scanned!
	private static HashMap<String, Boolean> projectsFullScanStatus = new HashMap<String, Boolean>(); 
	
	public static final RuleValidator ruleValidator = new RuleValidator();
	
	/**
	 * Provides information whether the project needs to be fully scanned
	 * @param project: Name of the project
	 * @return true if the project needs to be fully scanned
	 */
	public static Boolean getProjectFullScanStatus(String project)
	{
		if(projectsFullScanStatus.containsKey(project))
		{
			return projectsFullScanStatus.get(project);
		}
		else
		{
			projectsFullScanStatus.put(project,false);
			return true;			
		}
	}
	
	/**
	 * Set full scan status of a project, true if fullscan is neccessary
	 * @param project: Name of the project
	 * @param full: true if fullscan is neccessary
	 */
	public static void setProjectFullScanStatus(String project, Boolean full)
	{
		projectsFullScanStatus.put(project, full);
	}
	
}
