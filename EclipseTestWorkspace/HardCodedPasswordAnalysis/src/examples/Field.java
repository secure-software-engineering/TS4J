package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class Field {
	private String keyString = "Dieser Schlusessel ist geheim";;
	public Field(){
		
	}
	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public void encrypt() {
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}