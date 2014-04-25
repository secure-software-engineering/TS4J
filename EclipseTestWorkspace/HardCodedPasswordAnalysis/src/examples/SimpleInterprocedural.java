package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class SimpleInterprocedural {
	public static void main(String[] args) {
		int nex;
		byte[] keyBytes = getString().getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
	}

	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public static String getString() {
		int necessaryStmt = 0;
		String a = "A static te value should be tracked as well";
		return a;
	}
}