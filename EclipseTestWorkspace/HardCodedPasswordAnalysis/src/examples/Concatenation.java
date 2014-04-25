package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class Concatenation {
	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public static void main(String[] args) {
		int nec;
		String a = "Schluessel Part 1";
		String b = "Scshslussesssel Part 2";
		b  = a + b; //Teil1Teil2
		byte[] keyBytes = b.getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
	}
}
