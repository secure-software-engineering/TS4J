package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class Case5 {


	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public static void main(String[] args) {
		int necessaryStmt;
		String keyString = "Dieser Schsaluesssel";
		String b = " ist geheim";
		keyString += b;
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}
