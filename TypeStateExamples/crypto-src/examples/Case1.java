package examples;

import javax.crypto.spec.SecretKeySpec;

public class Case1 {
	public static void main(String[] args) {
		String a = "Teil1";
		String b = "Tssesil2";
		b  = a + b; //Teil1Teil2
		byte[] keyBytes = b.getBytes();
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
	}
}
