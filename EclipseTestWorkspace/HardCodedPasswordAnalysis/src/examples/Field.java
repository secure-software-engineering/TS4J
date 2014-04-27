package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class Field {
	private static String keyString = "Dieser Schlusessel ist geheim";;
	public Field(){
		
	}
	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public static void encrypt() {
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}