package examples;

import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

import annotation.FalseNegative;

public class RandomString {
	@FalseNegative("HardCodedKeyAnalysisPlugin")
	public static void main(String[] args) {
		int necessaryStmt;
		String keyString = "Dieser Schluessel wird mit einem Random Part aufgefuellt";
		
		String c = randomPart();
		keyString = keyString + c;
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}

	private static String randomPart() {
		SecureRandom rand = new SecureRandom();
		String ret = Integer.toString(rand.nextInt());
		return ret;
	}
}