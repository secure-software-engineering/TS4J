package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class IntraproceduralWithByteArray {
	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public static void main(String[] args) {
		byte[] bytes = {1,2,1,2,1,1,8,8,1,6,1,2,1,1,9,1};
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}