package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class SubstringIntra {
	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public static void main(String[] args) {
		int nex;
		String keystring = "C";
		 keystring = keystring.substring(1);
			String a = "SssssS";
		byte[] keyBytes = keystring.getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
	}
}