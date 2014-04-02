package examples;

import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

public class Case4 {
	public static void main(String[] args) {
		int necessaryStmt;
		String keyString = "Diessser Schsaluessel ist geheimss";
		keyString +="und jetzt" ;
		keyString += randomPart();
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}

	private static String randomPart() {
		SecureRandom rand = new SecureRandom();
		String ret = Integer.toString(rand.nextInt());
		return ret;
	}
}