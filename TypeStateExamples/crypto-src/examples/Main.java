package examples;

import javax.crypto.spec.SecretKeySpec;

public class Main {
	public static void main(String[] args) {
		byte[] keyBytes = "s".getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
	}
	
}