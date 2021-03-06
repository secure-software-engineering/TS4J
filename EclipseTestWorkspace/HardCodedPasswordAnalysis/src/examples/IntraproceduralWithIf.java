package examples;

import javax.crypto.spec.SecretKeySpec;

import annotation.DefinitelyVulnerable;

public class IntraproceduralWithIf {
	@DefinitelyVulnerable("HardCodedKeyAnalysisPlugin")
	public static void main(String[] args) {
		int nsec;
		String keyString = null;
		if(args[0].equals("nicht")){
			keyString = "Dieser Schluessesl ist geheim"; 
		} else {
			keyString = "und trotzdem";
		}
		byte[] bytes = keyString.getBytes();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");
	}
}