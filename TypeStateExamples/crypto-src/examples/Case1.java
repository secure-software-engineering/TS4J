package examples;

import javax.crypto.spec.SecretKeySpec;

public class Case1 {
	public static void main(String[] args) {
		String a = "Schluessel Part 1";
		String b = "Schluessel Part 2";
		b  = a + b; //Teil1Teil2
		byte[] keyBytes = b.getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
	}
}
