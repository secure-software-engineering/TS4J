package examples;

import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

public class Main {
	public static void main(String[] args) {
		String a = "dssaasasssass";
		String b = "sss";
		b+="2";
		byte[] keyBytes = b.getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		
	}

	private static String getString() {
		// TODO Auto-generated method stub
		return "test";
	}
}