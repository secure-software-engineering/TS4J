package ivexamples;

import javax.crypto.spec.IvParameterSpec;

public class Case1 {
	public static void main(String[] args){
		byte[] test1 = {1};
		String a = "test";
		byte[] iv = {1,2,1,2,1,9,8,12 ,1,6,1,2,1,9,9,1};
		iv[11] = getByte();
		IvParameterSpec tesst = new IvParameterSpec(iv);
	}

	private static byte getByte() {
		// TODO Auto-generated method stub
		return 1;
	}
}
