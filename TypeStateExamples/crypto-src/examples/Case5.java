package examples;

import javax.crypto.spec.SecretKeySpec;

import de.fraunhofer.sit.codescan.typestate.hardcodedkeyanalysis.HardCodedKeyAnalysisPlugin;
import annotation.DefinitelyVulnerable;

public class Case5 {


	@DefinitelyVulnerable(HardCodedKeyAnalysisPlugin.class)
	public static void main(String[] args) {
		int necessaryStmt;
		String keyString = "Dieser Schsaluesssel";
		String b = " ist geheim";
		keyString += b;
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}
