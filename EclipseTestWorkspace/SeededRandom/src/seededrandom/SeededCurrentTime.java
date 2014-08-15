package seededrandom;

import java.security.SecureRandom;

public class SeededCurrentTime {
	public static void main(String[] args){
		SecureRandom rand = new SecureRandom();
		long time = System.currentTimeMillis();
		rand.setSeed(time);
	}
}
