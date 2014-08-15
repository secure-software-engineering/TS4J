package seededrandom;

import java.security.SecureRandom;

public class SeededConstant {

	public static void main(String[] args) {
		byte[] SEED = new byte[] {0};
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.setSeed(SEED);

	}

}
