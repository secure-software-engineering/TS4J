package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class Intraprocedural {
	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public static void main(String[] args) {
		String keyString = "Dieser Schluessel ist geheim";
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}