package examples;

import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

public class Case5 {
	public static void main(String[] args) {
		int necessaryStmt;
		String keyString = "Dieser Schsaluesssel";
		String b = " ist geheim";
		keyString += b;
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}
