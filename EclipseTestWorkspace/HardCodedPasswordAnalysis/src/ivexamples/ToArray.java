package ivexamples;

import javax.crypto.spec.IvParameterSpec;

public class ToArray {
	public static void main(String[] args){

		byte[] iv = new byte[16];
		for(int i = 1; i < 17; i++){
			iv[i-1] = (byte) i;
		}
		IvParameterSpec spec = new IvParameterSpec(iv);
	}

}
