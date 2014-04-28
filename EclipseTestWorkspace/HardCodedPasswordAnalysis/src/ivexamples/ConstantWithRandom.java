package ivexamples;

import java.util.Random;

import javax.crypto.spec.IvParameterSpec;

public class ConstantWithRandom {
	public static void main(String[] args){
		int necss;
		byte value1=22;
		byte value2=random();
		byte[] iv = {1,value1,0,2,1,-128,value1,value1,value2,6,1,2,1,0,9,1};
		IvParameterSpec ivs = new IvParameterSpec(iv);
	}

	private static byte random() {
		Random random = new Random();
		return (byte) (random.nextInt() % 10);
	}

}
