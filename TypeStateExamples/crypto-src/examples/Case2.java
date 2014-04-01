package examples;

import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

public class Case2 {
	public static void main(String[] args) {
		byte[] keyBytes = getString().getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		
	}

	private static String getString() {
		String c = "testestt";
		String a = "testestt";
		return a;
	}
}