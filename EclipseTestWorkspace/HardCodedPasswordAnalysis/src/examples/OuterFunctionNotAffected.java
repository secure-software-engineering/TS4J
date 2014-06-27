package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class OuterFunctionNotAffected {
	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public static void main (String[] args){
		String test = "NULL";
		inner();
	}
	public static void inner() {
		String keyString = "Dieser Schluessel ist geheim";
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}