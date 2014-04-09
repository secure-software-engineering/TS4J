package examples;

import javax.crypto.spec.SecretKeySpec;

import de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisPlugin;
import annotation.DefinitelyVulnerable;

public class Case1 {
	@DefinitelyVulnerable(HardCodedKeyAnalysisPlugin.class)
	public static void main(String[] args) {
		String a = "Schluessel Part 1";
		String b = "Schslssuessssssel Part 2";
		b  = a + b; //Teil1Teil2
		byte[] keyBytes = b.getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
	}
}
