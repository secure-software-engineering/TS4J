package de.fraunhofer.sit.codescan.framework;

public interface IAnalysisPlugin {

	public interface IFilter {
		/**
		 * If not <code>null</code> then the analysis will only be applied to
		 * methods that match the subsignature returned by {@link #getMethodSubSignature()} and
		 * which are subtypes of the class returned by this method.
		 */
		String getSuperClassName();

		String getDeclSubSignature();

		String getCallSubSignature();
	}

	public IFilter[] getFilters();
	
}
