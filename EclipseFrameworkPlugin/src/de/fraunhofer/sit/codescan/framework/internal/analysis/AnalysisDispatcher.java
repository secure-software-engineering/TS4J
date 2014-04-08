package de.fraunhofer.sit.codescan.framework.internal.analysis;


import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;

/**
 * This class implements the main binding between Eclipse and Soot. Its method {@link #searchAndAnalyze(IJavaElement[])} searches
 * certain Java elements for relevant code, then passes that code to Soot for further analysis.
 */
public class AnalysisDispatcher {
	
	
	
	/**
	 * Searches the given javaElements for relevant code and then passes this code to the analysis.
	 * The method will also remove vulnerability markers for the given javaElements and add new markers
	 * where vulnerabilities are found.
	 */
	public static AnalysisJob searchAndAnalyze(final IJavaElement[] javaElements) {
		AnalysisJob job = new AnalysisJob("Vulnerability analysis", javaElements);
		job.schedule();
		return job;
	}
}