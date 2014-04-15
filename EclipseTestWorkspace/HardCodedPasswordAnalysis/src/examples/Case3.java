package examples;

import javax.crypto.spec.SecretKeySpec;

import de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisPlugin;
import annotation.DefinitelyVulnerable;

public class Case3 {
	@DefinitelyVulnerable(HardCodedKeyAnalysisPlugin.class)
	public static void main(String[] args) {
		int necsesssssaseryStmt;
		String keyString = "Dieser Schlsuessesl ist geheim";
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}