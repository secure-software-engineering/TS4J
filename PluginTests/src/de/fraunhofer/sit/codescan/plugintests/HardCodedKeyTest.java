package de.fraunhofer.sit.codescan.plugintests;

import org.junit.Test;

public class HardCodedKeyTest extends AbstractTest {
	private static final String HARD_CODED_KEY_CLASS = "HardCodedKeyAnalysisPlugin";
	
	@Test
	public void testCheckHardCodedKeyVulnerables() throws InterruptedException {
		checkDefinitvlyVulnerable(HARD_CODED_KEY_CLASS);
	}

	@Test
	public void testCheckHardCodedKeyFalseNegatives() throws InterruptedException {
		checkFalseNegatives(HARD_CODED_KEY_CLASS);
	}
}
