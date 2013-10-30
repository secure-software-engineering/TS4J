package de.fraunhofer.sit.crvalidator.plugin.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import de.fraunhofer.sit.crvalidator.exception.NoRulesFoundException;
import de.fraunhofer.sit.crvalidator.exception.UnknownRuleException;
import de.fraunhofer.sit.crvalidator.plugin.helpers.ExceptionHelper;

/**
 * Creates custom marker
 * @author triller
 *
 */
public class MarkerBuilder {

	public static final String VIOLATIONMARKER = "de.fraunhofer.sit.crvalidator.plugin.markers.violationmarker";
	public static String EXCEPTIONMARKER = "de.fraunhofer.sit.crvalidator.plugin.markers.exceptionmarker";
	
	/**
	 * Create a custom violation marker for a eclipse resource
	 * 
	 * @param res: Eclipse resource where the marker should be attached to
	 * @param violation: Violation as string that should be shown
	 * @param relation: Relation as string that should be shown
	 * @param description: Description for the marker that should be shown
	 * @param lineNumber: line number where the marker should point to within the resource
	 * @throws CoreException
	 */
	public static void createViolationMarker(IResource res, String violation, String relation, String description, int lineNumber) throws CoreException
	{
		IMarker marker = res.createMarker(VIOLATIONMARKER);
		marker.setAttribute(IMarker.MESSAGE, description);
		marker.setAttribute("relation", relation);
		marker.setAttribute("violation", violation);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);		
	}
	
	/**
	 * Creates custom marker on eclipse resources where exceptions occurred
	 * 
	 * @param res: eclipse resource where the marker should be attached to
	 * @param exception: exception that should be attached to the resource
	 */
	public static void createExceptionMarker(IResource res, Exception exception, boolean showStackTrace)	
	{
		String exceptionStr = "";
		int lineNumber = -1;
		
		if (exception instanceof NoRulesFoundException)
		{
			exceptionStr = "rules.txt is empty";
		}
		else if (exception instanceof UnknownRuleException)
		{
			UnknownRuleException ure = (UnknownRuleException) exception;
			exceptionStr = "Unknown rule found";
			lineNumber = ure.getLineNumber();				
		}
		else
		{
			exceptionStr = ExceptionHelper.exceptionToString(exception, showStackTrace);
		}
		
		IMarker marker;
		try {
			marker = res.createMarker(EXCEPTIONMARKER);
			
			marker.setAttribute(IMarker.MESSAGE, exceptionStr);
			marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			
			if(lineNumber > 0)
			{
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			}			
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			
		} catch (CoreException e) {
			System.err.println("Fatal Error, could not create Exception marker for resource: ");
			System.err.println(res + "\n");
			System.err.println("Original Exception String:" + exceptionStr + "\n");
			e.printStackTrace();
		}
	}
	
}
