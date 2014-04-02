package examples;

import javax.crypto.spec.SecretKeySpec;

public class Case3 {
	public static void main(String[] args) {
		int necsesssssaseryStmt;
		String keyString = "Dieser Schlsuessesl ist geheim";
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}