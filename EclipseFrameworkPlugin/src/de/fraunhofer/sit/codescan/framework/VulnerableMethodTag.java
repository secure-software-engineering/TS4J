package de.fraunhofer.sit.codescan.framework;

import soot.SootMethod;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * A tag indicating that a method is vulnerable. The analysis will attach this
 * tag to a {@link SootMethod} if that method was found to be vulnerable.
 * This tag is currently used internally within the plugin but also by the 
 * test harness (which is why it is not internal).
 */
public final class VulnerableMethodTag implements Tag {

	private final AnalysisConfiguration config;

	public VulnerableMethodTag(AnalysisConfiguration config) {
		this.config = config;
	}

	public AnalysisConfiguration getAnalysisConfig() {
		return config;
	}

	public String getName() {
		return VulnerableMethodTag.class.getName();
	}

	public byte[] getValue() throws AttributeValueException {
		throw new UnsupportedOperationException();
	}

}
