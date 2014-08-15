package seededrandom;

import java.security.SecureRandom;

import javax.crypto.spec.IvParameterSpec;

public class SeededConstantByte {
	//tes
	public static void main(String[] args){
		SecureRandom rand = new SecureRandom();
		byte[] array = {1,2,1,2,1,1,8,1,1,6,1,2,1,9,9,1};
		byte var = 12;
		rand.setSeed(array);
	}
}
