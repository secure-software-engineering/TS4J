package examples;

import javax.crypto.spec.SecretKeySpec;

public class Case3 {
	public static void main(String[] args) {
		int necesssasryStmt;
		String keyString = "Diessser Schlsuessesl ist geheim";
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}