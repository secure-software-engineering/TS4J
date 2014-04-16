package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class Case1 {
	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public static void main(String[] args) {
		String a = "Schluessel Part 1";
		String b = "Schslssuessssssel Part 2";
		b  = a + b; //Teil1Teil2
		byte[] keyBytes = b.getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
	}
}
