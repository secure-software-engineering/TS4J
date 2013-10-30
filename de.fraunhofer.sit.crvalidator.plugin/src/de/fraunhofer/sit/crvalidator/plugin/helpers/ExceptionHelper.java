package de.fraunhofer.sit.crvalidator.plugin.helpers;

public class ExceptionHelper {

	/**
	 * Concatenates the output of printStackTrace() of an exception to a string
	 * @param e: exception whose information should to be converted to a string 
	 * @return
	 */
	public static String exceptionToString(Exception e, boolean showStackTrace)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.getMessage());
		
		if(showStackTrace)
		{
			sb.append("\n");
			for (StackTraceElement ste : e.getStackTrace())
			{
				sb.append(ste.toString());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
}
