package ivexamples;

import javax.crypto.spec.IvParameterSpec;

public class ConstantWithVariable {
	public static void main(String[] args){
		byte va=122;
		byte sa=12;

		byte[] iv = {1,va,0,2,1,-128,va,va,sa,16,1,2,1,9,0,1};
		IvParameterSpec ivs = new IvParameterSpec(iv);
	}

}
