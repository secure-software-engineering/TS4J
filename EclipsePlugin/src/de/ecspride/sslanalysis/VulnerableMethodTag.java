package de.ecspride.sslanalysis;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * A tag indicating that a method is vulnerable.
 */
public class VulnerableMethodTag implements Tag {

	public String getName() {
		return VulnerableMethodTag.class.getName();
	}

	public byte[] getValue() throws AttributeValueException {
		throw new UnsupportedOperationException();
	}

}
