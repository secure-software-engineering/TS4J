package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;
import de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisPlugin;

public class Case2 {
	public static void main(String[] args) {
		byte[] keyBytes = getString().getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
	}

	@DefinitelyVulnerable(HardCodedKeyAnalysisPlugin.class)
	public static String getString() {
		int necessaryStmt = 0;
		String a = "A static return value should be tracked as well";
		return a;
	}
}