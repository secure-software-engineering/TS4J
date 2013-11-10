package de.fraunhofer.sit.codescan.framework;

/**
 * Configures a single analysis pass.
 */
public interface AnalysisConfiguration {
	
	/**
	 * The subsignature of methods at which this analysis should start.
	 * <code>Example: void foo(java.lang.String)</code>.
	 */
	String getMethodSubSignature();
	
	/**
	 * If not <code>null</code> then the analysis will only be applied to
	 * methods that match the subsignature returned by {@link #getMethodSubSignature()} and
	 * which are subtypes of the class returned by this method.
	 */
	String getSuperClassName();
		
	/**
	 * Returns the analysis plugin that actually conducts the analysis.
	 */
	IFDSAnalysisPlugin getAnalysisPlugin();
}
