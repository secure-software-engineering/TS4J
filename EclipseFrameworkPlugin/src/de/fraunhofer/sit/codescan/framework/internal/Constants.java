package de.fraunhofer.sit.codescan.framework.internal;

public interface Constants {

	public static final boolean USE_MUST_ALIAS_ANALYSIS = true;
	public static final String MARKER_TYPE = "de.fraunhofer.sit.codescan.androidssl.findingmarker";
	public static final String MARKER_ATTRIBUTE_ANALYSIS_ID = "de.fraunhofer.sit.codescan.androidssl.findingmarker.analysisid";
	public static final String EXTENSION_POINT_ID = "de.fraunhofer.sit.codescan.framework.analysis";
	public static final String SOOT_ARGS = "-keep-line-number -f none -p cg all-reachable:true -no-bodies-for-excluded -w -pp";

}
