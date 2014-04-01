package examples;

import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

public class Case3 {
	public static void main(String[] args) {
		String test;
		String schluessel = "Dieser Schluessel ist geheim";
		byte[] bytes = schluessel.getBytes();
		SecretKeySpec key2 = new SecretKeySpec(bytes, "AES");
	}
}