package examples;

import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class Case2 {
	@DefinitelyVulnerable
	public static void main(String[] args) {
		byte[] keyBytes = getString().getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		
	}

	private static String getString() {
		int necessaryStmt = 0;
		String a = "A static return value should be tracked as well";
		return a;
	}
}